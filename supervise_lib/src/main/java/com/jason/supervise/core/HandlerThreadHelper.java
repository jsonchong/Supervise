package com.jason.supervise.core;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by Jason on 2017/12/5.
 */

public class HandlerThreadHelper {


    private static HandlerThreadWrapper sLoopThread = new HandlerThreadWrapper("sample");
    private static HandlerThreadWrapper sWorkingThread = new HandlerThreadWrapper("writelog");


    public static Handler getLoopThreadHandler() {
        return sLoopThread.getHandler();
    }

    private static class HandlerThreadWrapper {
        private Handler handler = null;

        HandlerThreadWrapper(String name) {
            HandlerThread handlerThread = new HandlerThread("HertzThread" + name);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        Handler getHandler() {
            return handler;
        }
    }

    public static Handler getWorkingThreadHandler() {
        return sWorkingThread.getHandler();
    }
}
