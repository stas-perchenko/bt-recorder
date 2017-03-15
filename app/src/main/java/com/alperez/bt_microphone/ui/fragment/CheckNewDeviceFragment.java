package com.alperez.bt_microphone.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.management.DiscoveredDeviceVerifier;
import com.alperez.bt_microphone.databinding.FragmentCheckNewDeviceBinding;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.storage.DatabaseAdapter;
import com.alperez.bt_microphone.ui.IFullScreenProgress;
import com.alperez.bt_microphone.utils.Callback;

import junit.framework.TestListener;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public class CheckNewDeviceFragment extends Fragment {

    public interface OnDeviceFerifiedListener {
        void onDeveiceVerified(ValidBtDevice device);
        void onErrorVerification(DiscoveredBluetoothDevice device);
    }

    public static CheckNewDeviceFragment newInstance(DiscoveredBluetoothDevice newDevice) {
        Bundle args = new Bundle();
        args.putParcelable("device", newDevice);
        CheckNewDeviceFragment f = new CheckNewDeviceFragment();
        f.setArguments(args);
        return f;
    }


    private DiscoveredBluetoothDevice argDevice;
    private IFullScreenProgress fullScreenProgress;
    private OnDeviceFerifiedListener resultListener;


    private DiscoveredDeviceVerifier controller;


    private FragmentCheckNewDeviceBinding vBinding;
    private ValidBtDevice resultDevice;




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof IFullScreenProgress)) {
            throw new IllegalStateException("A host activity must implement the IFullScreenProgress");
        } else if (!(context instanceof OnDeviceFerifiedListener)) {
            throw new IllegalStateException("A host activity must implement the OnDeviceFerifiedListener");
        } else {
            fullScreenProgress = (IFullScreenProgress) context;
            resultListener = (OnDeviceFerifiedListener) context;
        }
    }

    public void setArgDevice(DiscoveredBluetoothDevice device) {
        argDevice = device;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArgDevice(getArguments().getParcelable("device"));

        resultDevice = null;
        //fullScreenProgress.showFullScreenProgress("Check device...");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (vBinding == null) {
            vBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_check_new_device, container, false);
            vBinding.setClickerApply(v -> onApplyClicked());
        } else {
            container.removeView(vBinding.getRoot());
        }

        vBinding.checkStage1.getBackground().setLevel(0);
        vBinding.checkStage2.getBackground().setLevel(0);
        vBinding.checkStage3.getBackground().setLevel(0);
        vBinding.progressStage1.setVisibility(View.INVISIBLE);
        vBinding.progressStage2.setVisibility(View.INVISIBLE);
        vBinding.progressStage3.setVisibility(View.INVISIBLE);

        return vBinding. getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (controller != null) {
            controller.release();
        }
        controller = createVerificationController(argDevice);
        controller.start();
        if (resultDevice != null) {
            vBinding.setDevice(resultDevice);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.release();
            controller = null;
        }
    }











    private DiscoveredDeviceVerifier createVerificationController(final DiscoveredBluetoothDevice device) {
        return DiscoveredDeviceVerifier.createForDevice(device, new DiscoveredDeviceVerifier.OnStageListener() {
            @Override
            public void onStage1Start() {
                vBinding.progressStage1.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStage1Complete() {
                vBinding.progressStage1.setVisibility(View.INVISIBLE);
                vBinding.checkStage1.getBackground().setLevel(1);
            }

            @Override
            public void onStage2Start() {
                vBinding.progressStage2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStage2Complete() {
                vBinding.progressStage2.setVisibility(View.INVISIBLE);
                vBinding.checkStage2.getBackground().setLevel(1);
            }

            @Override
            public void onStage3Start() {
                vBinding.progressStage3.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStage3Complete() {
                vBinding.progressStage3.setVisibility(View.INVISIBLE);
                vBinding.checkStage3.getBackground().setLevel(1);
            }
        }).withResultCallback(new Callback<ValidBtDevice>() {
            @Override
            public void onComplete(ValidBtDevice result) {
                controller = null;
                //fullScreenProgress.dismissFullScreenProgress();
                populateValidDevice(result);
            }

            @Override
            public void onError(Throwable error) {
                controller = null;
                //fullScreenProgress.dismissFullScreenProgress();
                resultListener.onErrorVerification(device);
            }
        });
    }

    private void populateValidDevice(ValidBtDevice result) {
        resultDevice = result;
        if (vBinding != null) {
            vBinding.setDevice(result);
        }
    }






    /**********************************************************************************************/
    /*******************  Store Verified device section  *****************************************/
    private void onApplyClicked() {
        String name = vBinding.editUserDefinedName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity(), "Please, name the device!", Toast.LENGTH_SHORT).show();
        } else {
            ValidBtDevice finalDevice = saveNamedDevice(vBinding.getDevice().withUserDefinedName(name));
            resultListener.onDeveiceVerified(finalDevice);
        }
    }

    private ValidBtDevice saveNamedDevice(ValidBtDevice namedDevice) {
        ValidBtDevice timedDev = namedDevice.withTimeDiscovered(new Date());
        DatabaseAdapter db = new DatabaseAdapter();
        try {
            db.insertValidDevice(timedDev);
            return timedDev;
        } finally {
            db.close();
        }
    }
}
