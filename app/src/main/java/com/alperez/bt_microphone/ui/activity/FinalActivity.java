package com.alperez.bt_microphone.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.core.DeviceState;
import com.alperez.bt_microphone.core.RemoteDevice;
import com.alperez.bt_microphone.databinding.ActivityFinalBinding;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceFile;
import com.alperez.bt_microphone.rest.response.commonmodels.DevicePosition;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceStatus;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.viewmodel.MainControlsViewModel;

/**
 * Created by stanislav.perchenko on 3/24/2017.
 */

public class FinalActivity extends AppCompatActivity implements RemoteDevice.OnCommandResultListener {
    public static final String ARG_VALID_DEVICE_ID = "device_id";

    private RemoteDevice remDevice;


    private ActivityFinalBinding vBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            ValidDeviceDbModel dbDevice = getDeviceArgument();
            BluetoothDevice btDevice = BtUtils.getBtAdapter(this).getRemoteDevice(dbDevice.macAddress());
            remDevice = new RemoteDevice(dbDevice.withBluetoothDevice(btDevice), 2000, this);
        } catch (BluetoothNotSupportedException e){
            finish();
            return;
        }


        vBinding = DataBindingUtil.setContentView(this, R.layout.activity_final);
        vBinding.setViewModel(new MainControlsViewModel());
        vBinding.inProgressCoverView.setOnClickListener((v) -> {/* For touch interception */});

        vBinding.actionSettings.setOnClickListener(v -> {});

        vBinding.setClickerRecord((v) -> onRecordClicked());
        vBinding.setClickerStop((v) -> onStopClicked());
        vBinding.setClickerPlayPause((v) -> onPlayPauseClicked());
        vBinding.setClickerGainUp((v) -> onGainUpClicked());
        vBinding.setClickerGainDown((v) -> onGainDownClicked());
        vBinding.setClickerPrevTrack((v) -> onPrevTrackClicked());
        vBinding.setClickerNextTrack((v) -> onNextTrackClicked());


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


    @Override
    protected void onStart() {
        super.onStart();
        getWindow().getDecorView().postDelayed(() -> remDevice.commandStatus(), 500);
    }

    private void onStopClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        vBinding.getViewModel().setDevState(DeviceState.STOPPING);
        remDevice.commandStop();
    }

    private void onRecordClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        vBinding.getViewModel().setDevState(DeviceState.START_RECORDING);
        remDevice.commandRecord();
    }

    private void onPlayPauseClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        if (vBinding.getViewModel().getDevState() == DeviceState.PLAYING) {
            vBinding.getViewModel().setDevState(DeviceState.PAUSING);
            remDevice.commandPause();
        } else {
            vBinding.getViewModel().setDevState(DeviceState.START_PLAYING);
            remDevice.commandPlay();
        }
    }

    private void onGainUpClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        remDevice.commandGainUp();
    }

    private void onGainDownClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        remDevice.commandGainDown();
    }

    private void onPrevTrackClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        remDevice.commandPrevFile();
    }

    private void onNextTrackClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        remDevice.commandNextFile();
    }









    @Override
    public void onStatusUpdate(DeviceStatus devStatus) {
        vBinding.getViewModel().setCommandInProgress(false);
        vBinding.getViewModel().setDeviceStatus(devStatus);
    }

    @Override
    public void onNewFile(DeviceFile devFile) {
        vBinding.getViewModel().setCommandInProgress(false);
        vBinding.getViewModel().setCurrentFile(devFile);
    }

    @Override
    public void onPositionUpdate(DevicePosition position) {
        vBinding.getViewModel().setCommandInProgress(false);
        vBinding.getViewModel().setCurrentPosition(position);
    }

    @Override
    public void onSimpleCommandComplete(String commandName) {
        vBinding.getViewModel().setCommandInProgress(false);
    }

    @Override
    public void onDeviceResponseError(Class<? extends BaseRestCommand> commandClass, String reason) {
        Toast.makeText(this, "Device responded error - \n"+reason, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCommunicationError(Class<? extends BaseRestCommand> commandClass, String error) {
        Toast.makeText(this, "Communication error - \n"+error, Toast.LENGTH_LONG).show();
    }
}
