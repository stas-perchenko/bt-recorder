package com.alperez.bt_microphone.bluetoorh.management;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.utils.Callback;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public class DiscoveredDeviceVerifier {
    private final DiscoveredBluetoothDevice device;
    private Callback<ValidBtDevice> resultCallback;


    private volatile boolean released;
    private final Thread worker;
    private final Handler resultHandler;


    public static DiscoveredDeviceVerifier createForDevice(DiscoveredBluetoothDevice device) {
        return new DiscoveredDeviceVerifier(device);
    }


    private DiscoveredDeviceVerifier(DiscoveredBluetoothDevice device) {
        this.device = device;

        resultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (resultCallback != null) {
                    if (msg.obj instanceof ValidBtDevice) {
                        resultCallback.onComplete((ValidBtDevice) msg.obj);
                    } else if (msg.obj instanceof Throwable) {
                        resultCallback.onError((Throwable) msg.obj);
                    }
                }
            }
        };

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
        Thread.sleep(600);


        //---  Emulatye check ---
        int n = 0;
        while (!released && n++ < 150) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e){}
        }

        if (released) {
            throw new InterruptedException("Worker released early!");
        } else {
            throw new Exception("Error device verification");
        }
    }
}
