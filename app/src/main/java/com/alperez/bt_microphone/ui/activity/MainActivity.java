package com.alperez.bt_microphone.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.connector.OnTransceiverStatusListener;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiverImpl;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundReceiver;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundReceiverImpl;
import com.alperez.bt_microphone.bluetoorh.connector.sound.OnSoundDataReceivedListener;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.Layout;
import com.alperez.bt_microphone.ui.adapter.DataTransferArrayAdapter;
import com.alperez.bt_microphone.ui.viewmodel.DataTransferViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Stack;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */
@Layout(value = R.layout.activity_main)
public class MainActivity extends BaseActivity {
    public static final String ARG_VALID_DEVICE_ID = "device_id";


    private ValidBtDevice mDevice;

    private DataTransferArrayAdapter logAdapter;

    private BtDataTransceiver deviceTransceiverCommand;
    private BtSoundReceiver deviceReceiverSound;

    private int commandCounter;


    private View vCommandConnStatus;
    private TextView vTxtCommandConnTry;

    private TextView vTxtNumSoundBytes;
    private TextView vTxtSoundDataRate;
    private View vSoundConnStatus;
    private TextView vTxtSoundConnTry;

    private int soundBytesCounter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();

        mDevice = getDeviceArgument();
        if (mDevice == null) {
            Toast.makeText(this, "No device provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            BluetoothDevice device = BtUtils.getBtAdapter(this).getRemoteDevice(mDevice.macAddress());
            mDevice.withBluetoothDevice(device);
            deviceTransceiverCommand = new BtDataTransceiverImpl(device, GlobalConstants.UUID_SERVICE_1, data -> getWindow().getDecorView().post(() -> logAdapter.add(DataTransferViewModel.createReceivedItem(new Date(), data))));
            deviceTransceiverCommand.setOnTransceiverStatusListener(new OnTransceiverStatusListener() {
                @Override
                public void onConnectionRestorted(int nTry) {
                    updateCommandConnectionState(true, nTry);
                }

                @Override
                public void onConnectionAttemptFailed(int nTry) {
                    updateCommandConnectionState(false, nTry);
                }

                @Override
                public void onConnectionBroken(String nameThreadCauseFailure, Throwable reason) {
                    updateCommandConnectionState(false, -1);
                }
            });

            getWindow().getDecorView().postDelayed(() -> {
                deviceReceiverSound = new BtSoundReceiverImpl(device, GlobalConstants.UUID_SERVICE_2, new OnSoundDataReceivedListener() {
                    @Override
                    public void onDataReceiver(byte[] buffer, int offcet, int nBytes) {
                        soundBytesCounter += nBytes;
                        getWindow().getDecorView().post(() -> vTxtNumSoundBytes.setText(""+soundBytesCounter));
                    }

                    @Override
                    public void onDataRateMeasured(float bps) {
                        getWindow().getDecorView().post(() -> vTxtSoundDataRate.setText(String.format("%.1f bps", bps)));
                    }
                });
                deviceReceiverSound.setOnTransceiverStatusListener(new OnTransceiverStatusListener() {
                    @Override
                    public void onConnectionRestorted(int nTry) {
                        updateSoundConnectionState(true, nTry);
                        soundBytesCounter = 0;
                        getWindow().getDecorView().post(() -> vTxtNumSoundBytes.setText(""));
                    }

                    @Override
                    public void onConnectionAttemptFailed(int nTry) {
                        updateSoundConnectionState(false, nTry);
                    }

                    @Override
                    public void onConnectionBroken(String nameThreadCauseFailure, Throwable reason) {
                        updateSoundConnectionState(false, -1);
                    }
                });
            }, 1500);


        } catch (BluetoothNotSupportedException e) {
            if (deviceTransceiverCommand != null) deviceTransceiverCommand.release();
            if (deviceReceiverSound != null) deviceReceiverSound.release();
            Toast.makeText(this, "BluetoothNotSupportedException", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        vCommandConnStatus = findViewById(R.id.command_conn_status);
        vTxtCommandConnTry = (TextView) findViewById(R.id.command_conn_try);

        vTxtNumSoundBytes = (TextView) findViewById(R.id.sound_num_bytes);
        vTxtSoundDataRate = (TextView) findViewById(R.id.sound_data_rate);
        vSoundConnStatus = findViewById(R.id.sound_conn_status);
        vTxtSoundConnTry = (TextView) findViewById(R.id.sound_conn_try);


        ((TextView) findViewById(R.id.device_given_name)).setText(mDevice.userDefinedName());
        ((TextView) findViewById(R.id.device_system_name)).setText(mDevice.deviceName());
        ((TextView) findViewById(R.id.device_mac_address)).setText(mDevice.macAddress());

        ((ListView) findViewById(R.id.log_list)).setAdapter(logAdapter = new DataTransferArrayAdapter(this, new Stack<>()));

        findViewById(R.id.btn_command_version).setOnClickListener(v -> sendSingleCommand("verion"));
        findViewById(R.id.btn_command_set_time).setOnClickListener(v -> sendCommandSetTime(new Date()));
        findViewById(R.id.btn_command_status).setOnClickListener(v -> sendSingleCommand("status"));

        findViewById(R.id.btn_command_record).setOnClickListener(v -> sendSingleCommand("record"));
        findViewById(R.id.btn_command_stop).setOnClickListener(v -> sendSingleCommand("stop"));
        findViewById(R.id.btn_command_play).setOnClickListener(v -> sendSingleCommand("play"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceTransceiverCommand != null) deviceTransceiverCommand.release();
        if (deviceReceiverSound != null) deviceReceiverSound.release();
    }

    private ValidBtDevice getDeviceArgument() {
        long id = getIntent().getLongExtra(ARG_VALID_DEVICE_ID, -1);
        if (id <= 0) {
            return null;
        }
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            return db.selectValidDeviceById(id);
        } finally {
            db.close();
        }
    }



    private void sendCommandSetTime(Date d) {
        try {
            JSONObject jTime = new JSONObject();
            jTime.put("id", ++commandCounter);
            jTime.put("command", "time");
            jTime.put("time", String.format("%1$tH:%1$tM.%1$tS", d));
            jTime.put("date", String.format("%1$tm/%1$td.%1$ty", d));
            String command = jTime.toString();

            deviceTransceiverCommand.sendDataNonBlocked(command);

            logDataSentToUi(command);
        } catch (JSONException ignore) {}
    }


    private void sendSingleCommand(String comType) {
        try {
            JSONObject jCommand = new JSONObject();
            jCommand.put("id", ++commandCounter);
            jCommand.put("command", comType);

            String command = jCommand.toString();
            deviceTransceiverCommand.sendDataNonBlocked(command);

            logDataSentToUi(command);
        } catch (JSONException ignore) {}
    }





    private void logDataSentToUi(String data) {
        DataTransferViewModel item = DataTransferViewModel.createSentItem(new Date(), data);
        logAdapter.add(item);
    }





    private void updateCommandConnectionState(final boolean connected, final int nConnTry) {
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (nConnTry >= 0) {
                    vTxtCommandConnTry.setText(""+nConnTry);
                }
                vCommandConnStatus.getBackground().setLevel(connected ? 1 : 0);
            }
        });
    }

    private void updateSoundConnectionState(final boolean connected, final int nConnTry) {
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (nConnTry >= 0) {
                    vTxtSoundConnTry.setText(""+nConnTry);
                }
                vSoundConnStatus.getBackground().setLevel(connected ? 1 : 0);
            }
        });
    }



}
