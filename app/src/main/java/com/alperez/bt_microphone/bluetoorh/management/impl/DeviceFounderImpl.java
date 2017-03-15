package com.alperez.bt_microphone.bluetoorh.management.impl;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.bluetoorh.management.DeviceDiscovery;
import com.alperez.bt_microphone.bluetoorh.management.DeviceFounder;
import com.alperez.bt_microphone.bluetoorh.management.OnDeviceFoundListener;
import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.storage.DatabaseAdapter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public class DeviceFounderImpl implements DeviceFounder {
    public static final String TAG = "DeviceFounder";


    private final Object lockInstance = new Object();

    private Context context;
    private DeviceDiscovery deviceDiscovery;
    private OnDeviceFoundListener devFoundListener;


    private DatabaseAdapter dbAdapter;
    private ExecutorService sequantialExecuter;

    private volatile boolean isDiscoveryStated;
    private volatile boolean isThisStarted;
    private volatile boolean released;

    private Set<String> handledMACs = new HashSet<>();

    private volatile boolean uuidReceiverRegisterred;

    public DeviceFounderImpl(Context context, @NonNull DeviceDiscovery deviceDiscovery) {
        this.context = context;
        this.deviceDiscovery = deviceDiscovery;
        deviceDiscovery.setOnDeviceDiscoveryListener(this);
    }

    @Override
    public void setOnDeviceFoundListener(OnDeviceFoundListener l) {
        devFoundListener = l;
    }

    @Override
    public boolean isStarted() {
        return isThisStarted;
    }

    @Override
    public void start() {
        if (released) throw new IllegalStateException("Already released");
        synchronized (lockInstance) {


            if (sequantialExecuter == null) {
                sequantialExecuter = Executors.newSingleThreadExecutor();
                Log.d(TAG, "Create executor");
                sequantialExecuter.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(14000);
                            Log.d(TAG, "End initial delay task");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            }
            if (dbAdapter == null) {
                dbAdapter = new DatabaseAdapter();
            }
            if (!uuidReceiverRegisterred) {
                context.registerReceiver(mUuidReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));
                uuidReceiverRegisterred = true;
            }
            isThisStarted = true;
            startDiscovery();

            Log.d(TAG, "|||||||||||||||||||   DeviceFounder start  |||||||||||||||||||||||||");
        }
    }

    @Override
    public void stop() {
        if (released) throw new IllegalStateException("Already released");
        synchronized (lockInstance) {
            isThisStarted = false;
            stopDiscovery();
            handledMACs.clear();

            if (sequantialExecuter != null) {
                sequantialExecuter.shutdownNow();
                sequantialExecuter = null;
            }

            Log.d(TAG, "|||||||||||||||||||   DeviceFounder STOP  |||||||||||||||||||||||||");
        }
    }

    /**
     * No operation allowed after this call;
     */
    @Override
    public void release() {
        if (!released) {
            synchronized (lockInstance) {
                stop();
                deviceDiscovery.release();

                if (dbAdapter != null) {
                    dbAdapter.close();
                    dbAdapter = null;
                }
                if (uuidReceiverRegisterred) {
                    context.unregisterReceiver(mUuidReceiver);
                }
                released = true;
                Log.d(TAG, "<<<<<<<<<<<<<<<<  Device founder released  >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            }
        }
    }

    private void startDiscovery() {
        synchronized (lockInstance) {
            if (!isDiscoveryStated) {
                isDiscoveryStated = true;
                deviceDiscovery.resumeDiscovery();
            }
        }
    }

    private void stopDiscovery() {
        synchronized (lockInstance) {
            if (isDiscoveryStated) {
                isDiscoveryStated = false;
                deviceDiscovery.stopDiscovery();
            }
        }
    }

    private boolean waitForDiscoveryStopTimeout(int timeoutMillis) {
        long tStart = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(130);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (!deviceDiscovery.isDiscovering()) {
                return true;
            }
        } while ((System.currentTimeMillis() - tStart) < timeoutMillis && !Thread.interrupted());
        return false;
    }

    @Override
    public void onDeviceDiscovered(final DiscoveredBluetoothDevice device) {
        if (handledMACs.contains(device.getDevice().getAddress())) {
            Log.d(TAG, "~~~~~~>>>  A device is discovered but already handled - "+device.getDevice().getAddress());
            return;
        } else {
            Log.d(TAG, "======>>>  onDeviceDiscovered() - "+device.getDevice().getAddress()+"   Enqueue new test!");
            handledMACs.add(device.getDevice().getAddress());
            sequantialExecuter.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        doJobOnDiscoveredDevice(device);
                    } catch (Exception e) {
                        Log.e(TAG, "~~~~~~~~~~~~~  Error processing device: "+device.getDevice().getAddress());
                        e.printStackTrace();
                    }
                }
            });
        }




    }

    /**
     * This is the entry points of handling newly discovered devices.
     * This method must be called from the background thread.
     * A working result is reported using the Result Handler
     * @param device
     */
    private void doJobOnDiscoveredDevice(DiscoveredBluetoothDevice device) throws Exception {
        long deviceId = BlacklistedBtDevice.create(device.getDevice().getAddress(), null, new Date(), null).id();
        Log.d(TAG, String.format("===========  Start handling discovered device. Address - %s, Name - %s, RSSI=%d =================",device.getDevice().getAddress(), device.getDevice().getName(), device.getRSSI()));

        BlacklistedBtDevice blackDev = dbAdapter.selectBlacklistedDeviceById(deviceId);
        if (blackDev != null) {
            Log.e(TAG, "This device is in the Blacklist");
            resultHandlerToUi.sendMessage(resultHandlerToUi.obtainMessage(0, blackDev));
            return;
        }

        ValidBtDevice validDev = dbAdapter.selectValidDeviceById(deviceId);
        if (validDev != null) {
            Log.e(TAG, "This device is already known");
            resultHandlerToUi.sendMessage(resultHandlerToUi.obtainMessage(0, validDev));
            return;
        }

        handleUnknownDevice(device);
    }

    private void handleUnknownDevice(DiscoveredBluetoothDevice device) throws Exception {
        Log.d(TAG, "Start handling unknown device: "+device.getMacAddress());

        try {
            //---  Stop discovery process for performance reasons ---
            stopDiscovery();
            if (!waitForDiscoveryStopTimeout(5000)) {
                Log.e(TAG, "~~~~~~~~~~~~~~~~~  Timeout waiting for stop discovery!!!");
                return;
            } else {
                Log.d(TAG, "Discovery stopped");
            }

            uuidCondensor.reset();
            uuidCondensor.setTrackingDevice(device.getDevice());
            if (!device.getDevice().fetchUuidsWithSdp()) {
                Log.e(TAG, "~~~~~~~~~~~~~~~~~  Error start SDP precedure ~~~~~~~~~~~~~~~");
                return;
            }


            boolean timeoutCondition = false;
            do {
                delay(80);

                if (uuidCondensor.hasAllUUIDs(GlobalConstants.ALL_SERVICE_UUIDS)) {
                    Log.d(TAG, "--------===  Result OK! All UUIDs were found  ===----------");
                    resultHandlerToUi.sendMessage(resultHandlerToUi.obtainMessage(0, device));
                    return;
                }

                synchronized (uuidCondensor) {
                    if ((uuidCondensor.getNumUUIDs() > 0) && (uuidCondensor.timeAfterLastUUID() > GlobalConstants.MAX_TIME_AFTER_LAST_UUID_DISCOVERED)) {
                        timeoutCondition = true;
                        Log.d(TAG, ">>> Timeout after last UUID!");
                    } else if (uuidCondensor.timeFromStartSDP() > GlobalConstants.MAX_TIME_FOR_SDP) {
                        timeoutCondition = true;
                        Log.d(TAG, ">>> Timeout of total time SDP");
                    }
                }
            } while(!timeoutCondition && !Thread.interrupted());


            //--- Save device to blacklist ---
            if (uuidCondensor.getNumUUIDs() > 0 || uuidCondensor.wasUUIDAnsverNull()) {
                long res = dbAdapter.insertDeviceToBlacklist(BlacklistedBtDevice.create(device.getDevice().getAddress(), device.getDevice().getName(), new Date(), null));
                Log.d(TAG, String.format("~~~~> Device was inserted to the blacklist: %s, ins res = %d ", device.getDevice().getAddress(), res));
            }

        } finally {
            //--- Start discovery process back ---
            synchronized (lockInstance) {
                if (isThisStarted) {
                    startDiscovery();
                }
            }
            delay(300);
        }
    }







    /**********************************************************************************************/
    /**********************************************************************************************/
    private final SdpResultCondensor uuidCondensor = new SdpResultCondensor();

    private static class SdpResultCondensor {
        private BluetoothDevice trackingDevice;
        private long timeStartTracking;
        private long timeLastUUID;
        private boolean uuidResponseNull;

        private Set<UUID> uuids = new HashSet<>();

        public synchronized void reset() {
                trackingDevice = null;
                timeLastUUID = 0;
                timeStartTracking = 0;
                uuids.clear();
            }

        public synchronized void setTrackingDevice(BluetoothDevice trackingDevice) {
            if (this.trackingDevice != null) throw new IllegalStateException("Already tracking another device");
            this.trackingDevice = trackingDevice;
            timeStartTracking = System.currentTimeMillis();
        }

        public synchronized boolean addUUID(BluetoothDevice device, UUID uuid) {
            if (trackingDevice == null) throw new IllegalStateException("No tracking device");

            if (device == null || uuid == null) {
                return false;
            } else if (!trackingDevice.getAddress().equals(device.getAddress())) {
                return false;
            }
            uuids.add(uuid);
            timeLastUUID = System.currentTimeMillis();
            return true;
        }

        public synchronized void nullUuidResponse(BluetoothDevice device) {
            if (trackingDevice == null) throw new IllegalStateException("No tracking device");
            if (device != null && trackingDevice.getAddress().equals(device.getAddress())) {
                //--- Force timeout to finish device processing ---
                timeStartTracking = System.currentTimeMillis() - GlobalConstants.MAX_TIME_FOR_SDP + 100;
                uuidResponseNull = true;
            }
        }

        public synchronized boolean hasAllUUIDs(UUID[] uuids) {
            for (UUID uuid : uuids) {
                if (!this.uuids.contains(uuid)) {
                    return false;
                }
            }
            return true;
        }

        public synchronized int timeFromStartSDP() {
            return (int)(System.currentTimeMillis() - timeStartTracking);
        }

        public synchronized int timeAfterLastUUID() {
            return (int)(System.currentTimeMillis() - timeLastUUID);
        }

        public synchronized int getNumUUIDs() {
            return uuids.size();
        }

        public synchronized boolean wasUUIDAnsverNull() {
            return uuidResponseNull;
        }

        public boolean isReady() {
            return trackingDevice != null;
        }
    }



    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



    private final BroadcastReceiver mUuidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice deviceExtra = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
            if (uuidExtra != null) {
                for (Parcelable pUuid : uuidExtra) {
                    UUID u = ((ParcelUuid) pUuid).getUuid();


                    if (uuidCondensor.isReady()) {

                        if (uuidCondensor.addUUID(deviceExtra, u)) {
                            Log.d(TAG, String.format("---->  The UUID found - %s for device %s - ADDED", u, deviceExtra.getAddress()));
                        } else {
                            Log.d(TAG, String.format("~~~~>  The UUID found - %s for device %s - NOT ADDED, WRONG DEVICE", u, deviceExtra.getAddress()));
                        }

                    } else {
                        Log.e(TAG, String.format("~~~~>  The UUID found - %s for device %s  BUT the FOUNDER is STOPPED!!!", u, deviceExtra.getAddress()));
                    }
                }
            } else {

                uuidCondensor.nullUuidResponse(deviceExtra);


                Log.e(TAG, String.format("~~~~>  ACTION_UUID for device - %s  UUID array is NULL!!!", deviceExtra.getAddress()));
            }
        }
    };






    /**
     * The handler which posts out result of discovery to a client in the UI thread.
     */
    private Handler resultHandlerToUi = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (resultHandlerToUi != null) {
                if ((msg.obj != null) && (msg.obj instanceof ValidBtDevice)) {
                    devFoundListener.onValidDeviceFound((ValidBtDevice) msg.obj);
                } else if ((msg.obj != null) && (msg.obj instanceof BlacklistedBtDevice)) {
                    devFoundListener.onBlacklistedDeviceFound((BlacklistedBtDevice) msg.obj);
                } else if ((msg.obj != null) && (msg.obj instanceof DiscoveredBluetoothDevice)) {


                    devFoundListener.onNewRawDeviceFound((DiscoveredBluetoothDevice) msg.obj);
                }
            }
        }
    };
}
