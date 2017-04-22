package com.alperez.bt_microphone.ui;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.location.Location;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.ui.viewmodel.BtDeviceViewModel;

import java.text.SimpleDateFormat;
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


    @BindingAdapter("recording_start")
    public static void setTextView_Dtae1(TextView tv, Date d) {
        tv.setText(dateToDateTimeString(d));
    }

    @BindingAdapter("date_only")
    public static void setTextView_DateOnly(TextView tv, Date d) {
        tv.setText(String.format("%1$td-%1$tm-%1$tY", d));
    }

    @BindingAdapter("time_only")
    public static void setTextView_TimeOnly(TextView tv, Date d) {
        tv.setText(String.format("%1$tH:%1$tM:%1$tS", d));
    }

    @BindingAdapter("date_and_time")
    public static void setTextView_DateAndTime(TextView tv, Date d) {
        tv.setText(String.format("%1$td-%1$tm-%1$tY \u2192 %1$tH:%1$tM:%1$tS", d));
    }

    @BindingAdapter("time_from_millis")
    public static void setTextView_TimeFromMillis(TextView tv, long t) {
        timeFromMillisDate.setTime(t);
        tv.setText(String.format("%1$tM:%1$tS.%1$tL", timeFromMillisDate));
    }
    private static Date timeFromMillisDate = new Date();



    /********************************  Automatic Conversions  *************************************/
    @BindingConversion
    public static String convertToString(Date d) {
        return String.format("%1$td-%1$tm-%1$tY", d);
    }

    @BindingConversion
    public static String convertToString(Location loc) {
        return (loc != null) ? String.format("lat: %s\nlon: %s", latitudeToString(loc.getLatitude()), longitudeToString(loc.getLongitude())) : "lat: -.----\nlon: -.----";
    }









    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy \u2192 HH:mm:ss.SSS");

    public static String dateToDateTimeString(Date d) {
        synchronized (dateTimeFormatter) {
            return dateTimeFormatter.format(d);
        }
    }


    public static final String latitudeToString(double latitude) {
        return (latitude >= 0) ? String.format("%.5f\u00B0N", latitude) : String.format("%.5f\u00B0S", latitude);
    }

    public static final String longitudeToString(double longitude) {
        return (longitude >= 0) ? String.format("%.5f\u00B0E", longitude) : String.format("%.5f\u00B0W", longitude);
    }

}
