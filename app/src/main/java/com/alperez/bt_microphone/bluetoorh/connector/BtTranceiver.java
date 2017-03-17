package com.alperez.bt_microphone.bluetoorh.connector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.BtUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by stanislav.perchenko on 3/17/2017.
 */

public class BtTranceiver {

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
    private final OnDataReceivedListener rcvListener;


    //----  Transceiver status notification section ---
    private final Object statusListenerLock = new Object();
    private OnTransceiverStatusListener statusListener;

    /**********************************  Public API section  **************************************/

    public BtTranceiver(@NonNull BluetoothDevice device, @NonNull UUID serviceUUID, @NonNull OnDataReceivedListener rcvListener) {
        this.device = device;
        this.serviceUUID = serviceUUID;
        this.rcvListener = rcvListener;

        transmThread = new TransmissionThread(getClass().getSimpleName()+": transmission thread");
        rcvThread = new ReceivingThread(getClass().getSimpleName()+": receiving thread");
        transmThread.start();
        rcvThread.start();

        openConnectionAsync(75);
    }

    public void setOnTransceiverStatusListener(OnTransceiverStatusListener l) {
        synchronized (statusListenerLock) {
            statusListener = l;
        }
    }

    /**
     * Submits new data to the transmission queue. If data String does not end with the "\r\n" sequence,
     * these bytes are added before transmission.
     * @param data
     */
    public void sendDataNonBlocked(String data) {
        synchronized (transmissionQueue) {
            transmissionQueue.add(data);
            transmissionQueue.notify();
        }
    }



    /**
     * An instance of the BtTranceiver cannot be used after release
     */
    public void release() {
        if (!released) {
            synchronized (connectorLock) {
                if (!released) {
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
            //TODO Log this event
            return;
        }

        //TODO Log here
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
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                        data = BtUtils.prepareDataToTransmit(transmissionQueue.remove());
                    }
                }


                if (released) break;

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




                if (data != null && localOS != null && !released) {
                    try {
                        localOS.write(data);
                    } catch (IOException e) {
                        //---  RECONNECT!!!  ---
                        localOS = null; //Reset connection!
                        reconnectAfterFailure(Thread.currentThread().getName(), e);
                    }
                }

            }// Top-level thread cycle

        }//run()
    }


    private class ReceivingThread extends Thread {

        public ReceivingThread(String name) {
            super(name);
        }

        //TODO Real implementation here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        @Override
        public void run() {
            while(released) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    public interface OnDataReceivedListener {
        void onReceive(String data);
    }

    public interface OnTransceiverStatusListener {
        void onConnectionRestorted(int nTry);
        void onConnectionAttemptFailed(int nTry);
        void onConnectionBroken(String nameThreadCauseFailure, Throwable reason);
    }
}
