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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArgDevice(getArguments().getParcelable("device"));
    }

    public void setArgDevice(DiscoveredBluetoothDevice device) {
        argDevice = device;
    }

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


        resultDevice = null;
        fullScreenProgress.showFullScreenProgress("Check device...");
        controller = DiscoveredDeviceVerifier.createForDevice(argDevice).withResultCallback(new Callback<ValidBtDevice>() {
            @Override
            public void onComplete(ValidBtDevice result) {
                controller = null;
                fullScreenProgress.dismissFullScreenProgress();
                populateValidDevice(result);
            }

            @Override
            public void onError(Throwable error) {
                controller = null;
                fullScreenProgress.dismissFullScreenProgress();
                resultListener.onErrorVerification(argDevice);
            }
        }).start();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        controller.release();
        controller = null;
    }


    private FragmentCheckNewDeviceBinding vBinding;
    private ValidBtDevice resultDevice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (vBinding == null) {
            vBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_check_new_device, container, false);
            vBinding.setClickerApply(v -> onApplyClicked());
        } else {
            container.removeView(vBinding.getRoot());
        }
        if (resultDevice != null) {
            vBinding.setDevice(resultDevice);
        }
        return vBinding. getRoot();
    }

    private void populateValidDevice(ValidBtDevice result) {
        resultDevice = result;
        if (vBinding != null) {
            vBinding.setDevice(result);
        }
    }



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
