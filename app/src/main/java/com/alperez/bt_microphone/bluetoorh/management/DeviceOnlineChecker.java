package com.alperez.bt_microphone.bluetoorh.management;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.model.ValidDeviceDbModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by stanislav.perchenko on 4/28/2017.
 */

public class DeviceOnlineChecker {

    public interface OnCheckResultListener {
        void onCheckResult(ValidDeviceDbModel device, boolean online);
    }


    private OnCheckResultListener onCheckResultListener;

    ExecutorService exec;
    private volatile boolean released;

    public DeviceOnlineChecker(@NonNull OnCheckResultListener onCheckResultListener) {
        this.onCheckResultListener = onCheckResultListener;

        exec = Executors.newSingleThreadExecutor();



    }

    public synchronized void checkDeviceAsync(ValidDeviceDbModel device) {
        if (released) throw new IllegalStateException("Already released");
        exec.submit(new DeviceCheckTaskRunnable(device));
    }

    public synchronized void release() {
        exec.shutdownNow();
        exec = null;
        released = true;
    }

    private void onDeviceChecked(ValidDeviceDbModel device, boolean online) {
        resultHandler.obtainMessage(online ? 1 : 0, device).sendToTarget();
    }

    private Handler resultHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            onCheckResultListener.onCheckResult((ValidDeviceDbModel) msg.obj, (msg.what > 0));
        }
    };



    private class DeviceCheckTaskRunnable implements Runnable {
        ValidDeviceDbModel device;

        public DeviceCheckTaskRunnable(ValidDeviceDbModel device) {
            this.device = device;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1800);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

            if (!Thread.interrupted()) {
                onDeviceChecked(device, true);
            }
        }
    }
}
