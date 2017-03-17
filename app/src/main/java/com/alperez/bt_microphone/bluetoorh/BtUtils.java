package com.alperez.bt_microphone.bluetoorh;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class BtUtils {
    @SuppressLint("NewApi")
    public static BluetoothAdapter getBtAdapter(Context ctx) throws BluetoothNotSupportedException {

        BluetoothAdapter ad = (Build.VERSION.SDK_INT >= 18) ? ((BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter() : BluetoothAdapter.getDefaultAdapter();
        if (ad != null) {
            return ad;
        } else {
            throw new BluetoothNotSupportedException();
        }
    }


    public static void silentCloseBtSocket(@Nullable BluetoothSocket soc) {
        if (soc != null) {
            try {
                soc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void silentlyCloseCloseable(@Nullable Closeable clos) {
        if (clos != null) {
            try {
                clos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static byte[] prepareDataToTransmit(String text) {
        if (text.length() == 0) return new byte[0];


        byte[] origData = text.getBytes();
        int len = origData.length;

        if (origData[len-2] == 0x0D && origData[len-1] == 0x0A) {
            return origData;
        } else {
            int newLen = len;
            if (origData[len-2] == 0x0A && origData[len-1] == 0x0D) {
                newLen = len-2;
            } else if (origData[len-1] == 0x0D || origData[len-1] == 0x0A) {
                newLen = len-1;
            }
            byte[] nData = new byte[newLen + 2];
            System.arraycopy(origData, 0, nData, 0, newLen);
            nData[newLen-2] = 0x0D;
            nData[newLen-1] = 0x0A;
            return nData;
        }
    }
}
