package com.alperez.bt_microphone.ui.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.alperez.bt_microphone.BR;
import com.alperez.bt_microphone.core.DeviceState;

/**
 * Created by stanislav.perchenko on 3/25/2017.
 */

public class MainControlsViewModel extends BaseObservable {


    private boolean controlsLocked;
    private boolean optionsLocked;

    private boolean commandInProgress;

    

    /***** Status-related ********/
    private String memorySpace = "";
    private String batteryLevel = "";
    private boolean phantomPower;
    private int sampleRate;
    private String gainLevel = "";
    private DeviceState devState;










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
}
