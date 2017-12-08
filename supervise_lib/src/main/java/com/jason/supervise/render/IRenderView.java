package com.jason.supervise.render;

/**
 * Created by Jason on 2017/12/6.
 */

public interface IRenderView {

    interface CallBack {
        boolean onDrawEnd(IRenderView renderView, String key);
    }

    void setComplete(boolean complete);
}
