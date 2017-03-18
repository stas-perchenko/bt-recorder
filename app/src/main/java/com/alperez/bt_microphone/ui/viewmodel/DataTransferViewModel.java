package com.alperez.bt_microphone.ui.viewmodel;

import com.alperez.bt_microphone.utils.FormatUtils;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/18/2017.
 */

public class DataTransferViewModel {

    public static DataTransferViewModel createSentItem(Date timestamp, String text) {
        DataTransferViewModel item = new DataTransferViewModel("Sent");
        item.timestamp = timestamp;
        item.data = text;
        return item;
    }

    public static DataTransferViewModel createReceivedItem(Date timestamp, String text) {
        DataTransferViewModel item = new DataTransferViewModel("Received");
        item.timestamp = timestamp;
        item.data = text;
        return item;
    }


    private String transferDirection;
    private Date timestamp;
    private String timeText;
    private String data;

    private DataTransferViewModel(String transferDirection) {
        this.transferDirection = transferDirection;
    }

    public String getTransferDirection() {
        return transferDirection;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTimeText() {
        return (timeText == null) ? (timeText = "at: "+String.format(FormatUtils.UI_TIME_FAST, timestamp)) : timeText;
    }

    public String getData() {
        return data;
    }
}
