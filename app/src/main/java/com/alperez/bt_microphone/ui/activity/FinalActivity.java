package com.alperez.bt_microphone.ui.activity;

import android.bluetooth.BluetoothDevice;
import android.databinding.DataBindingUtil;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundPlayer;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundPlayerImpl;
import com.alperez.bt_microphone.core.DeviceState;
import com.alperez.bt_microphone.core.RemoteDevice;
import com.alperez.bt_microphone.databinding.ActivityFinalBinding;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceFile;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceStatus;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.viewmodel.MainControlsViewModel;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/24/2017.
 */

public class FinalActivity extends AppCompatActivity implements RemoteDevice.OnCommandResultListener {
    public static final String ARG_VALID_DEVICE_ID = "device_id";

    private RemoteDevice remDevice;
    private BtSoundPlayer mPlayer;


    private ActivityFinalBinding vBinding;

    private AudioTrack createAudioTrack() {
        int minBufSizeBytes = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        minBufSizeBytes = 1024;
        return new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT, 2*minBufSizeBytes, AudioTrack.MODE_STREAM);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            ValidDeviceDbModel dbDevice = getDeviceArgument();
            BluetoothDevice btDevice = BtUtils.getBtAdapter(this).getRemoteDevice(dbDevice.macAddress());
            remDevice = new RemoteDevice(dbDevice.withBluetoothDevice(btDevice), 2000, this);

            mPlayer = new BtSoundPlayerImpl(btDevice, GlobalConstants.UUID_SERVICE_2, createAudioTrack());
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
        vBinding.setClickerPhantom((v) -> onPhantom());

        vBinding.setClickerForward((v) -> onForward(10));
        vBinding.setClickerReverse((v) -> onReverse(10));

        vBinding.setClickerSmprate1((v) -> onNewRate(GlobalConstants.SAMPLE_RATE_48K));
        vBinding.setClickerSmprate2((v) -> onNewRate(GlobalConstants.SAMPLE_RATE_96K));
        vBinding.setClickerSmprate3((v) -> onNewRate(GlobalConstants.SAMPLE_RATE_192K));

        vBinding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int controlH = getTotalChildrenHEight(vBinding.controlContainer);


                int reqH = vBinding.getRoot().getHeight();
                if (reqH != controlH) {
                    int space = reqH - vBinding.row6Container.getLayoutParams().height - vBinding.title1.getMeasuredHeight();
                    int rowH = Math.round(space / 6f);
                    vBinding.row0Container.getLayoutParams().height = rowH;
                    vBinding.row1Container.getLayoutParams().height = rowH;
                    vBinding.row2Container.getLayoutParams().height = rowH;
                    vBinding.row3Container.getLayoutParams().height = rowH;



                    vBinding.row4Container.getLayoutParams().height = rowH;
                    vBinding.row5Container.getLayoutParams().height = rowH;


                    vBinding.getRoot().requestLayout();
                }
                if (Build.VERSION.SDK_INT >= 16) {
                    vBinding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    vBinding.getRoot().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }

            private int getTotalChildrenHEight(ViewGroup vg) {
                int h = 0;
                for (int i=0; i<vg.getChildCount(); i++) {
                    h += vg.getChildAt(i).getLayoutParams().height;
                }
                return h;
            }
        });
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
        getWindow().getDecorView().postDelayed(() -> {
            remDevice.commandVersion();
            remDevice.commandCurrentFile();
            remDevice.commandStatus();
        }, 600);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) mPlayer.release();
        if (remDevice != null) remDevice.release();
    }









    private void onStopClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        vBinding.getViewModel().setDevState(DeviceState.STOPPING);
        remDevice.commandStop();
    }

    private void onRecordClicked() {
        MainControlsViewModel vModel = vBinding.getViewModel();
        if ((vModel.getDevState() != DeviceState.RECORDING) && (vModel.getDevState() != DeviceState.START_RECORDING)) {
            vBinding.getViewModel().setCommandInProgress(true);
            vBinding.getViewModel().setDevState(DeviceState.START_RECORDING);
            remDevice.commandRecord();
        } else {
            vModel.setDevState(vModel.getDevState());
        }
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

    private void onForward(int seconds) {
        remDevice.commandFastForward(seconds);
    }

    private void onReverse(int seconds) {
        remDevice.commandFastReverse(seconds);
    }

    private void onNewRate(int rate) {
        MainControlsViewModel vModel = vBinding.getViewModel();
        if (vModel.getRecordingSampleRate() != rate) {
            vModel.setRecordingSampleRate(0);
            remDevice.commandSetSampleRate(rate);
        } else {
            vModel.setRecordingSampleRate(rate);
        }
    }

    private void onPhantom() {
        vBinding.getViewModel().setCommandInProgress(true);
        boolean ph = !vBinding.getViewModel().isPhantomPower();
        remDevice.commandPhantomOnOff(ph);
        //vBinding.getViewModel().setPhantomPower(ph);
    }






    private boolean gotStateFirstTime = true;

    @Override
    public void onStatusUpdate(DeviceStatus devStatus) {
        vBinding.getViewModel().setCommandInProgress(false);
        vBinding.getViewModel().setDeviceStatus(devStatus);
        if (gotStateFirstTime) {
            gotStateFirstTime = false;
            vBinding.getRoot().post(() -> remDevice.commandSetTime(new Date()));
        }

        switch (devStatus.deviceState()) {
            case PLAYING:
            case RECORDING:
                mPlayer.play();
                break;
            case PAUSED:
            case STOPPED:
                mPlayer.pause();
                break;
        }

    }

    @Override
    public void onNewFile(DeviceFile devFile) {
        vBinding.getViewModel().setCommandInProgress(false);
        vBinding.getViewModel().setCurrentFile(devFile);
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
