package com.alperez.bt_microphone.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stanislav.perchenko on 3/10/2017.
 */

public class RunnableSequantialExecuter {
    private Handler workerHandler;
    private volatile boolean released;

    private final List<Runnable> pendingTasks = new ArrayList<>(3);

    /**
     * Enqueue runneables for executing on this thread
     * @param r
     */
    public void enqueueRunnable(@NonNull Runnable r) {
        if (released) throw new IllegalStateException("Already released");
        synchronized (pendingTasks) {
            if (workerHandler != null) {
                workerHandler.post(r);
            } else {
                pendingTasks.add(r);
            }
        }
    }

    public void release() {
        if (!released) {
            synchronized (pendingTasks) {
                workerHandler.getLooper().quit();
            }
        }
    }


    public RunnableSequantialExecuter(){
        new Thread("RunnableSequantialExecuter") {
            @Override
            public void run() {
                Looper.prepare();
                synchronized (pendingTasks) {
                    workerHandler = new Handler(Looper.myLooper());
                    if (pendingTasks.size() > 0) {
                        for (Runnable task : pendingTasks) workerHandler.post(task);
                    }
                }
                Looper.loop();
            }
        }.start();
    }
}
