package com.alperez.bt_microphone.utils;

import android.content.Context;

import com.alperez.bt_microphone.R;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public class FormatUtils {
    public static final String EMPTY_STRING = "";

    public static final String UI_DATE_FORMAT_ISO="%tF";
    public static final String UI_DATE_FORMAT_RU="%1$td-%1$tm-%1$tY";

    public static final String UI_TIME_FAST = "%1$tM:%1$tS.%1$tL";



    public static String timeMillisToHMS(long millis) {
        int ss = Math.round(millis / 1000f);
        int hh = ss / 3600;
        ss -= (hh * 3600);
        int mm = ss / 60;
        ss -= (mm * 60);
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    public static final String memoryCartStatusToText(Context c, final int memStatus) {
        switch (memStatus) {
            case 0:
                return c.getString(R.string.memory_card_status_0);
            case 1:
                return c.getString(R.string.memory_card_status_1);
            case 2:
                return c.getString(R.string.memory_card_status_2);
            case 3:
                return c.getString(R.string.memory_card_status_3);
            case 4:
                return c.getString(R.string.memory_card_status_4);
            case 5:
                return c.getString(R.string.memory_card_status_5);
            default:
                return c.getString(R.string.memory_card_status_undef);
        }
    }

}
