package com.jason.supervise.core;

import android.os.Process;

/**
 * Created by Jason on 2017/12/5.
 */

public class SamplingThread extends Thread {

    private SamplingHandler samplingHandler;
    private double samplingRate;

    private volatile boolean quit = false;

    public SamplingThread(SamplingHandler samplingHandler, double samplingRate) {
        super("cycleSample");
        this.samplingHandler = samplingHandler;
        this.samplingRate = samplingRate;
    }


    public void quit() {
        quit = true;
        interrupt();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            try {
                samplingHandler.onSamplingEvent();
                Thread.sleep((long) (samplingRate));
            } catch (InterruptedException e) {
                if (quit) {
                    return;
                }
                e.printStackTrace();
            }
        }
    }

    public interface SamplingHandler {

        void onSamplingEvent();
    }

    public boolean isStarted() {
        return getState() != State.NEW;
    }
}
