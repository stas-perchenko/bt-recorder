package com.alperez.bt_microphone.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundPlayer;
import com.alperez.bt_microphone.bluetoorh.connector.sound.BtSoundPlayerImpl;
import com.alperez.bt_microphone.bluetoorh.connector.sound.OnPlayerPerformanceListener;
import com.alperez.bt_microphone.bluetoorh.connector.sound.SoundLevelMeter;
import com.alperez.bt_microphone.core.DeviceState;
import com.alperez.bt_microphone.core.RemoteDevice;
import com.alperez.bt_microphone.databinding.ActivityFinalBinding;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.rest.OnCompleteListener;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceFile;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceStatus;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.viewmodel.MainControlsViewModel;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stanislav.perchenko on 3/24/2017.
 */

public class FinalActivity extends AppCompatActivity implements RemoteDevice.OnCommandResultListener, LocationListener {
    public static final int REQUEST_LOCATION_PERMISSION = 101;

    public static final String ARG_VALID_DEVICE_ID = "device_id";

    private RemoteDevice remDevice;
    private BtSoundPlayer mPlayer;


    private ActivityFinalBinding vBinding;

    private boolean locationEnabled;
    private boolean fineProviderEnabled;
    private Location currentDeviceLocation;

    private AudioTrack createAudioTrack() {
        int minBufSizeBytes = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        minBufSizeBytes = 1024;
        return new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT, 2 * minBufSizeBytes, AudioTrack.MODE_STREAM);
    }

    private LocationManager locationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
            locationEnabled = true;
            fineProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        try {
            ValidDeviceDbModel dbDevice = getDeviceArgument();
            BluetoothDevice btDevice = BtUtils.getBtAdapter(this).getRemoteDevice(dbDevice.macAddress());
            remDevice = new RemoteDevice(dbDevice.withBluetoothDevice(btDevice), 2000, this);

            mPlayer = new BtSoundPlayerImpl(btDevice, GlobalConstants.UUID_SERVICE_2, createAudioTrack(), new SoundLevelMeter((rms, peak) -> {
                vBinding.levelPeak.setLevel(peak);
                vBinding.levelRms.setLevel(Math.round(rms * 2.5f));
            }));
            mPlayer.setOnPlayerPerformanceListener(new OnPlayerPerformanceListener() {
                @Override
                public void onBytesReceived(int nBytes) {

                }

                @Override
                public void onBytesPlayed(int nBytes) {
                    vBinding.getViewModel().addTimePlayed(nBytes / 8f);
                }
            });
        } catch (BluetoothNotSupportedException e) {
            finish();
            return;
        }


        vBinding = DataBindingUtil.setContentView(this, R.layout.activity_final);
        vBinding.setViewModel(new MainControlsViewModel(this));
        vBinding.inProgressCoverView.setOnClickListener((v) -> {/* For touch interception */});

        vBinding.levelPeak.setMaxLevel(128);
        vBinding.levelRms.setMaxLevel(128);

        vBinding.actionSettings.setOnClickListener(v -> {
        });

        vBinding.setClickerRecord((v) -> onRecordClicked());
        vBinding.setClickerStop((v) -> onStopClicked());
        vBinding.setClickerPlayPause((v) -> onPlayPauseClicked());
        vBinding.setClickerGainUp((v) -> onGainUpClicked());
        vBinding.setClickerGainDown((v) -> onGainDownClicked());
        vBinding.setClickerPrevTrack((v) -> onPrevTrackClicked());
        vBinding.setClickerNextTrack((v) -> onNextTrackClicked());
        vBinding.setClickerPhantom((v) -> onPhantom());

        vBinding.setListenerForward((int step, int dt) -> onForward(dt));
        vBinding.setListenerReverse((int step, int dt) -> onReverse(dt));

        vBinding.setClickerSmprate1((v) -> onNewRate(GlobalConstants.SAMPLE_RATE_48K));
        vBinding.setClickerSmprate2((v) -> onNewRate(GlobalConstants.SAMPLE_RATE_96K));
        vBinding.setClickerSmprate3((v) -> onNewRate(GlobalConstants.SAMPLE_RATE_192K));

        vBinding.setClickerSettings((v) -> onShowSettings());

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
                for (int i = 0; i < vg.getChildCount(); i++) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                locationEnabled = true;
                fineProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

        mStatusTimer = new Timer();
        mStatusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                DeviceState devState = vBinding.getViewModel().getDevState();
                switch (devState) {
                    case PLAYING:
                    case RECORDING:
                        remDevice.commandStatus();
                }
            }
        }, 1500, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStatusTimer.cancel();
    }

    private Timer mStatusTimer;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) mPlayer.release();
        if (remDevice != null) remDevice.release();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }


    /******************************  Location listener  *******************************************/
    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location", "Location received from - "+location.getProvider());
        if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
            currentDeviceLocation = location;
            vBinding.getViewModel().setCurrentDeviceLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            fineProviderEnabled = true;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            fineProviderEnabled = false;
            currentDeviceLocation = null;
        }
    }
    /**********************************************************************************************/


    private void onStopClicked() {
        vBinding.getViewModel().setCommandInProgress(true);
        if (vBinding.getViewModel().getDevState() == DeviceState.RECORDING) {
            vBinding.getRoot().postDelayed(()->remDevice.commandCurrentFile(), 150);
        }
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




    /**********************  Settings menu  *******************************************************/
    private void onShowSettings() {
        String[] items = {getString(R.string.action_show_memory_status), getString(R.string.action_memory_format), getString(R.string.action_set_time)};
        new AlertDialog.Builder(this).setTitle(R.string.settings_dialog_title).setItems(items, (DialogInterface dialog, int which) -> {
            switch (which) {
                case 0:
                    onShowMemoryStatus();
                    break;
                case 1:
                    onFormatCard();
                    break;
                case 2:
                    remDevice.commandSetTime(new Date());
                    break;
            }
        }).show();
    }


    /******************************  Format memory menu option  ***********************************/
    private void onFormatCard() {
        new AlertDialog.Builder(this).setTitle(R.string.dialog_format_title).setMessage(R.string.dialog_format_message)
                .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) -> startFormatMemory() )
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private void startFormatMemory() {
        remDevice.commandFormat(new OnCompleteListener() {
            @Override
            public void onComplete() {
                getFormattingProgressDialog().dismiss();
                Toast.makeText(FinalActivity.this, R.string.msg_formatting_ok, Toast.LENGTH_LONG).show();
                getWindow().getDecorView().postDelayed(() -> {
                    remDevice.commandCurrentFile();
                    remDevice.commandStatus();
                }, 150);
            }

            @Override
            public void onError(String reason) {
                getFormattingProgressDialog().dismiss();
                Toast.makeText(FinalActivity.this, getString(R.string.msg_formatting_error)+" - "+reason, Toast.LENGTH_LONG).show();
            }
        });

        getFormattingProgressDialog().show();
    }

    private ProgressDialog formattingProgress;
    private ProgressDialog getFormattingProgressDialog() {
        if (formattingProgress == null) {
            formattingProgress = new ProgressDialog(this);
            formattingProgress.setIndeterminate(true);
            formattingProgress.setCancelable(false);
            formattingProgress.setCanceledOnTouchOutside(false);
            formattingProgress.setMessage(getString(R.string.msg_formatting_progress));
        }
        return formattingProgress;
    }

    /***********************  Show mem status menu option  ****************************************/
    private void onShowMemoryStatus() {
        new AlertDialog.Builder(this).setTitle(R.string.dialog_mem_card_status_title)
                .setMessage(vBinding.getViewModel().getMemCardStatus())
                .setPositiveButton(android.R.string.ok, null).show();
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

        checkLocation(devStatus.deviceLocation());

        switch (devStatus.deviceState()) {
            case PLAYING:
            case RECORDING:
                mPlayer.play();
                break;
            case PAUSED:
            case STOPPED:
                mPlayer.pause();


                vBinding.getRoot().postDelayed(() -> {
                    vBinding.levelRms.setLevel(0);
                    vBinding.levelPeak.setLevel(0);
                }, 150);
                break;
        }

    }

    private void checkLocation(Location deviceLocation) {
        if (locationEnabled && fineProviderEnabled && currentDeviceLocation != null) {
            if ((deviceLocation == null) || (deviceLocation.distanceTo(currentDeviceLocation) > 30)) {
                vBinding.getRoot().postDelayed(() -> remDevice.commandSetLocation(currentDeviceLocation), 150);
            }
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
