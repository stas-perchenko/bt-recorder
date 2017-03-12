package com.alperez.bt_microphone.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.Layout;
import com.alperez.bt_microphone.ui.fragment.CheckKnownDevicesFragment;
import com.alperez.bt_microphone.ui.fragment.CheckNewDeviceFragment;
import com.alperez.bt_microphone.ui.fragment.DiscoverDevicesFragment;

import java.util.List;
import java.util.Map;

@Layout(value = R.layout.activity_start)
public class StartActivity extends BaseActivity implements DiscoverDevicesFragment.BluetoothAdapterProvider, DiscoverDevicesFragment.OnDeviceSelectionResultListener, CheckNewDeviceFragment.OnDeviceFerifiedListener {


    BluetoothAdapter btAdapter;
    private List<ValidBtDevice> savedDevices;


    private Map<String, Fragment> allFragments;
    private Fragment currFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();


        try {
            btAdapter = BtUtils.getBtAdapter(this);
        } catch (BluetoothNotSupportedException e) {
            e.printStackTrace();
            finish();
            return;
        }

        savedDevices = selectSavedDevices();
        setKnownDevicesGroupVisible(savedInstanceState.size() > 0);

        findViewById(R.id.btn_connect_last).setOnClickListener(v -> goToMainActivity(savedDevices.get(0)));
        findViewById(R.id.btn_check_known).setOnClickListener(v -> showCheckKnownDevicesFragment());
        findViewById(R.id.btn_search_new).setOnClickListener(v -> showDiscoverDevicesFragment());

    }

    private void goToMainActivity(ValidBtDevice dev) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.ARG_VALID_DEVICE_ID, dev.id());
        startActivity(intent);
        finish();
    }

    private void showCheckKnownDevicesFragment() {
        if ((currFragment != null) && (currFragment instanceof CheckKnownDevicesFragment)) {
            // The required fragment is being shown;
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (currFragment != null) {
            ft.detach(currFragment);
        }

        Fragment f = allFragments.get(CheckKnownDevicesFragment.class.getName());
        if (f != null) {
            ((CheckKnownDevicesFragment) f).setDevices(savedDevices);
            ft.attach(f);
        } else {
            f = new CheckKnownDevicesFragment();
            ((CheckKnownDevicesFragment) f).setDevices(savedDevices);
            allFragments.put(CheckKnownDevicesFragment.class.getName(), f);
            ft.add(R.id.children_fragments_container, f);
        }
        ft.commit();
    }

    private void showDiscoverDevicesFragment() {
        if ((currFragment != null) && (currFragment instanceof DiscoverDevicesFragment)) {
            // The required fragment is being shown;
            return;
        }


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (currFragment != null) {
            ft.detach(currFragment);
        }

        Fragment f = allFragments.get(DiscoverDevicesFragment.class.getName());
        if (f != null) {
            ft.attach(f);
        } else {
            f = new DiscoverDevicesFragment();
            allFragments.put(DiscoverDevicesFragment.class.getName(), f);
            ft.add(R.id.children_fragments_container, f);
        }
        ft.commit();
    }




    /*************************  interfaces for the Discover activity  *****************************/
    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }

    @Override
    public void onNewDeviceSelected(DiscoveredBluetoothDevice device) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if ((currFragment != null) && (currFragment instanceof CheckNewDeviceFragment)) {
            ft.remove(currFragment);
            allFragments.remove(CheckNewDeviceFragment.class.getName());
            currFragment = null;
        } else if (currFragment != null) {
            ft.detach(currFragment);
        }

        Fragment f = allFragments.get(CheckNewDeviceFragment.class.getName());
        if (f != null) {
            ((CheckNewDeviceFragment) f).setArgDevice(device);
            ft.attach(f);
        } else {
            f = CheckNewDeviceFragment.newInstance(device);
            allFragments.put(CheckNewDeviceFragment.class.getName(), f);
            ft.add(R.id.children_fragments_container, f);
        }
        ft.commit();
    }

    @Override
    public void onKnowDeviceSelected(ValidBtDevice device) {
        goToMainActivity(device);
    }





    /**********************  Interfaces for Check device Fragment  ********************************/
    @Override
    public void onDeveiceVerified(ValidBtDevice device) {
        goToMainActivity(device);
    }

    @Override
    public void onErrorVerification(DiscoveredBluetoothDevice device) {
        //TODO Show error and ask question!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }






    /**********************************************************************************************/
    private void setKnownDevicesGroupVisible(boolean visible) {
        findViewById(R.id.container_known_devices).setVisibility(visible ? View.VISIBLE : View.GONE);
        findViewById(R.id.txt_no_saved_devices).setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private List<ValidBtDevice> selectSavedDevices() {
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            return db.selectAllValidDevices();
        } finally {
            db.close();
        }
    }










}
