package com.alperez.bt_microphone.ui.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.location.Location;

import com.alperez.bt_microphone.BR;
import com.alperez.bt_microphone.core.DeviceState;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceFile;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceStatus;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/25/2017.
 */

public class MainControlsViewModel extends BaseObservable {


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
        setDeviceTime(devStatus.deviceTime());
        setDevState(devStatus.deviceState());
        setMemorySpace(Long.toString(devStatus.freeSpaceBytes()));
        setBatteryLevel(Integer.toString(devStatus.batteryLevel()));
        setPhantomPower(devStatus.isPhantomPowerOn());
        setGainLevel(Integer.toString(devStatus.gainLevel()));
        setRecordingSampleRate(devStatus.recordingSampleRate());
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

    /**********************************************************************************************/


    /*****  File-related ********/
    private Date currentTimeStart = new Date(0);
    private long currentDuration;
    private long currentPosition;
    private String currentSampleRate = "0";
    private Location currentLocation = new Location("");


    public void setCurrentFile(DeviceFile devFile) {
        setCurrentTimeStart(devFile.startTime());
        setCurrentDuration(devFile.durationMillis());
        setCurrentPosition(devFile.currentPosition());
        setCurrentSampleRate(Integer.toString(devFile.sampleRate()));
        setCurrentLocation(devFile.geoLocation());
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
        if (currentPosition != this.currentPosition) {
            this.currentPosition = currentPosition;
            notifyPropertyChanged(BR.currentPosition);
        }
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
    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        if (currentLocation.distanceTo(this.currentLocation) > 10) {
            this.currentLocation = currentLocation;
            notifyPropertyChanged(BR.currentLocation);
        }
    }
}
