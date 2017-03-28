package com.alperez.bt_microphone.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/28/2017.
 */

public class RestUtils {

    /*******************  Parsing/Formatting date-time for communication purposes  ****************/
    private static final String REST_DATE_TIME_FORMAT = "%1$tFT%1$tT.%1$tL";

    public static final String dateToRemoteString(Date d) {
        synchronized (REST_DATE_TIME_FORMAT) {
            return String.format(REST_DATE_TIME_FORMAT, d);
        }
    }

    public static final Date parseRemoteDateTime(String time) throws ParseException {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(time);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(time);
            } catch (ParseException e1) {
                return new SimpleDateFormat("yyyy-MM-dd").parse(time);
            }
        }
    }

}
