package com.alperez.bt_microphone.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.alperez.bt_microphone.R;

/**
 * Created by stanislav.perchenko on 3/24/2017.
 */

public class LockableButton extends TextView {

    private static final int[] STATE_LOCKED = {R.attr.state_locked};

    private boolean drawableLockInit;
    private Drawable drawableLock;
    private int drLockMargin;
    private boolean locked;

    public LockableButton(Context context) {
        super(context);
    }

    public LockableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public LockableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LockableButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LockableToggleButton, defStyleAttr, defStyleRes);
        try {
            drawableLock = a.getDrawable(R.styleable.LockableToggleButton_drawable_lock);
            locked = a.getBoolean(R.styleable.LockableToggleButton_state_locked, locked);
            drLockMargin = a.getDimensionPixelSize(R.styleable.LockableToggleButton_drawable_lock_margin, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setClickable(!locked);
    }

    public void lock() {
        if (!locked) {
            locked = true;
            setClickable(!locked);
            refreshDrawableState();
        }
    }

    public void unlock() {
        if (locked) {
            locked = false;
            setClickable(!locked);
            refreshDrawableState();
        }
    }

    public void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;
            setClickable(!locked);
            refreshDrawableState();
        }
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drState = super.onCreateDrawableState(extraSpace + 1);
        if (locked) {
            mergeDrawableStates(drState, STATE_LOCKED);
        }
        return drState;
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (drawableLock != null && drawableLock.isStateful() && drawableLock.setState(getDrawableState())) {
            invalidate(drawableLock.getBounds());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawableLockInit = false;
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (drawableLock != null) {
            if (!drawableLockInit) {
                drawableLockInit = true;
                int top = Math.round((float)(getHeight() - drawableLock.getIntrinsicHeight()) / 2f);
                drawableLock.setBounds(drLockMargin, top, drLockMargin+drawableLock.getIntrinsicWidth(), top+drawableLock.getIntrinsicHeight());
            }
            drawableLock.draw(canvas);
        }
    }
}
