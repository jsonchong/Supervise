package com.jason.supervise.core;

import android.os.Looper;
import android.util.Log;

import com.jason.supervise.SuperviseConfiguration;
import com.jason.supervise.entity.SuperViseEntity;
import com.jason.supervise.sample.SampleChain;

/**
 * Created by Jason on 2017/12/5.
 */

public class SuperViseCore implements SamplingThread.SamplingHandler {

    private static final String TAG = "SuperViseCore";
    private SuperviseConfiguration configuration;
    private SampleChain samplerChain;
    private SamplingThread samplingThread;
    private SuperViseEntity perfEntity;
    private boolean mDisableBlockDetect;


    public SuperViseCore(SuperviseConfiguration configuration, boolean disableBlockDetect) {
        this.configuration = configuration;
        this.mDisableBlockDetect = disableBlockDetect;
        this.samplerChain = SampleChain.createSamplerChain(configuration);
        this.samplingThread = new SamplingThread(this, configuration.samplingRate);
    }

    public void start() {
        Log.d(TAG, "start: sample");
        if (!mDisableBlockDetect)
            //Looper.getMainLooper().setMessageLogging(mainLooperPrinter);
            //判断线程是否start过,用来修复小米手机兼容性问题
            if (samplingThread.isStarted()) {
                samplingThread = new SamplingThread(this, configuration.samplingRate);
            }
        samplingThread.start();
        samplerChain.start();
    }

    public void stop() {
        Log.d(TAG, "stop: sample");
        Looper.getMainLooper().setMessageLogging(null);
        samplingThread.quit();
        samplerChain.stop();
        HandlerThreadHelper.getWorkingThreadHandler().removeCallbacksAndMessages(null);
    }

    @Override
    public void onSamplingEvent() {
        final SuperViseEntity entity = new SuperViseEntity();
        HandlerThreadHelper.getWorkingThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                samplerChain.doSample(entity);
                Log.e(TAG, entity.toString());
            }
        });
    }
}
