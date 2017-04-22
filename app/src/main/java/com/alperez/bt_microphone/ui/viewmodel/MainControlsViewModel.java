package com.alperez.bt_microphone.ui.viewmodel;

import android.app.Activity;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.location.Location;

import com.alperez.bt_microphone.BR;
import com.alperez.bt_microphone.core.DeviceState;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceFile;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceStatus;
import com.alperez.bt_microphone.utils.FormatUtils;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/25/2017.
 */

public class MainControlsViewModel extends BaseObservable {

    private Activity hostActivity;

    public MainControlsViewModel(Activity hostActivity) {
        this.hostActivity = hostActivity;
    }

    private boolean controlsLocked;
    private boolean optionsLocked;

    private boolean commandInProgress;



    /***** Status-related ********/
    private Date deviceTime = new Date(0);
    private DeviceState devState = DeviceState.UNDEFINED;
    private String memorySpace = "0";
    private String batteryLevel = "0";
    private boolean phantomPower;
    private int recordingSampleRate = 0;
    private String gainLevel = "0";
    private String memCardStatus = "";


    private Location currentDeviceLocation;

    @Bindable
    public Location getCurrentDeviceLocation() {
        return currentDeviceLocation;
    }

    public void setCurrentDeviceLocation(Location currentDeviceLocation) {
        this.currentDeviceLocation = currentDeviceLocation;
        notifyPropertyChanged(BR.currentDeviceLocation);
    }



    @Bindable
    public boolean isControlsLocked() {
        return controlsLocked;
    }

    public void setControlsLocked(boolean controlsLocked) {
        this.controlsLocked = controlsLocked;
        notifyPropertyChanged(BR.controlsLocked);
    }


    @Bindable
    public boolean isOptionsLocked() {
        return optionsLocked;
    }

    public void setOptionsLocked(boolean optionsLocked) {
        this.optionsLocked = optionsLocked;
        notifyPropertyChanged(BR.optionsLocked);
    }

    @Bindable
    public boolean isCommandInProgress() {
        return commandInProgress;
    }

    public void setCommandInProgress(boolean commandInProgress) {
        this.commandInProgress = commandInProgress;
        notifyPropertyChanged(BR.commandInProgress);
    }


    /**********************************************************************************************/
    /**
     * This is the main setter to update full device status.
     * It compares new values with current ones and calls appropriate setters if necessary
     * @param devStatus
     */
    public void setDeviceStatus(DeviceStatus devStatus) {
        long timeRecordingMillis = 1000*devStatus.freeSpaceBytes() / (3 * devStatus.recordingSampleRate());


        setDeviceTime(devStatus.deviceTime());
        setDevState(devStatus.deviceState());
        setMemorySpace( FormatUtils.timeMillisToHMS(timeRecordingMillis) );
        setBatteryLevel(Integer.toString(devStatus.batteryLevel()));
        setPhantomPower(devStatus.isPhantomPowerOn());
        setGainLevel(Integer.toString(devStatus.gainLevel()));
        setRecordingSampleRate(devStatus.recordingSampleRate());
        setMemCardStatus(FormatUtils.memoryCartStatusToText(hostActivity, devStatus.memoryCardstatus()));


        switch (devStatus.deviceState()) {
            case UNDEFINED:
            case STOPPED:
            case RECORDING:
                setCurrentPosition(0);
        }

    }


    @Bindable
    public Date getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(Date deviceTime) {
        if (!deviceTime.equals(this.deviceTime)) {
            this.deviceTime = deviceTime;
            notifyPropertyChanged(BR.deviceTime);
        }
    }

    @Bindable
    public DeviceState getDevState() {
        return devState;
    }
    public void setDevState(DeviceState devState) {
        this.devState = devState;
        notifyPropertyChanged(BR.devState);
    }



    @Bindable
    public String getMemorySpace() {
        return memorySpace;
    }


    public void setMemorySpace(String memorySpace) {
        if (!memorySpace.equals(this.memorySpace)) {
            this.memorySpace = memorySpace;
            notifyPropertyChanged(BR.memorySpace);
        }
    }


    @Bindable
    public String getBatteryLevel() {
        return batteryLevel;
    }
    public void setBatteryLevel(String batteryLevel) {
        if (!batteryLevel.equals(this.batteryLevel)) {
            this.batteryLevel = batteryLevel;
            notifyPropertyChanged(BR.batteryLevel);
        }
    }
    @Bindable
    public boolean isPhantomPower() {
        return phantomPower;
    }
    public void setPhantomPower(boolean phantomPower) {
        if (this.phantomPower != phantomPower) {
            this.phantomPower = phantomPower;
            notifyPropertyChanged(BR.phantomPower);
        }
    }
    @Bindable
    public String getGainLevel() {
        return gainLevel;
    }
    public void setGainLevel(String gainLevel) {
        if (this.gainLevel != gainLevel) {
            this.gainLevel = gainLevel;
            notifyPropertyChanged(BR.gainLevel);
        }
    }

    @Bindable
    public int getRecordingSampleRate() {
        return recordingSampleRate;
    }
    public void setRecordingSampleRate(int recordingSampleRate) {
        this.recordingSampleRate = recordingSampleRate;
        notifyPropertyChanged(BR.recordingSampleRate);
    }

    @Bindable
    public String getMemCardStatus() {
        return memCardStatus;
    }

    public void setMemCardStatus(String memCardStatus) {
        this.memCardStatus = memCardStatus;
        notifyPropertyChanged(BR.memCardStatus);
    }

    /**********************************************************************************************/


    /*****  File-related ********/
    private Date currentTimeStart = new Date(0);
    private long currentDuration;
    private float currentPosition;
    private String currentSampleRate = "0";
    private Location currentFileLocation;


    public void setCurrentFile(DeviceFile devFile) {
        setCurrentTimeStart(devFile.startTime());
        setCurrentDuration(devFile.durationMillis());
        setCurrentPosition(devFile.currentPosition());
        setCurrentSampleRate(Integer.toString(devFile.sampleRate()));
        setCurrentFileLocation(devFile.geoLocation());
    }


    @Bindable
    public Date getCurrentTimeStart() {
        return currentTimeStart;
    }

    public void setCurrentTimeStart(Date currentTimeStart) {
        if (!currentTimeStart.equals(this.currentTimeStart)) {
            this.currentTimeStart = currentTimeStart;
            notifyPropertyChanged(BR.currentTimeStart);
        }
    }

    @Bindable
    public int getCurrentDuration() {
        return (int)currentDuration;
    }

    public void setCurrentDuration(long currentDuration) {
        if (currentDuration != this.currentDuration) {
            this.currentDuration = currentDuration;
            notifyPropertyChanged(BR.currentDuration);
        }
    }

    @Bindable
    public int getCurrentPosition() {
        return (int)Math.min(currentPosition, currentDuration);
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
        notifyPropertyChanged(BR.currentPosition);
    }

    public void addTimePlayed(float timeMillis) {
        currentPosition += timeMillis;
        notifyPropertyChanged(BR.currentPosition);
    }




    @Bindable
    public String getCurrentSampleRate() {
        return currentSampleRate;
    }

    public void setCurrentSampleRate(String currentSampleRate) {
        if (!currentSampleRate.equals(this.currentSampleRate)) {
            this.currentSampleRate = currentSampleRate;
            notifyPropertyChanged(BR.currentSampleRate);
        }
    }

    @Bindable
    public Location getCurrentFileLocation() {
        return currentFileLocation;
    }

    public void setCurrentFileLocation(Location currentFileLocation) {
        this.currentFileLocation = currentFileLocation;
        notifyPropertyChanged(BR.currentFileLocation);
    }



}
