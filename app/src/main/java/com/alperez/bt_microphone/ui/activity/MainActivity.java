package com.alperez.bt_microphone.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.management.DeviceDiscovery;
import com.alperez.bt_microphone.bluetoorh.management.DeviceFounder;
import com.alperez.bt_microphone.bluetoorh.management.OnDeviceFoundListener;
import com.alperez.bt_microphone.bluetoorh.management.impl.DeviceDiscoveryImpl;
import com.alperez.bt_microphone.bluetoorh.management.impl.DeviceFounderImpl;
import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;

public class MainActivity extends AppCompatActivity {



    private DeviceFounder deviceFounder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DeviceDiscovery deviceDiscovery = null;
        try {
            deviceDiscovery = new DeviceDiscoveryImpl(this, BtUtils.getBtAdapter(this));
        } catch (BluetoothNotSupportedException e) {
            e.printStackTrace();
            finish();
            return;
        }


        deviceFounder = new DeviceFounderImpl(this, deviceDiscovery);
        deviceFounder.setOnDeviceFoundListener(new OnDeviceFoundListener() {
            @Override
            public void onNewRawDeviceFound(DiscoveredBluetoothDevice device) {

            }

            @Override
            public void onValidDeviceFound(ValidBtDevice device) {

            }

            @Override
            public void onBlacklistedDeviceFound(BlacklistedBtDevice device) {

            }
        });


        findViewById(R.id.btn_start_stop).setOnClickListener(v -> onStartStop((Button)v));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceFounder.isStarted()) {
            deviceFounder.release();
        }
    }

    private void onStartStop(Button btn) {
        if (deviceFounder.isStarted()) {
            deviceFounder.stop();
            btn.setText("start");
        } else {
            deviceFounder.start();
            btn.setText("stop");
        }
    }
}
