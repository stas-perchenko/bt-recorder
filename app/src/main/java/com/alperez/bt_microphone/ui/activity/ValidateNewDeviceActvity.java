package com.alperez.bt_microphone.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.management.DiscoveredDeviceVerifier;
import com.alperez.bt_microphone.databinding.ActivityValidateNewDeviceBinding;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.utils.Callback;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 4/26/2017.
 */

public class ValidateNewDeviceActvity extends BaseActivity {
    public static final String ARG_DISCOVERED_DEVICE = "new_device";
    public static final String RESULT_VALID_DEVICE = "device";


    public static void startForResult(Activity parent, int requestCode, DiscoveredBluetoothDevice device) {
        Intent intent = new Intent(parent, ValidateNewDeviceActvity.class).putExtra(ARG_DISCOVERED_DEVICE, device);
        parent.startActivityForResult(intent, requestCode);
    }




    private ActivityValidateNewDeviceBinding vBinding;

    private DiscoveredDeviceVerifier verificationController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vBinding = DataBindingUtil.setContentView(this, R.layout.activity_validate_new_device);

        setupToolbar();

        vBinding.setClickerApply((v) -> {
            String name = vBinding.edtDevName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Name the device please.", Toast.LENGTH_SHORT).show();
            } else {
                onApplyWithDeviceName(name);
            }
        });

        vBinding.edtDevName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                vBinding.btnSave.setEnabled(s.length() > 3);
            }
        });


        DiscoveredBluetoothDevice argDevice = getIntent().getParcelableExtra(ARG_DISCOVERED_DEVICE);
        verificationController = DiscoveredDeviceVerifier.createForDevice(argDevice).execute(new Callback<ValidDeviceDbModel>() {
            @Override
            public void onComplete(ValidDeviceDbModel result) {
                verificationController = null;
                vBinding.setDevice(result);
            }

            @Override
            public void onError(Throwable error) {
                verificationController = null;
                onFinishWithError(error.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (verificationController != null) verificationController.release();
    }


    @Override
    protected String getActivityTitle() {
        return "DEVICE VALIDATION";
    }

    @Override
    protected String getActivitySubtitle() {
        return null;
    }

    private void onFinishWithError(String reason) {
        new AlertDialog.Builder(this).setTitle("Verification error").setMessage(reason).setPositiveButton("Close", (dialog, which) -> {
            setResult(RESULT_CANCELED);
            finish();
        }).setCancelable(false).show();
    }

    private void onApplyWithDeviceName(String devName) {
        ValidDeviceDbModel finalDevice = vBinding.getDevice().withUserDefinedName(devName);
        saveNamedDevice(finalDevice);

        setResult(RESULT_OK, new Intent().putExtra(RESULT_VALID_DEVICE, finalDevice));
        finish();
    }

    private ValidDeviceDbModel saveNamedDevice(ValidDeviceDbModel namedDevice) {
        ValidDeviceDbModel timedDev = namedDevice.withTimeDiscovered(new Date());
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            db.insertValidDevice(timedDev);
            return timedDev;
        } finally {
            db.close();
        }
    }
}
