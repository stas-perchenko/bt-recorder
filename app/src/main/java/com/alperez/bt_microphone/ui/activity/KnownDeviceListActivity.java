package com.alperez.bt_microphone.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.BluetoothNotSupportedException;
import com.alperez.bt_microphone.bluetoorh.BtUtils;
import com.alperez.bt_microphone.bluetoorh.management.DeviceOnlineChecker;
import com.alperez.bt_microphone.bluetoorh.management.impl.DeviceFounderImpl;
import com.alperez.bt_microphone.databinding.KnownDeviceListItemBinding;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.viewmodel.KnownDeviceListItemViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stanislav.perchenko on 4/28/2017.
 */

public class KnownDeviceListActivity extends BaseActivity implements DeviceOnlineChecker.OnCheckResultListener {


    BluetoothAdapter btAdapter;

    private LinearLayout vDeviceContainer;

    private List<KnownDeviceListItemViewModel> deviceVModels;

    private DeviceOnlineChecker deviceOnlineChecker;

    private boolean areDevicesBeingChecked;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            btAdapter = BtUtils.getBtAdapter(this);
        } catch (BluetoothNotSupportedException e) {
            e.printStackTrace();
            finish();
            return;
        }


        setContentView(R.layout.activity_known_device_list);
        vDeviceContainer = (LinearLayout) findViewById(R.id.container);
        findViewById(R.id.action_search_new).setOnClickListener((v) -> startActivity(new Intent(this, SearchDevicesActivity.class)));

        setupToolbar();

        deviceVModels = selectAllKnownDevices();

        createDeviceViewItems(deviceVModels);


        btAdapter.cancelDiscovery();
        deviceOnlineChecker = new DeviceOnlineChecker(this);
        for (KnownDeviceListItemViewModel vModel : deviceVModels) {
            vModel.setKnownDeviceStatus(KnownDeviceListItemViewModel.KnownDeviceStatus.STATUS_CHECKING);
            deviceOnlineChecker.checkDeviceAsync(vModel.getValidDevice());
        }
        areDevicesBeingChecked = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateActionSearchClickability();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceOnlineChecker.release();
    }

    @Override
    protected String getActivityTitle() {
        return "STORED DEVICES";
    }

    @Override
    protected String getActivitySubtitle() {
        return null;
    }


    private List<KnownDeviceListItemViewModel> selectAllKnownDevices() {

        DatabaseAdapter db = new DatabaseAdapter();
        try {
            List<ValidDeviceDbModel> devs = db.selectAllValidDevices();

            List<KnownDeviceListItemViewModel> result = new ArrayList<>(devs.size());
            for (ValidDeviceDbModel dev : devs) {

                BluetoothDevice btDevice = btAdapter.getRemoteDevice(dev.macAddress());

                result.add(new KnownDeviceListItemViewModel(dev.withBluetoothDevice(btDevice)));
            }
            return result;
        } finally {
            db.close();
        }
    }

    private void createDeviceViewItems(List<KnownDeviceListItemViewModel> vModels) {
        vDeviceContainer.removeAllViews();
        LayoutInflater inflater = getWindow().getLayoutInflater();
        for (KnownDeviceListItemViewModel vModel : vModels) {
            KnownDeviceListItemBinding binding = KnownDeviceListItemBinding.inflate(inflater, vDeviceContainer, true);
            binding.setViewModel(vModel);
            binding.getRoot().setTag(vModel.getValidDevice());
            binding.getRoot().setOnClickListener((v) -> {
                ValidDeviceDbModel dev = (ValidDeviceDbModel) v.getTag();
                startActivity(new Intent(this, FinalActivity.class).putExtra(FinalActivity.ARG_VALID_DEVICE_ID, dev.id()));
                finish();
            });
        }
    }

    int nDevicesChecked = 0;

    @Override
    public void onCheckResult(ValidDeviceDbModel device, boolean online) {
        long checkedId = device.id();
        for (KnownDeviceListItemViewModel vModel : deviceVModels) {
            if (vModel.getValidDevice().id() == checkedId) {
                nDevicesChecked ++;
                vModel.setKnownDeviceStatus(online ? KnownDeviceListItemViewModel.KnownDeviceStatus.STATUS_ONLINE : KnownDeviceListItemViewModel.KnownDeviceStatus.STATUS_OFFLINE);


                if (nDevicesChecked == deviceVModels.size()) {
                    areDevicesBeingChecked = false;
                    updateActionSearchClickability();
                }

                return;
            }
        }
    }



    private void updateActionSearchClickability() {
        findViewById(R.id.action_search_new).setEnabled(!areDevicesBeingChecked);
    }




    /*********************  Menu implementation  **************************************************/
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
