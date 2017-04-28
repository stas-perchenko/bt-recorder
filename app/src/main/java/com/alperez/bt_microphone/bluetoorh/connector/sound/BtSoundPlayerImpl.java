package com.alperez.bt_microphone.bluetoorh.connector.sound;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.media.AudioTrack;

import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.connector.OnConnectionStatusListener;
import com.alperez.bt_microphone.utils.ThreadLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by stanislav.perchenko on 3/19/2017.
 */

public class BtSoundPlayerImpl implements BtSoundPlayer {
    public static final String TAG = "BtSoundPlayer";


    private static final int MAX_FRAME_SIZE = 512;

    private SoundLevelMeter soundLevelMeter;

    private BluetoothDevice device;
    private UUID serviceUUID;
    private AudioTrack aTrack;




    private Thread workThread;
    private volatile boolean released;

    /**
     * Whether playback enabled or disabled by a client code
     */
    private final AtomicBoolean playEnabled = new AtomicBoolean(false);

    /**
     * Whether audio player really plays sound back
     */
    private final AtomicBoolean trackPlaying = new AtomicBoolean(false);

    /**
     * Whether Bluetooth connection established or not
     */
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public BtSoundPlayerImpl(BluetoothDevice device, UUID serviceUUID, AudioTrack aTrack, SoundLevelMeter soundLevelMeter) {
        this.device = device;
        this.serviceUUID = serviceUUID;
        this.aTrack = aTrack;
        this.soundLevelMeter = soundLevelMeter;

        workThread = new Thread(() -> workerMethodFinalized(), "bt-player");
        workThread.start();
    }

    @Override
    public void setOnPlayerPerformanceListener(OnPlayerPerformanceListener l) {
        synchronized (playerPerformanceListenerLock) {
            playerPerformanceListener = l;
        }
    }

    @Override
    public void setOnConnectionStatusListener(OnConnectionStatusListener l) {
        synchronized (connStatusListenerLock) {
            connStatusListener = l;
        }
    }

    @Override
    public void play() {
        if (released) throw new IllegalStateException("Already released");

        playEnabled.set(true);
    }

    @Override
    public void pause() {
        if (released) throw new IllegalStateException("Already released");

        playEnabled.set(false);
    }

    @Override
    public void release() {
        if (!released) {
            playEnabled.set(false);
            released = true;
            workThread.interrupt();
            workThread = null;
            connected.set(false);
            BtUtils.silentlyCloseCloseable(soc);
        }
    }


    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public boolean isPlaying() {
        return playEnabled.get();
    }

    @Override
    public boolean isAudioTrackPlaying() {
        return trackPlaying.get();
    }




    /**********************************************************************************************/
    private void workerMethodFinalized() {
        try {
            workerMethodNonFinalized();
        } finally {
            //----  Close and release all resources  ----
            BtUtils.silentlyCloseCloseable(iStream);
            BtUtils.silentlyCloseCloseable(soc);
            aTrack.pause();
            trackPlaying.set(false);
            aTrack.flush();
            aTrack.release();
        }
    }


    private BluetoothSocket soc = null;
    private InputStream iStream = null;
    private int nReconnects;
    private int nConnFailed;
    private void workerMethodNonFinalized() {
        while (!released) {


            while (!connected.get() && !released) {
                try {
                    connect();
                    connected.set(true);
                    nConnFailed = 0;
                    notifyConnectionEstablished(++ nReconnects);
                } catch (IOException e) {
                    if (!released) {
                        notifyConnectionAttemptFailed(++ nConnFailed);
                    }
                }
            }

            if (released) {
                return;
            }


            try {
                int cntrPrePlayBytes = 0;
                byte[] buffer = new byte[MAX_FRAME_SIZE];
                while (true) {
                    int nBytes = iStream.read(buffer);

                    ThreadLog.d(TAG, nBytes+" bytes got");

                    notifyBytesReceived(nBytes);
                    if (playEnabled.get()) {
                        //--- Mode PLAY ---
                        aTrack.write(buffer, 0, Math.round(nBytes));


                        soundLevelMeter.submitSamples(buffer, 0, nBytes);


                        ThreadLog.d(TAG, nBytes+" bytes writen ~~~~~~");
                        if (!trackPlaying.get()) {
                            cntrPrePlayBytes += nBytes;
                            if (cntrPrePlayBytes >= 1024) {
                                ThreadLog.d(TAG, "------->  Track start");
                                aTrack.play();
                                trackPlaying.set(true);
                                notifyBytesPlayed(cntrPrePlayBytes);
                                cntrPrePlayBytes = 0;
                            }
                        } else {
                            notifyBytesPlayed(nBytes);
                        }
                    } else {
                        //--- Mode PAUSE ---
                        if (trackPlaying.get()) {
                            ThreadLog.d(TAG, "------->  Track pause");
                            aTrack.pause();
                            trackPlaying.set(false);
                        }
                    }

                    if (released) return;
                }
            } catch(IOException e) {
                if (trackPlaying.get()) {
                    ThreadLog.d(TAG, "------->  Track stop");
                    aTrack.stop();
                    trackPlaying.set(false);
                }
                connected.set(false);
                notifyConnectionBroken(e);
                BtUtils.silentlyCloseCloseable(iStream);
                BtUtils.silentlyCloseCloseable(soc);
            }


        } // TOP while(!released)
    }


    private void connect() throws IOException {
        soc = device.createRfcommSocketToServiceRecord(serviceUUID);
        soc.connect(); // Do the actual connecting job!!!!
        iStream = soc.getInputStream();
    }





    /******************  Notify client code via listeners  ****************************************/
    private final Object playerPerformanceListenerLock = new Object();
    private volatile OnPlayerPerformanceListener playerPerformanceListener;
    private final Object connStatusListenerLock = new Object();
    private volatile OnConnectionStatusListener connStatusListener;


    private void notifyBytesReceived(int nBytes) {
        if (playerPerformanceListener != null) {
            synchronized (playerPerformanceListenerLock) {
                if (playerPerformanceListener != null) {
                    playerPerformanceListener.onBytesReceived(nBytes);
                }
            }
        }
    }

    private void notifyBytesPlayed(int nBytes) {
        if (playerPerformanceListener != null) {
            synchronized (playerPerformanceListenerLock) {
                if (playerPerformanceListener != null) {
                    playerPerformanceListener.onBytesPlayed(nBytes);
                }
            }
        }
    }



    private void notifyConnectionEstablished(int numReconnects) {
        if (connStatusListener != null) {
            synchronized (connStatusListenerLock) {
                if (connStatusListener != null) {
                    connStatusListener.onConnectionRestorted(numReconnects);
                }
            }
        }
    }

    private void notifyConnectionAttemptFailed(int nTry) {
        if (connStatusListener != null) {
            synchronized (connStatusListenerLock) {
                if (connStatusListener != null) {
                    connStatusListener.onConnectionAttemptFailed(nTry);
                }
            }
        }
    }

    private void notifyConnectionBroken(Throwable reason) {
        if (connStatusListener != null) {
            synchronized (connStatusListenerLock) {
                if (connStatusListener != null) {
                    connStatusListener.onConnectionBroken(Thread.currentThread().getName(), reason);
                }
            }
        }
    }

}
