package com.alperez.bt_microphone.bluetoorh.management;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.rest.RestUtils;
import com.alperez.bt_microphone.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public class DiscoveredDeviceVerifier {

    public static final String TAG = "DeviceVerifier";


    private final DiscoveredBluetoothDevice device;
    private Callback<ValidDeviceDbModel> resultCallback;





    private volatile boolean released;
    private final Thread worker;



    public static DiscoveredDeviceVerifier createForDevice(DiscoveredBluetoothDevice device) {
        return new DiscoveredDeviceVerifier(device);
    }


    private DiscoveredDeviceVerifier(DiscoveredBluetoothDevice device) {
        this.device = device;

        worker = new Thread(getClass().getSimpleName()) {
            @Override
            public void run() {
                try {
                    ValidDeviceDbModel result = doTheJobBackground(device.getDevice());
                    resultHandler.obtainMessage(0, result).sendToTarget();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Verifier thread was interrupted - "+e.getMessage());
                    // Do nothing in this case
                } catch (Exception e) {
                    Log.e(TAG, "Verifier has finished with error - "+e.getMessage());
                    e.printStackTrace();
                    resultHandler.obtainMessage(0, e).sendToTarget();
                } finally {
                    BtUtils.silentlyCloseCloseable(btSocket);
                }
            }
        };
    }



    public DiscoveredDeviceVerifier execute(Callback<ValidDeviceDbModel> callback) {
        resultCallback = callback;
        worker.start();
        return this;
    }

    public void release() {
        released = true;
        worker.interrupt();


        BtUtils.silentlyCloseCloseable(btSocket);
    }




































    BluetoothSocket btSocket;


    private final Object rcvDataLock = new Object();
    private VersionResponse receivedDataModel;


    private ValidDeviceDbModel doTheJobBackground(BluetoothDevice btDev) throws Exception {


        btSocket = btDev.createRfcommSocketToServiceRecord(GlobalConstants.UUID_SERVICE_1_1);
        btSocket.connect();


        new RcvWorkerThread(btSocket.getInputStream()).start();



        int commandId = sendVersionCommand(btSocket.getOutputStream());


        VersionResponse localResponse;
        synchronized (rcvDataLock) {
            rcvDataLock.wait(5000);
            localResponse = receivedDataModel;
        }
        if (localResponse == null) {
            throw new IOException("The device did not respond in time");
        } else if (localResponse.id != commandId) {
            throw new IOException("The device respond with wrong data ID");
        }

        return ValidDeviceDbModel.builder()
                .setMacAddress(btDev.getAddress())
                .setDeviceName(btDev.getName())
                .setSerialNumber(localResponse.serialNumber)
                .setHardwareVersion(localResponse.hVer)
                .setFirmwareVersion(localResponse.sVer)
                .setReleaseDate(localResponse.manufDate)
                .setTimeDiscovered(new Date())
                .setTimeLastConnected(new Date())
                .setBluetoothDevice(btDev)
                .build();
    }

    private int sendVersionCommand(OutputStream oStream) throws JSONException, IOException {
        JSONObject jCommand = new JSONObject();
        jCommand.put("id", new Random().nextInt(999999)+1000);
        jCommand.put("command", "version");

        oStream.write((jCommand.toString()+"\r\n").getBytes());

        return jCommand.getInt("id");
    }








    /**********************************************************************************************/
    private final Handler resultHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (resultCallback != null) {
                if (msg.obj instanceof ValidDeviceDbModel) {
                    resultCallback.onComplete((ValidDeviceDbModel) msg.obj);
                } else if (msg.obj instanceof Throwable) {
                    resultCallback.onError((Throwable) msg.obj);
                }
            }
        }
    };




















    /*****************************  Receiver part  ************************************************/
    private class RcvWorkerThread extends Thread {

        InputStream iStream;

        public RcvWorkerThread(InputStream iStream) {
            super("verification-receiver-thread");
            this.iStream = iStream;
        }

        @Override
        public void run() {
            while(!released) {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {


                    ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
                    int symb;
                    byte b_1=0, b_2=0;
                    while((symb = iStream.read()) >= 0 && !released) {
                        b_2 = b_1;
                        b_1 = (byte)symb;
                        bos.write(symb);

                        if ((b_2 == 0x0D && b_1 == 0x0A)) {
                            byte[] data = bos.toByteArray();
                            String textData = new String(data, 0, data.length-2);
                            bos.reset();
                            synchronized (rcvDataLock) {
                                receivedDataModel = VersionResponse.fromJson(textData);
                                rcvDataLock.notifyAll();
                            }
                            return;
                        }
                    }


                } catch (Exception ignore){

                } finally {
                    BtUtils.silentlyCloseCloseable(iStream);
                }
            }
            BtUtils.silentlyCloseCloseable(iStream);
        } //run()
    }

    private static class VersionResponse {
        public int id;
        public String serialNumber;
        public int hVer, sVer;
        public Date manufDate;

        public static VersionResponse fromJson(String json) throws JSONException, ParseException {
            JSONObject jResp = new JSONObject(json);

            VersionResponse model = new VersionResponse();
            model.id = jResp.getInt("id");
            if (!jResp.getString("answer").equalsIgnoreCase("ok")) {
                throw new JSONException("Device respond error");
            }
            JSONObject jPayload = jResp.getJSONObject("version");
            model.serialNumber = jPayload.getString("serial");
            model.hVer = jPayload.getInt("hardware");
            model.sVer = jPayload.getInt("software");
            model.manufDate = RestUtils.parseRemoteDateTime(jPayload.getString("time"));
            return model;
        }

        private VersionResponse(){}
    }


}
