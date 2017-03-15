package com.alperez.bt_microphone.ui;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.ui.viewmodel.BtDeviceViewModel;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class BindingAdapters {

    @BindingAdapter("deviceTypeIcon")
    public static void setImageView_DeviceTipeIcon(ImageView iv, int devType) {

        switch (devType) {
            case BtDeviceViewModel.DEVICE_TYPE_INVALID:
                iv.setImageResource(R.drawable.ic_bluetooth_disabled_black_24dp);
                break;
            case BtDeviceViewModel.DEVICE_TYPE_VALID_NEW:
                iv.setImageResource(R.drawable.ic_bluetooth_searching_black_24dp);
                break;
            case BtDeviceViewModel.DEVICE_TYPE_VALID_SAVED:
                iv.setImageResource(R.drawable.ic_favorite_black_24dp);
                break;
        }
    }


    @BindingAdapter("rssi")
    public static void setTextView_RSSI(TextView tv, short rssi) {
        if (rssi == Short.MIN_VALUE) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(String.format(((rssi >= 0) ? "+%d dBm" : "-%d dBm"), rssi));
        }
    }


    @BindingAdapter("intValueAsText")
    public static void setTextView_IntValue(TextView tv, int value) {
        tv.setText(Integer.toString(value));
    }



    /********************************  Automatic Conversions  *************************************/
    @BindingConversion
    public static String convertToString(Date d) {
        return String.format("%1$td-%1$tm-%1$tY", d);
    }


}
