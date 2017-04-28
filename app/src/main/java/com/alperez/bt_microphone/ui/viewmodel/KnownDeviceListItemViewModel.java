package com.alperez.bt_microphone.ui.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.alperez.bt_microphone.BR;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;

/**
 * Created by stanislav.perchenko on 4/28/2017.
 */

public class KnownDeviceListItemViewModel extends BaseObservable {

    public enum KnownDeviceStatus {
        STATUS_CHECKING, STATUS_OFFLINE, STATUS_ONLINE
    }


    private ValidDeviceDbModel validDevice;
    private KnownDeviceStatus knownDeviceStatus;


    public KnownDeviceListItemViewModel(ValidDeviceDbModel validDevice) {
        this.validDevice = validDevice;
    }

    @Bindable
    public ValidDeviceDbModel getValidDevice() {
        return validDevice;
    }

    public void setValidDevice(ValidDeviceDbModel validDevice) {
        this.validDevice = validDevice;
        notifyPropertyChanged(BR.validDevice);
    }

    @Bindable
    public KnownDeviceStatus getKnownDeviceStatus() {
        return knownDeviceStatus;
    }

    public void setKnownDeviceStatus(KnownDeviceStatus knownDeviceStatus) {
        if ((this.knownDeviceStatus == null) || (this.knownDeviceStatus != knownDeviceStatus)) {
            this.knownDeviceStatus = knownDeviceStatus;
            notifyPropertyChanged(BR.knownDeviceStatus);
        }
    }
}
