package com.alperez.bt_microphone.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.management.DeviceOnlineChecker;
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


    private LinearLayout vDeviceContainer;

    private List<KnownDeviceListItemViewModel> deviceVModels;

    private DeviceOnlineChecker deviceOnlineChecker;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_known_device_list);
        vDeviceContainer = (LinearLayout) findViewById(R.id.container);
        findViewById(R.id.action_search_new).setOnClickListener((v) -> {/* Open activity to search new devices */});

        setupToolbar();

        deviceVModels = selectAllKnownDevices();

        createDeviceViewItems(deviceVModels);


        deviceOnlineChecker = new DeviceOnlineChecker(this);
        for (KnownDeviceListItemViewModel vModel : deviceVModels) {
            vModel.setKnownDeviceStatus(KnownDeviceListItemViewModel.KnownDeviceStatus.STATUS_CHECKING);
            deviceOnlineChecker.checkDeviceAsync(vModel.getValidDevice());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceOnlineChecker.release();
    }

    @Override
    protected String getActivityTitle() {
        return null;
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
                result.add(new KnownDeviceListItemViewModel(dev));
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

                //TODO Remove this after testing !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if (++ nDevicesChecked > 1) throw new RuntimeException("Device address conflict");
                vModel.setKnownDeviceStatus(online ? KnownDeviceListItemViewModel.KnownDeviceStatus.STATUS_ONLINE : KnownDeviceListItemViewModel.KnownDeviceStatus.STATUS_OFFLINE);
                return;
            }
        }
    }
}
