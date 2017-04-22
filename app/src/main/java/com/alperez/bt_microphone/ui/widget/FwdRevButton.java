package com.alperez.bt_microphone.ui.widget;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stanislav.perchenko on 4/21/2017.
 */

public class FwdRevButton extends LockableButton {
    public static final int TIME_INITIAL_TO_REPEAT = 2500;
    public static final int TIME_REPEAT_INTERVAL = 1200;

    public static final int PER_STEP_INCREMENT_TIME = 3;
    public static final int SINGLE_STEP_TIME = 5;

    public interface OnMoveListener {
        void onMove(int nStep, int nSeconds);
    }


    private OnMoveListener onMoveListener;

    public FwdRevButton(Context context) {
        super(context);
    }

    public FwdRevButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FwdRevButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FwdRevButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // Not supported
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        // Not supported
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        // Not supported
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isClickable()) {
            onTouchAction(event.getAction());
        }
        return super.onTouchEvent(event);
    }

    private void onTouchAction(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startTracking();
                break;
            case MotionEvent.ACTION_UP:
                if (nTrackingSteps.get() == 0 && (onMoveListener != null)) {
                    onMoveListener.onMove(0, SINGLE_STEP_TIME);
                }
                dismissTracking();
                break;
            case MotionEvent.ACTION_CANCEL:
                dismissTracking();
                break;
        }
    }




    private Timer mTimer;
    private final AtomicInteger nTrackingSteps = new AtomicInteger(0);


    private void startTracking() {
        dismissTracking();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onNewStep(nTrackingSteps.addAndGet(1));
            }
        }, TIME_INITIAL_TO_REPEAT, TIME_REPEAT_INTERVAL);

    }

    private void dismissTracking() {
        if (mTimer != null) {
            nTrackingSteps.set(0);
            mTimer.cancel();
            mTimer = null;
        }
    }


    private void onNewStep(int step) {
        if (onMoveListener != null) {
            int t = PER_STEP_INCREMENT_TIME * Math.min(step, 16);
            onMoveListener.onMove(step, t);
        }
    }
}
