package com.alperez.bt_microphone.bluetoorh.connector.data;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.connector.BtConnector;
import com.alperez.bt_microphone.bluetoorh.connector.OnConnectionStatusListener;
import com.alperez.bt_microphone.utils.ThreadLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by stanislav.perchenko on 3/17/2017.
 */

public class BtDataTransceiverImpl implements BtDataTransceiver {
    public static final String TAG = "BtTrans";

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

    //---  Transmission-related section  ----
    private final TransmissionThread transmThread;

    /**
     * Anyone who wants to work with data transmission must acquire this lock!
     */
    private final Queue<String> transmissionQueue = new LinkedList<>();

    //----  Receiver-related section  ----
    private final ReceivingThread rcvThread;
    private final OnTextDataReceivedListener rcvListener;


    //----  Transceiver status notification section ---
    private final Object statusListenerLock = new Object();
    private OnConnectionStatusListener statusListener;

    /**********************************  Public API section  **************************************/

    public BtDataTransceiverImpl(@NonNull BluetoothDevice device, @NonNull UUID serviceUUID, @NonNull OnTextDataReceivedListener rcvListener) {
        if (device == null) throw new IllegalArgumentException("BluetoothDevice cannot be null");
        if (serviceUUID == null) throw new IllegalArgumentException("Service UUID cannot be null");
        if (rcvListener == null) throw new IllegalArgumentException("Listener cannot be null");
        this.device = device;
        this.serviceUUID = serviceUUID;
        this.rcvListener = rcvListener;

        ThreadLog.d(TAG, "=====  Transceiver created  =======");

        transmThread = new TransmissionThread(getClass().getSimpleName()+": transmission thread");
        rcvThread = new ReceivingThread(getClass().getSimpleName()+": receiving thread");
        transmThread.start();
        rcvThread.start();

        openConnectionAsync(75);
    }

    @Override
    public void setOnTransceiverStatusListener(OnConnectionStatusListener l) {
        synchronized (statusListenerLock) {
            statusListener = l;
        }
    }

    /**
     * Submits new data to the transmission queue. If data String does not end with the "\r\n" sequence,
     * these bytes are added before transmission.
     * @param data
     */
    @Override
    public void sendDataNonBlocked(String data) {
        synchronized (transmissionQueue) {
            ThreadLog.d(TAG, "Request send data - "+data);
            transmissionQueue.add(data);
            transmissionQueue.notify();
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
                    transmThread.interrupt();
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

        if (connReady) {
            synchronized (transmissionQueue) {
                transmissionQueue.notify();
            }
            //TODO Notify receiver thread!
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




    /********************************  Data transmit section  *************************************/
    private class TransmissionThread extends Thread {

        public TransmissionThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            ThreadLog.d(TAG, "---------- Thread started -----------");
            String textToSend = null; //TODO Remove this after testing
            byte[] data = null;
            OutputStream localOS = null;

            while (!released) {

                //--- Wait for data to transmit ---
                if (data == null) {
                    synchronized (transmissionQueue) {
                        while(transmissionQueue.isEmpty()) {
                            try {
                                transmissionQueue.wait();
                            } catch (InterruptedException e) {
                                if (released) {
                                    ThreadLog.e(TAG, "Transmission - thread was interrupted and released");
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                        if (released) {
                            ThreadLog.e(TAG, "Transmission - leave thread early on release (1)");
                            break;
                        }
                        data = BtUtils.prepareDataToTransmit(textToSend = transmissionQueue.remove());
                    }
                }

                ThreadLog.d(TAG, "Transmission - got data to send: "+textToSend);

                if (released) {
                    ThreadLog.e(TAG, "Transmission - leave thread early on release (2)");
                    break;
                }

                //--- get OutputStream safely ---
                if (localOS == null) {
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
                        localOS = oStream;
                    }
                }

                ThreadLog.d(TAG, "Transmission - got OutputStream");


                if (data != null && localOS != null && !released) {
                    try {
                        localOS.write(data);
                        localOS.flush();
                        data = null;

                        ThreadLog.d(TAG, "Transmission - Data sent OK");

                        Thread.sleep(450);

                    } catch (IOException e) {
                        ThreadLog.e(TAG, "Transmission - Data sent ERROR - "+e.getMessage()+".   Try reconnect");
                        //---  RECONNECT!!!  ---
                        localOS = null; //Reset connection!
                        reconnectAfterFailure(Thread.currentThread().getName(), e);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                }

            }// Top-level thread cycle

            ThreadLog.e(TAG, "---------- Thread finished -----------");

        }//run()
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

                /*if (localIS != null && !released) {
                    byte[] buffer = new byte[16];
                    int nBytes;
                    while(true) {
                        try {
                            nBytes = localIS.read(buffer);
                            ThreadLog.d(TAG, nBytes+" bytes received");
                        } catch (IOException e) {

                            ThreadLog.e(TAG, "Receiving - Data sent ERROR - "+e.getMessage()+".   Try reconnect");
                            localIS = null;
                            reconnectAfterFailure(Thread.currentThread().getName(), e);
                            break;
                        }
                    }
                }*/


                if (localIS != null && !released) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);

                    try {
                        int symb;
                        byte b_1=0, b_2=0;
                        while ((symb = localIS.read()) >= 0 && !released) {
                            //ThreadLog.e(TAG, "Got character - "+(char)symb);
                            b_2 = b_1;
                            b_1 = (byte)symb;
                            bos.write(symb);
                            if ((b_2 == 0x0D && b_1 == 0x0A) || (b_2 == '/' && b_1 == 'n')) {
                                byte[] data = bos.toByteArray();
                                String textData = new String(data, 0, data.length-2);
                                bos.reset();
                                rcvListener.onReceive(textData);
                            }
                        }
                        if (!released) {
                            throw new IOException("Input thread ends");
                        }
                    } catch (IOException e) {
                        ThreadLog.e(TAG, "Receiving - Data sent ERROR - "+e.getMessage()+".   Try reconnect");
                        localIS = null;
                        reconnectAfterFailure(Thread.currentThread().getName(), e);
                    } finally {
                        BtUtils.silentlyCloseCloseable(bos);
                    }

                }





            }
            ThreadLog.e(TAG, "---------- Thread finished -----------");
        }
    }




}
