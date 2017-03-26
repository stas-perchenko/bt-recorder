package com.alperez.bt_microphone.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.connector.OnConnectionStatusListener;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiverImpl;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundPlayer;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundPlayerImpl;
import com.alperez.bt_microphone.bluetoorh.connector.sound.OnPlayerPerformanceListener;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
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


    private ValidDeviceDbModel mDevice;

    private DataTransferArrayAdapter logAdapter;

    private BtDataTransceiver deviceTransceiverCommand;
    //private BtSoundReceiver deviceReceiverSound;
    private BtSoundPlayer mPlayer;

    private int commandCounter;


    private View vCommandConnStatus;
    private TextView vTxtCommandConnTry;

    private TextView vTxtNumSoundBytesReceived;
    private TextView vTxtNumSoundBytesPlayed;
    private View vSoundConnStatus;
    private TextView vTxtSoundNumReconnect;
    private TextView vTxtSoundNumTryes;

    private int soundBytesReceivedCounter;
    private int soundBytesPlayedCounter;


    private ToggleButton vBtnPlay, vBtnStop;

    private boolean userRequestedPlay = true;


    private AudioTrack createAudioTrack() {

        int minBufSizeBytes = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);

        minBufSizeBytes = 1024;

        return new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT, 2*minBufSizeBytes, AudioTrack.MODE_STREAM);
    }

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
            deviceTransceiverCommand.setOnTransceiverStatusListener(new OnConnectionStatusListener() {
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


            mPlayer = new BtSoundPlayerImpl(device, GlobalConstants.UUID_SERVICE_2, createAudioTrack());
            mPlayer.setOnPlayerPerformanceListener(new OnPlayerPerformanceListener() {
                @Override
                public void onBytesReceived(int nBytes) {
                    soundBytesReceivedCounter += nBytes;
                    getWindow().getDecorView().post(() -> vTxtNumSoundBytesReceived.setText(""+soundBytesReceivedCounter));
                }

                @Override
                public void onBytesPlayed(int nBytes) {
                    soundBytesPlayedCounter += nBytes;
                    getWindow().getDecorView().post(() -> vTxtNumSoundBytesPlayed.setText(""+soundBytesPlayedCounter));
                }
            });
            mPlayer.setOnConnectionStatusListener(new OnConnectionStatusListener() {
                @Override
                public void onConnectionRestorted(int nTry) {
                    updateSoundConnectionState(true, nTry, 0);
                    soundBytesReceivedCounter = 0;
                    soundBytesPlayedCounter = 0;
                    getWindow().getDecorView().post(() -> {
                        vTxtNumSoundBytesReceived.setText("");
                        vTxtNumSoundBytesPlayed.setText("");
                    });
                }

                @Override
                public void onConnectionAttemptFailed(int nTry) {
                    updateSoundConnectionState(false, -1, nTry);
                }

                @Override
                public void onConnectionBroken(String nameThreadCauseFailure, Throwable reason) {
                    updateSoundConnectionState(false, -1, -1);
                }
            });

            /*getWindow().getDecorView().postDelayed(() -> {
                deviceReceiverSound = new BtSoundReceiverImpl(device, GlobalConstants.UUID_SERVICE_2, (data, offset, nBytes) -> {
                    soundBytesCounter += nBytes;
                    getWindow().getDecorView().post(() -> vTxtNumSoundBytesReceived.setText(""+soundBytesCounter));
                });
                deviceReceiverSound.setOnTransceiverStatusListener(new OnConnectionStatusListener() {
                    @Override
                    public void onConnectionRestorted(int nTry) {
                        updateSoundConnectionState(true, nTry);
                        soundBytesCounter = 0;
                        getWindow().getDecorView().post(() -> vTxtNumSoundBytesReceived.setText(""));
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
            }, 1500);*/


        } catch (BluetoothNotSupportedException e) {
            if(deviceTransceiverCommand != null) deviceTransceiverCommand.release();
            //if (deviceReceiverSound != null) deviceReceiverSound.release();
            if(mPlayer != null) mPlayer.release();
            Toast.makeText(this, "BluetoothNotSupportedException", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        vCommandConnStatus = findViewById(R.id.command_conn_status);
        vTxtCommandConnTry = (TextView) findViewById(R.id.command_conn_try);

        vTxtNumSoundBytesReceived = (TextView) findViewById(R.id.sound_num_bytes_received);
        vTxtNumSoundBytesPlayed = (TextView)  findViewById(R.id.sound_num_bytes_played);
        vSoundConnStatus = findViewById(R.id.sound_conn_status);
        vTxtSoundNumReconnect = (TextView) findViewById(R.id.sound_num_reconnects);
        vTxtSoundNumTryes = (TextView) findViewById(R.id.sound_failed_tries);


        ((TextView) findViewById(R.id.device_given_name)).setText(mDevice.userDefinedName());
        ((TextView) findViewById(R.id.device_system_name)).setText(mDevice.deviceName());
        ((TextView) findViewById(R.id.device_mac_address)).setText(mDevice.macAddress());

        ((ListView) findViewById(R.id.log_list)).setAdapter(logAdapter = new DataTransferArrayAdapter(this, new Stack<>()));

        findViewById(R.id.btn_command_version).setOnClickListener(v -> sendSingleCommand("verion"));
        findViewById(R.id.btn_command_set_time).setOnClickListener(v -> sendCommandSetTime(new Date()));
        findViewById(R.id.btn_command_status).setOnClickListener(v -> sendSingleCommand("status"));

        findViewById(R.id.btn_command_record).setOnClickListener(v -> sendSingleCommand("record"));
        vBtnStop = (ToggleButton) findViewById(R.id.btn_command_stop);
        vBtnPlay = (ToggleButton) findViewById(R.id.btn_command_play);

        vBtnPlay.setOnClickListener(v -> {
            sendSingleCommand("play");
            if (!userRequestedPlay) {
                userRequestedPlay = true;
                mPlayer.play();
            }
            updatePlayStopButtonsState();
        });

        vBtnStop.setOnClickListener(v -> {
            sendSingleCommand("stop");
            if (userRequestedPlay) {
                userRequestedPlay = false;
                mPlayer.pause();
            }
            updatePlayStopButtonsState();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userRequestedPlay) {
            mPlayer.play();
        }
        updatePlayStopButtonsState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(deviceTransceiverCommand != null) deviceTransceiverCommand.release();
        //if (deviceReceiverSound != null) deviceReceiverSound.release();
        if(mPlayer != null) mPlayer.release();
    }

    private ValidDeviceDbModel getDeviceArgument() {
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


    private void updatePlayStopButtonsState() {
        vBtnStop.setChecked(!userRequestedPlay);
        vBtnPlay.setChecked(userRequestedPlay);
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


            AudioTrack audioTrack = null;
            audioTrack.play();
            audioTrack.pause();
            audioTrack.stop();
            audioTrack.release();
            audioTrack.write(new byte[5], 0, 0);


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

    private void updateSoundConnectionState(final boolean connected, final int nReconnects, final int nFailedTries) {
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (nReconnects >= 0) {
                    vTxtSoundNumReconnect.setText(""+nReconnects);
                }
                if (nFailedTries >= 0) {
                    vTxtSoundNumTryes.setText(""+nFailedTries);
                }
                vSoundConnStatus.getBackground().setLevel(connected ? 1 : 0);
            }
        });
    }



}
