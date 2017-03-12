package com.alperez.bt_microphone.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.Layout;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */
@Layout(value = R.layout.activity_main)
public class MainActivity extends BaseActivity {
    public static final String ARG_VALID_DEVICE_ID = "device_id";


    ValidBtDevice mDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();

        mDevice = getDeviceArgument();
        if (mDevice == null) {
            Toast.makeText(this, "No device provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.txt_dev_name)).setText(mDevice.userDefinedName()+" - "+mDevice.deviceName());
    }

    private ValidBtDevice getDeviceArgument() {
        long id = getIntent().getLongExtra(ARG_VALID_DEVICE_ID, -1);
        if (id <= 0) {
            return null;
        }
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            return db.selectValidDeviceById(id);
        } finally {
            db.close();
        }
    }
}
