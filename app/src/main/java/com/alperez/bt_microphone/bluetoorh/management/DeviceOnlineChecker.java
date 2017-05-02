package com.alperez.bt_microphone.bluetoorh.management;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Random;
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


    private Handler resultHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            onCheckResultListener.onCheckResult((ValidDeviceDbModel) msg.obj, (msg.what > 0));
        }
    };



    private class DeviceCheckTaskRunnable implements Runnable {
        ValidDeviceDbModel device;
        private int MIN_CHECK_TIME = 850;

        public DeviceCheckTaskRunnable(ValidDeviceDbModel device) {
            this.device = device;
        }



        @Override
        public void run() {


            long tStart = System.currentTimeMillis();


            try {
                checkDevice();
                onDeviceChecked(tStart, true);
            } catch (Exception e) {
                e.printStackTrace();
                onDeviceChecked(tStart, false);
            } finally {
                BtUtils.silentlyCloseCloseable(dis);
                BtUtils.silentlyCloseCloseable(dos);
                BtUtils.silentCloseBtSocket(btSocket);
            }





        }


        private BluetoothSocket btSocket;
        private InputStream dis;
        private OutputStream dos;

        private void checkDevice() throws Exception {

            btSocket = device.bluetoothDevice().createRfcommSocketToServiceRecord(GlobalConstants.UUID_SERVICE_1_1);
            btSocket.connect();
            dis = btSocket.getInputStream();
            dos = btSocket.getOutputStream();

            int commandId = sendVersionCommand(dos);

            DiscoveredDeviceVerifier.VersionResponse resp = readResponse(dis);

            if (resp.id != commandId) {
                throw new IOException("Wrong ID in response");
            } else if (!resp.serialNumber.equals(device.serialNumber())) {
                throw new IOException(String.format("Wrong device serial number. Required - %s, got - %s", device.serialNumber(), resp.serialNumber));
            }

        }

        private int sendVersionCommand(OutputStream oStream) throws JSONException, IOException {
            JSONObject jCommand = new JSONObject();
            jCommand.put("id", new Random().nextInt(999999)+1000);
            jCommand.put("command", "version");

            oStream.write((jCommand.toString()+"\r\n").getBytes());

            return jCommand.getInt("id");
        }




        private DiscoveredDeviceVerifier.VersionResponse readResponse(InputStream iStream) throws IOException, JSONException, ParseException, InterruptedException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
            int symb;
            byte b_1=0, b_2;
            long tStart = System.currentTimeMillis();
            do {
                Thread.sleep(50);
                int aval = iStream.available();
                for (int i=0; i<aval; i++) {
                    symb = iStream.read();
                    if (symb < 0) throw new IOException("Unexpected end of stream");
                    b_2 = b_1;
                    b_1 = (byte)symb;
                    bos.write(symb);
                    if ((b_2 == 0x0D && b_1 == 0x0A)) {
                        byte[] data = bos.toByteArray();
                        String textData = new String(data, 0, data.length-2);
                        bos.reset();
                        return DiscoveredDeviceVerifier.VersionResponse.fromJson(textData);
                    }
                }
            } while ((System.currentTimeMillis() - tStart) < 1000 && !released);
            throw new IOException("Timeout or released");
        }



        private void onDeviceChecked(long tStart, boolean online) {
            int dt = (int)(System.currentTimeMillis() - tStart);
            if (dt < MIN_CHECK_TIME) {
                try {
                    Thread.sleep(MIN_CHECK_TIME - dt);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }

            if (!Thread.interrupted()) {
                resultHandler.obtainMessage(online ? 1 : 0, device).sendToTarget();
            }
        }


    }
}
