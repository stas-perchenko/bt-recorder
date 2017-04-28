package com.alperez.bt_microphone.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.management.impl.DeviceFounderImpl;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.Layout;
import com.alperez.bt_microphone.ui.fragment.DiscoverDevicesFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Layout(value = R.layout.activity_start)
public class StartActivity extends BaseActivity implements DiscoverDevicesFragment.BluetoothAdapterProvider, DiscoverDevicesFragment.OnDeviceSelectionResultListener {
    private static final int REQUEST_VALIDATE_DEVICE = 101;


    BluetoothAdapter btAdapter;
    private List<ValidDeviceDbModel> savedDevices;


    private Map<String, Fragment> allFragments = new HashMap<>();
    private Fragment currFragment;


    @Override
    protected String getActivityTitle() {
        return "START SCREEN";
    }

    @Override
    protected String getActivitySubtitle() {
        return null;
    }

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
        setKnownDevicesGroupVisible(savedDevices.size() > 0);

        findViewById(R.id.btn_connect_last).setOnClickListener(v -> goToMainActivity(savedDevices.get(0)));
        findViewById(R.id.btn_check_known).setOnClickListener(v -> startActivity(new Intent(this, KnownDeviceListActivity.class)));
        findViewById(R.id.btn_search_new).setOnClickListener(v -> showDiscoverDevicesFragment());

    }

    private void goToMainActivity(ValidDeviceDbModel dev) {
        Intent intent = new Intent(this, FinalActivity.class);
        intent.putExtra(FinalActivity.ARG_VALID_DEVICE_ID, dev.id());
        startActivity(intent);
        finish();
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
        currFragment = f;
        ft.commit();
    }




    /*************************  interfaces for the Discover activity  *****************************/
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





    /**********************************************************************************************/
    private void setKnownDevicesGroupVisible(boolean visible) {
        findViewById(R.id.container_known_devices).setVisibility(visible ? View.VISIBLE : View.GONE);
        findViewById(R.id.txt_no_saved_devices).setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private List<ValidDeviceDbModel> selectSavedDevices() {
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            return db.selectAllValidDevices();
        } finally {
            db.close();
        }
    }








    /**********************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_clear_db, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_clear_db:
                clearDeviceCache();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void clearDeviceCache() {
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            db.beginTransaction();

            int nValid = db.clearValidDevices();
            int nBlack = db.clearBlacklistedDevices();
            Log.d(DeviceFounderImpl.TAG, String.format("Device cache was cleared. N valid = %d, N blacklisted = %d", nValid, nBlack));
            db.commitTransaction();
        } finally {
            db.endTransaction();
            db.close();


        }
    }
}
