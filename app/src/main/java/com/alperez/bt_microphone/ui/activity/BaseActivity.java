package com.alperez.bt_microphone.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.alperez.bt_microphone.R;
import com.alperez.bt_microphone.ui.IFullScreenProgress;
import com.alperez.bt_microphone.ui.Layout;

import java.lang.annotation.Annotation;

/**
 * Created by stanislav.perchenko on 3/12/2017.
 */

public abstract class BaseActivity extends AppCompatActivity implements IFullScreenProgress {
    private ProgressDialog mDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //----  Set content by annotation if presented  ----
        Layout layAnnot = getClass().getAnnotation(Layout.class);
        if (layAnnot != null) {
            setContentView(layAnnot.value());
        }
    }



    protected void setupToolbar() {
        ActionBar ab = getSupportActionBar();
        if (ab == null) {
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            ab = getSupportActionBar();
        }
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(getActivityTitle());
            ab.setSubtitle(getActivitySubtitle());
        }

        //--- This is the ugly hack to support setting title multiple times when the Toolbar
        //--- is in the CollapsingToolbarLayout
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        if (tb != null) {
            ViewParent vp = tb.getParent();
            if ((vp != null) && (vp instanceof CollapsingToolbarLayout)) {
                ((CollapsingToolbarLayout) vp).setTitle(getActivityTitle());
            }
        }
    }

    protected final void updateActivityTitleAndSubtitle() {
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setTitle(getActivityTitle());
            ab.setSubtitle(getActivitySubtitle());
        }
    }


    protected abstract String getActivityTitle();
    protected abstract String getActivitySubtitle();









    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean result = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            if (w == null) return result;

            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom())) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return result;
    }



    @Override
    public void showFullScreenProgress(String message) {
        if (mDialog == null) {
            mDialog = new ProgressDialog(this);
            mDialog.setIndeterminate(true);
            mDialog.setTitle(message);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
        }
        mDialog.show();
    }

    @Override
    public void dismissFullScreenProgress() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

}
