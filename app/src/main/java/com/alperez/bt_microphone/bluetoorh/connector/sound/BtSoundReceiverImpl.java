package com.alperez.bt_microphone.bluetoorh.connector.sound;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.connector.BtConnector;
import com.alperez.bt_microphone.bluetoorh.connector.OnTransceiverStatusListener;
import com.alperez.bt_microphone.utils.ThreadLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public class BtSoundReceiverImpl implements BtSoundReceiver {
    public static final String TAG = "BtTrans";

    public static final int RCV_BUFF_SIZE = 2048;

    //--- Connection entry point ---
    private BluetoothDevice device;
    private UUID serviceUUID;

    //---  Exit condition ---
    private volatile boolean released;


    //--- Connection-related objects ---
    /**
     * Anyone who wants to work with connection must acquire this lock!
     */
    private final Object connectorLock = new Object();
    private BluetoothSocket socket;
    private InputStream iStream;
    private OutputStream oStream;
    private volatile boolean dataConnectionReady;
    private volatile int numConnectionTry;


    //----  Receiver-related section  ----
    private final ReceivingThread rcvThread;
    private final OnSoundDataReceivedListener rcvListener;
    private final byte[] receiverResultdataBuffer = new byte[RCV_BUFF_SIZE];


    //----  Transceiver status notification section ---
    private final Object statusListenerLock = new Object();
    private OnTransceiverStatusListener statusListener;

    /**********************************  Public API section  **************************************/

    public BtSoundReceiverImpl(@NonNull BluetoothDevice device, @NonNull UUID serviceUUID, @NonNull OnSoundDataReceivedListener rcvListener) {
        if (device == null) throw new IllegalArgumentException("BluetoothDevice cannot be null");
        if (serviceUUID == null) throw new IllegalArgumentException("Service UUID cannot be null");
        if (rcvListener == null) throw new IllegalArgumentException("Listener cannot be null");
        this.device = device;
        this.serviceUUID = serviceUUID;
        this.rcvListener = rcvListener;

        ThreadLog.d(TAG, "=====  Transceiver created  =======");

        rcvThread = new ReceivingThread(getClass().getSimpleName()+": receiving-thread");
        rcvThread.start();

        openConnectionAsync(75);
    }

    @Override
    public void setOnTransceiverStatusListener(OnTransceiverStatusListener l) {
        synchronized (statusListenerLock) {
            statusListener = l;
        }
    }



    /**
     * An instance of the BtDataTransceiverImpl cannot be used after release
     */
    @Override
    public void release() {
        if (!released) {
            synchronized (connectorLock) {
                if (!released) {
                    ThreadLog.e(TAG, "~~~~~~~~  Release transceiver  ~~~~~~~~~~~~");
                    released = true;
                    rcvThread.interrupt();
                    closeConnectionSilently();
                }
            }
        }
    }


    /*********************  Managing Connection section  ******************************************/

    private void openConnectionAsync(int initDelay) {
        if (!dataConnectionReady && !released) {
            synchronized (connectorLock) {
                if (!dataConnectionReady && !released) {
                    numConnectionTry ++;


                    ThreadLog.d(TAG, String.format("---->> Open connection async with initial delay %d ms, Ntry = %d", initDelay, numConnectionTry));


                    BtConnector.forDevice(device, serviceUUID)
                            .preConnectionDelay(initDelay)
                            .connectAsync(new BtConnector.OnDeviceConnectListener() {
                                @Override
                                public void onConnected(BluetoothSocket socket, InputStream iStream, OutputStream oStream) {
                                    // Instantiate connection data. Released condition is checked inside
                                    setConnection(socket, iStream, oStream);
                                }

                                @Override
                                public void onConnectionFailure(Throwable reason) {
                                    //---  Notify status listener  ---
                                    synchronized (statusListenerLock) {
                                        if (statusListener != null) {
                                            statusListener.onConnectionAttemptFailed(numConnectionTry);
                                        }
                                    }

                                    ThreadLog.e(TAG, "~~~~~~>>  onConnectionFailed() - "+reason.getMessage());

                                    //--- Try again with delay. Released condition is checked inside ---
                                    openConnectionAsync(750);
                                }
                            });
                }
            }
        }
    }

    private void setConnection(BluetoothSocket socket, InputStream iStream, OutputStream oStream) {
        boolean connReady = false;
        if (!dataConnectionReady && !released) {
            synchronized (connectorLock) {
                if (!dataConnectionReady && !released) {
                    ThreadLog.d(TAG, "=======>> Set new opened connection!");
                    dataConnectionReady = true;
                    this.socket = socket;
                    this.iStream = iStream;
                    this.oStream = oStream;
                    connReady = true;
                    connectorLock.notifyAll();


                    //---  Notify status listener  ---
                    synchronized (statusListenerLock) {
                        if (statusListener != null) {
                            statusListener.onConnectionRestorted(numConnectionTry);
                        }
                    }

                }
            }
        }
    }

    private void closeConnectionSilently() {
        if (dataConnectionReady) {
            synchronized (connectorLock) {
                if (dataConnectionReady) {
                    ThreadLog.e(TAG, "!!!!  Close connection silently !!!!");
                    dataConnectionReady = false;
                    BtUtils.silentlyCloseCloseable(iStream);
                    BtUtils.silentlyCloseCloseable(oStream);
                    BtUtils.silentlyCloseCloseable(socket);
                    iStream = null;
                    oStream = null;
                    socket = null;
                }
            }
        }
    }

    /**
     * This method must be called either from transmit or receiver Thread in case of exception over
     * data streams.
     * @param failedThreadName
     * @param failureReason
     */
    private synchronized void reconnectAfterFailure(String failedThreadName, Throwable failureReason) {
        if (!dataConnectionReady) {
            ThreadLog.e(TAG, "Reconnect after failure -> Process already started");
            return;
        }

        ThreadLog.d(TAG, "===> Reconnect after failure");

        closeConnectionSilently();

        synchronized (statusListenerLock) {
            if (statusListener != null) {
                statusListener.onConnectionBroken(failedThreadName, failureReason);
            }
        }

        openConnectionAsync(50);
    }





    private class ReceivingThread extends Thread {

        public ReceivingThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            ThreadLog.d(TAG, "---------- Thread started -----------");
            InputStream localIS = null;
            while(!released) {

                //--- get OutputStream safely ---
                if (localIS == null) {
                    synchronized (connectorLock) {
                        while (!dataConnectionReady) {
                            try {
                                connectorLock.wait();
                            } catch (InterruptedException e) {
                                if (released) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                        localIS = iStream;
                    }
                }

                if (localIS != null && !released) {
                    byte[] buffer = new byte[RCV_BUFF_SIZE];
                    int nBytes;


                    long tStart = 0;
                    int cntrBytes = 0;


                    while(true) {
                        try {
                            nBytes = localIS.read(buffer);

                            long currT = System.currentTimeMillis();
                            long dt = currT - tStart;
                            if (tStart == 0) {
                                tStart = currT;
                                cntrBytes = 0;
                            } else {
                                cntrBytes += nBytes;
                                if (dt > 1600) {
                                    rcvListener.onDataRateMeasured(cntrBytes*1000f / dt);
                                    tStart = 0;
                                }
                            }



                            //ThreadLog.d(TAG, nBytes+" received");

                            synchronized (receiverResultdataBuffer) {
                                System.arraycopy(buffer, 0, receiverResultdataBuffer, 0, nBytes);
                                rcvListener.onDataReceiver(receiverResultdataBuffer, 0, nBytes);
                            }

                        } catch (IOException e) {

                            ThreadLog.e(TAG, "Receiving - Data sent ERROR - "+e.getMessage()+".   Try reconnect");
                            localIS = null;
                            reconnectAfterFailure(Thread.currentThread().getName(), e);
                            break;
                        }
                    }
                }




            }
            ThreadLog.e(TAG, "---------- Thread finished -----------");
        }
    }



}
