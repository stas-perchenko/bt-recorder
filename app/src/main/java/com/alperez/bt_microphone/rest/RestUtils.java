package com.alperez.bt_microphone.rest;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/28/2017.
 */

public class RestUtils {


    public static Location parseLocationFromJson(JSONObject json) {
        String sLat = json.optString("lat", null);
        String sLon = json.optString("lon", null);
        if (sLat == null || sLon == null) return null;
        Location loc = new Location("GLONASS");
        loc.setLatitude(Double.parseDouble(sLat));
        loc.setLongitude(Double.parseDouble(sLon));
        return loc;
    }

    public static boolean parseBooleanOnOffFallback(JSONObject jObj, String fieldName) throws JSONException {
        try {
            return jObj.getBoolean(fieldName);
        } catch (JSONException e) {
            String sBool = jObj.optString(fieldName, "false");
            if ("false".equalsIgnoreCase(sBool) || "off".equalsIgnoreCase(sBool)) {
                return false;
            } else if ("true".equalsIgnoreCase(sBool) || "on".equalsIgnoreCase(sBool)) {
                return true;
            } else {
                throw new JSONException("Wrong boolean field value - "+sBool);
            }
        }
    }

    public static int parseIntOptString(JSONObject jObj, String fieldName) throws JSONException {
        try {
            return jObj.getInt(fieldName);
        } catch (JSONException e) {
            String sNum = jObj.optString(fieldName, "false");
            try {
                return Integer.parseInt(sNum);
            } catch (Exception e1) {
                throw new JSONException("Wrong integer field value - "+sNum);
            }
        }
    }

    public static long parseLongOptString(JSONObject jObj, String fieldName) throws JSONException {
        try {
            return jObj.getLong(fieldName);
        } catch (JSONException e) {
            String sNum = jObj.optString(fieldName, "false");
            try {
                return Long.parseLong(sNum);
            } catch (Exception e1) {
                throw new JSONException("Wrong integer field value - "+sNum);
            }
        }
    }


    /*******************  Parsing/Formatting date-time for communication purposes  ****************/
    private static final String REST_DATE_TIME_FORMAT = "%1$tFT%1$tT.%1$tL%1$tz";

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
