package com.alperez.bt_microphone.ui.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.databinding.ActivityFinalBinding;
import com.alperez.bt_microphone.ui.viewmodel.MainControlsViewModel;

/**
 * Created by stanislav.perchenko on 3/24/2017.
 */

public class FinalActivity extends AppCompatActivity {

    private ActivityFinalBinding vBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vBinding = DataBindingUtil.setContentView(this, R.layout.activity_final);
        vBinding.setViewModel(new MainControlsViewModel());

        vBinding.actionSettings.setOnClickListener(v -> {});
    }
}
