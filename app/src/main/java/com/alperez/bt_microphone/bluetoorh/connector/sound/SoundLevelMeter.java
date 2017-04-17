package com.alperez.bt_microphone.bluetoorh.connector.sound;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * Created by stanislav.perchenko on 4/11/2017.
 */

public class SoundLevelMeter {

    private OnSoundLevelListener callback;
    private Handler uiHandler;

    public SoundLevelMeter(@NonNull OnSoundLevelListener callback) {
        this.callback = callback;
        uiHandler = new Handler(Looper.getMainLooper());
    }


    private int prescCounter = 0;
    private int prescValue = 0;
    public void submitSamples(byte[] samples, int offset, int len) {

        int smp, smpAbs;
        for (int i=0; i<len; i++) {
            smp = samples[offset ++];
            if (smp >= 0) {
                smp = 127 - smp;
            } else {
                smp = -129 - smp;
            }

            //Log.d("Level-Meter", smp+" -> "+smpAbs);

            updatePeakDetector(smp);

            prescValue += smp;
            if ((++ prescCounter) >= 4) {
                prescCounter = 0;
                eveluatePrescaledValue( Math.round((float)prescValue/4f) );
                prescValue = 0;
            }
        }
    }


    private long rmsAccum = 0;
    private int rmsCounter = 0;
    private void eveluatePrescaledValue(int v) {
        //Log.d("Level-Meter", "Prescaled = "+v);

        rmsAccum += v * v;
        if ((++ rmsCounter) >= 50) {
            final float rmsV = (float) Math.sqrt(rmsAccum / 50.0);
            //Log.d("Level-Meter", "--->> RMS = "+rmsV);
            rmsAccum = 0;
            rmsCounter = 0;
            notifyResult(filterRmsValue(rmsV));
        }

    }


    public static final int RMS_FILTER_LEN = 3;
    float[] rmsDelayLine = new float[RMS_FILTER_LEN];
    int rmsFilterIndex = 0;
    private float filterRmsValue(float iRms) {

        rmsDelayLine[rmsFilterIndex ++] = iRms;
        if (rmsFilterIndex >= RMS_FILTER_LEN) rmsFilterIndex = 0;
        float sum = 0;
        for (int i=0; i<RMS_FILTER_LEN; i++) {
            sum += rmsDelayLine[i];
        }
        return sum / RMS_FILTER_LEN;
    }



    private void notifyResult(final float rms) {
        final int peak = getPeak();
        resetPeakDetector();
        uiHandler.post(()->callback.onLevelUpdated(rms, peak));
    }





    /*********************************  Peak detector section  ************************************/
    private int peak_hi, peak_lo;

    private void updatePeakDetector(int level) {
        int levAbs = Math.abs(level);
        if (levAbs > peak_hi) {
            peak_lo = peak_hi;
            peak_hi = levAbs;
        } else if (levAbs > peak_lo) {
            peak_lo = levAbs;
        }



        //Log.d("Level-Meter", String.format("level=%d, p_lo=%d, p_hi=%d", level, peak_lo, peak_hi));
    }

    private int getPeak() {
        return peak_lo;
    }

    private void resetPeakDetector() {
        peak_hi = peak_lo = 0;
    }


}
