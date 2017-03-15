package com.alperez.bt_microphone.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.bluetoorh.management.DeviceFounder;
import com.alperez.bt_microphone.bluetoorh.management.OnDeviceFoundListener;
import com.alperez.bt_microphone.bluetoorh.management.impl.DeviceDiscoveryImpl;
import com.alperez.bt_microphone.bluetoorh.management.impl.DeviceFounderImpl;
import com.alperez.bt_microphone.databinding.FragmentDevicesDiscoveryBinding;
import com.alperez.bt_microphone.databinding.GeneralDeviceListItemBinding;
import com.alperez.bt_microphone.model.BlacklistedBtDevice;
import com.alperez.bt_microphone.model.DiscoveredBluetoothDevice;
import com.alperez.bt_microphone.model.ValidBtDevice;
import com.alperez.bt_microphone.ui.viewmodel.BtDeviceViewModel;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public class DiscoverDevicesFragment extends Fragment {

    public interface BluetoothAdapterProvider {
        BluetoothAdapter getBluetoothAdapter();
    }

    public interface OnDeviceSelectionResultListener {
        void onNewDeviceSelected(DiscoveredBluetoothDevice device);
        void onKnowDeviceSelected(ValidBtDevice device);
    }


    private OnDeviceSelectionResultListener resultListener;
    private BluetoothAdapterProvider btProvider;
    private DeviceFounder deviceFounder;

    private FragmentDevicesDiscoveryBinding vBinding;
    private LayoutInflater inflater;




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(DeviceFounderImpl.TAG, "Fragment.onAttach()");
        if (!(context instanceof BluetoothAdapterProvider)) {
            throw new IllegalStateException("A host activity must implement the BluetoothAdapterProvider");
        } else if (!(context instanceof OnDeviceSelectionResultListener)) {
            throw new IllegalStateException("A host activity must implement the OnDeviceSelectionResultListener");
        } else {
            btProvider = (BluetoothAdapterProvider) context;
            resultListener = (OnDeviceSelectionResultListener) context;
        }

        if (deviceFounder == null) {
            deviceFounder = new DeviceFounderImpl(context, new DeviceDiscoveryImpl(context, btProvider.getBluetoothAdapter()));
            deviceFounder.setOnDeviceFoundListener(new OnDeviceFoundListener() {
                @Override
                public void onNewRawDeviceFound(DiscoveredBluetoothDevice device) {
                    addDeviceToUi(device, vBinding.containerNewDevices, vBinding.noItemsNewDevices).setOnClickListener(clickerNewDevices);
                }

                @Override
                public void onValidDeviceFound(ValidBtDevice device) {
                    addDeviceToUi(device, vBinding.containerKnownDevices, vBinding.noItemsKnownDevices).setOnClickListener(clickerKnownDevices);
                }

                @Override
                public void onBlacklistedDeviceFound(BlacklistedBtDevice device) {
                    addDeviceToUi(device, vBinding.containerOtherDevices, vBinding.noItemsOtherDevices);
                }
            });
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DeviceFounderImpl.TAG, "Fragment.onCreate()");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(DeviceFounderImpl.TAG, "Fragment.onCreateView()");
        this.inflater = inflater;
        if (vBinding == null) {
            vBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices_discovery, container, false);
        } else {
            container.removeView(vBinding.getRoot());
        }
        clearUi();
        return vBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(DeviceFounderImpl.TAG, "Fragment.onViewCreated()");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(DeviceFounderImpl.TAG, "Fragment.onStart()");
        deviceFounder.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(DeviceFounderImpl.TAG, "Fragment.onStop()");
        deviceFounder.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DeviceFounderImpl.TAG, "Fragment.onDestroy()");
        deviceFounder.release();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(DeviceFounderImpl.TAG, "Fragment.onDetach()");
    }

    private void clearUi() {
        vBinding.sectionOtherDevices.setVisibility(View.GONE);
        vBinding.containerKnownDevices.removeAllViews();
        vBinding.containerNewDevices.removeAllViews();
        vBinding.containerOtherDevices.removeAllViews();
        vBinding.noItemsOtherDevices.setVisibility(View.VISIBLE);
        vBinding.noItemsKnownDevices.setVisibility(View.VISIBLE);
        vBinding.noItemsNewDevices.setVisibility(View.VISIBLE);
    }


    private View addDeviceToUi(BtDeviceViewModel device, ViewGroup container, View vNoItems) {
        vNoItems.setVisibility(View.GONE);
        GeneralDeviceListItemBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.general_device_list_item, container, true);
        itemBinding.setViewModel(device);
        itemBinding.getRoot().setTag(device);
        return itemBinding.getRoot();
    }


    private View.OnClickListener clickerNewDevices = (v) -> {
        Object tag = v.getTag();
        if ((tag != null) && (tag instanceof DiscoveredBluetoothDevice)) {
            resultListener.onNewDeviceSelected((DiscoveredBluetoothDevice) tag);
        }
    };

    private View.OnClickListener clickerKnownDevices = (v) -> {
        Object tag = v.getTag();
        if ((tag != null) && (tag instanceof ValidBtDevice)) {
            resultListener.onKnowDeviceSelected((ValidBtDevice) tag);
        }
    };
}
