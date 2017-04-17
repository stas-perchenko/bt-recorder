package com.alperez.bt_microphone.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by stanislav.perchenko on 4/10/2017.
 */

public class SoundLevelView extends View {
    public static final float THRESHOLD_LO = 0.6f;
    public static final float THRESHOLD_HI = 0.85f;

    public static final int COLOR_1_INACTIVE = Color.parseColor("#000000");
    public static final int COLOR_1_ACTIVE = Color.parseColor("#28DB18");
    public static final int COLOR_2_INACTIVE = Color.parseColor("#000000");
    public static final int COLOR_2_ACTIVE = Color.parseColor("#F6FF00");
    public static final int COLOR_3_INACTIVE = Color.parseColor("#000000");
    public static final int COLOR_3_ACTIVE = Color.parseColor("#F50C10");

    private int level;
    private int maxLevel;

    private Paint paint1Active;
    private Paint paint1Inactive;
    private Paint paint2Active;
    private Paint paint2Inactive;
    private Paint paint3Active;
    private Paint paint3Inactive;

    public SoundLevelView(Context context) {
        this(context, null);
    }

    public SoundLevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SoundLevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SoundLevelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        paint1Active = new Paint();
        paint1Active.setStyle(Paint.Style.FILL);
        paint1Active.setAntiAlias(true);
        paint1Active.setColor(COLOR_1_ACTIVE);

        paint1Inactive = new Paint();
        paint1Inactive.setStyle(Paint.Style.FILL);
        paint1Inactive.setAntiAlias(true);
        paint1Inactive.setColor(COLOR_1_INACTIVE);

        paint2Active = new Paint();
        paint2Active.setStyle(Paint.Style.FILL);
        paint2Active.setAntiAlias(true);
        paint2Active.setColor(COLOR_2_ACTIVE);

        paint2Inactive = new Paint();
        paint2Inactive.setStyle(Paint.Style.FILL);
        paint2Inactive.setAntiAlias(true);
        paint2Inactive.setColor(COLOR_2_INACTIVE);

        paint3Active = new Paint();
        paint3Active.setStyle(Paint.Style.FILL);
        paint3Active.setAntiAlias(true);
        paint3Active.setColor(COLOR_3_ACTIVE);

        paint3Inactive = new Paint();
        paint3Inactive.setStyle(Paint.Style.FILL);
        paint3Inactive.setAntiAlias(true);
        paint3Inactive.setColor(COLOR_3_INACTIVE);
    }



    public void setLevel(int level) {
        this.level = Math.min(maxLevel, level);
        updateLevelPx();
        invalidate();
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        if (level > maxLevel) {
            level = maxLevel;
        }
        updateLevelPx();
        invalidate();
    }


    int thresholdLoPx;
    int thresholdHiPx;
    int widthPx = -1;
    int levelPx = 0;



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthPx = getMeasuredWidth();
        thresholdLoPx = Math.round(THRESHOLD_LO * widthPx);
        thresholdHiPx = Math.round(THRESHOLD_HI * widthPx);
        updateLevelPx();
    }

    private void updateLevelPx() {
        if (widthPx > 0 && maxLevel > 0) {
            levelPx = Math.round((float)(widthPx * level) / maxLevel);
        } else {
            levelPx = 0;
        }
        //Log.d("Level-Meter", String.format("Update level W=%d, maxLev=%d, level=%d, levPx=%d", widthPx, maxLevel, level, levelPx));
    }




    @Override
    protected void onDraw(Canvas c) {
        final int h = getHeight();
        final int w = getWidth();
        if (levelPx <= thresholdLoPx) {
            c.drawRect(0, 0, levelPx, h, paint1Active);
            if (levelPx < thresholdLoPx) {
                c.drawRect(levelPx, 0, thresholdLoPx, h, paint1Inactive);
            }
            c.drawRect(thresholdLoPx, 0, thresholdHiPx, h, paint2Inactive);
            c.drawRect(thresholdHiPx, 0, w, h, paint3Inactive);

        } else if (levelPx <= thresholdHiPx) {
            c.drawRect(0, 0, thresholdLoPx, h, paint1Active);
            c.drawRect(thresholdLoPx, 0, levelPx, h, paint2Active);
            if (levelPx < thresholdHiPx) {
                c.drawRect(levelPx, 0, thresholdHiPx, h, paint2Inactive);
            }
            c.drawRect(thresholdHiPx, 0, w, h, paint3Inactive);
        } else {
            c.drawRect(0, 0, thresholdLoPx, h, paint1Active);
            c.drawRect(thresholdLoPx, 0, thresholdHiPx, h, paint2Active);
            c.drawRect(thresholdHiPx, 0, levelPx, h, paint3Active);
            if (levelPx < w) {
                c.drawRect(levelPx, 0, w, h, paint3Inactive);
            }
        }
    }
}