package com.alperez.bt_microphone.bluetoorh.management;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.utils.Callback;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public class DiscoveredDeviceVerifier {


    private final DiscoveredBluetoothDevice device;
    private Callback<ValidBtDevice> resultCallback;
    private OnStageListener testListener;





    private volatile boolean released;
    private final Thread worker;



    public static DiscoveredDeviceVerifier createForDevice(DiscoveredBluetoothDevice device, @NonNull OnStageListener testListener) {
        return new DiscoveredDeviceVerifier(device, testListener);
    }


    private DiscoveredDeviceVerifier(DiscoveredBluetoothDevice device, OnStageListener testListener) {
        this.device = device;
        this.testListener = testListener;



        worker = new Thread(getClass().getSimpleName()) {
            @Override
            public void run() {
                try {
                    ValidBtDevice result = doTheJobBackground();
                    resultHandler.obtainMessage(0, result).sendToTarget();
                } catch (InterruptedException e) {
                    // Do nothing in this case
                } catch (Exception e) {
                    e.printStackTrace();
                    resultHandler.obtainMessage(0, e).sendToTarget();
                }
            }
        };
    }

    public DiscoveredDeviceVerifier withResultCallback(Callback<ValidBtDevice> callback) {
        resultCallback = callback;
        return this;
    }


    public DiscoveredDeviceVerifier start() {
        worker.start();
        return this;
    }

    public void release() {
        released = true;
        worker.interrupt();
    }



    private ValidBtDevice doTheJobBackground() throws Exception {
        //--- Initial delay ---
        Thread.sleep(350);

        //--- Stage 1 ---
        resultHandler.obtainMessage(MSG_STAGE_1_START).sendToTarget();
        Thread.sleep(450);
        resultHandler.obtainMessage(MSG_STAGE_1_COMPLETE).sendToTarget();

        //--- Stage 2 ---
        Thread.sleep(80);
        resultHandler.obtainMessage(MSG_STAGE_2_START).sendToTarget();
        Thread.sleep(3450);
        resultHandler.obtainMessage(MSG_STAGE_2_COMPLETE).sendToTarget();


        //--- Stage 3 ---
        Thread.sleep(80);
        resultHandler.obtainMessage(MSG_STAGE_3_START).sendToTarget();
        Thread.sleep(450);
        resultHandler.obtainMessage(MSG_STAGE_3_COMPLETE).sendToTarget();


        return ValidBtDevice.builder()
                .setMacAddress(device.getDevice().getAddress())
                .setDeviceName(device.getDevice().getName())
                .setSerialNumber("sdgjdf-kgjbdfg-dflg")
                .setHardwareVersion(2)
                .setFirmwareVersion(150)
                .setReleaseDate(new Date(System.currentTimeMillis() - 2547864235L))
                .setTimeDiscovered(new Date())
                .setTimeLastConnected(new Date())
                .setBluetoothDevice(device.getDevice())
                .build();
    }








    /**********************************************************************************************/
    private final Handler resultHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STAGE_1_START:
                    testListener.onStage1Start();
                    break;
                case MSG_STAGE_1_COMPLETE:
                    testListener.onStage1Complete();
                    break;
                case MSG_STAGE_2_START:
                    testListener.onStage2Start();
                    break;
                case MSG_STAGE_2_COMPLETE:
                    testListener.onStage2Complete();
                    break;
                case MSG_STAGE_3_START:
                    testListener.onStage3Start();
                    break;
                case MSG_STAGE_3_COMPLETE:
                    testListener.onStage3Complete();
                    break;
                default:
                    if (resultCallback != null) {
                        if (msg.obj instanceof ValidBtDevice) {
                            resultCallback.onComplete((ValidBtDevice) msg.obj);
                        } else if (msg.obj instanceof Throwable) {
                            resultCallback.onError((Throwable) msg.obj);
                        }
                    }
            }
        }
    };


    private static final int MSG_STAGE_1_START = 100;
    private static final int MSG_STAGE_1_COMPLETE = 101;
    private static final int MSG_STAGE_2_START = 200;
    private static final int MSG_STAGE_2_COMPLETE = 201;
    private static final int MSG_STAGE_3_START = 300;
    private static final int MSG_STAGE_3_COMPLETE = 301;



    public interface OnStageListener {
        void onStage1Start();
        void onStage1Complete();
        void onStage2Start();
        void onStage2Complete();
        void onStage3Start();
        void onStage3Complete();
    }
}
