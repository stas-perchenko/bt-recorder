package com.alperez.bt_microphone.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.ui.activity.BaseActivity;
import com.alperez.bt_microphone.ui.activity.FinalActivity;
import com.alperez.bt_microphone.ui.activity.ValidateNewDeviceActvity;
import com.alperez.bt_microphone.ui.fragment.DiscoverDevicesFragment;

/**
 * Created by stanislav.perchenko on 4/28/2017.
 */

public class SearchDevicesActivity extends BaseActivity implements DiscoverDevicesFragment.BluetoothAdapterProvider, DiscoverDevicesFragment.OnDeviceSelectionResultListener {
    private static final int REQUEST_VALIDATE_DEVICE = 101;

    BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_devices);
        setupToolbar();
        getSupportFragmentManager().beginTransaction().add(R.id.content, new DiscoverDevicesFragment(), "discover-fragment").commit();

        try {
            btAdapter = BtUtils.getBtAdapter(this);
        } catch (BluetoothNotSupportedException e) {
            e.printStackTrace();
            finish();
            return;
        }
    }

    @Override
    protected String getActivityTitle() {
        return "DISCOVER DEVICES";
    }

    @Override
    protected String getActivitySubtitle() {
        return null;
    }


    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }

    @Override
    public void onNewDeviceSelected(DiscoveredBluetoothDevice device) {
        ValidateNewDeviceActvity.startForResult(this, REQUEST_VALIDATE_DEVICE, device);
    }

    @Override
    public void onKnowDeviceSelected(ValidDeviceDbModel device) {
        goToMainActivity(device);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VALIDATE_DEVICE && resultCode == RESULT_OK) {
            ValidDeviceDbModel result = data.getParcelableExtra(ValidateNewDeviceActvity.RESULT_VALID_DEVICE);
            if (result != null) {
                goToMainActivity(result);
            }
        } else if (requestCode != REQUEST_VALIDATE_DEVICE) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void goToMainActivity(ValidDeviceDbModel dev) {
        Intent intent = new Intent(this, FinalActivity.class);
        intent.putExtra(FinalActivity.ARG_VALID_DEVICE_ID, dev.id());
        startActivity(intent);
        finish();
    }
}
