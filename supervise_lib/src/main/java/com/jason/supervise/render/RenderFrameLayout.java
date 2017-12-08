package com.jason.supervise.render;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 背景透明的FrameLayout，嵌套在页面ContentView的最外层
 * Created by Jason on 2017/12/6.
 */

@SuppressLint("ViewConstructor")
public class RenderFrameLayout extends FrameLayout implements IRenderView {

    private final CallBack mCallback;

    private final String mKey;

    private boolean mIsComplete = false;

    /**
     * 页面创建ContentView时调用此方法获取WrappedContentView.<br/>
     * {@link android.app.Activity#setContentView(int)},<br/>
     *
     * @param key 页面的KEY，每个页面对应唯一的KEY
     */
    public static RenderFrameLayout wrap(String key, View child, CallBack callBack) {
        final RenderFrameLayout wrapper = new RenderFrameLayout(child.getContext(), key, callBack);
        final ViewGroup.LayoutParams childParams = child.getLayoutParams();
        if (childParams != null) {
            wrapper.setLayoutParams(childParams);
        }
        final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        child.setLayoutParams(params);
        wrapper.addView(child);
        return wrapper;
    }

    private RenderFrameLayout(Context context, String key, CallBack callback) {
        super(context);
        super.setBackgroundColor(0);
        mKey = key;
        mCallback = callback;
    }


    @Override
    public void setComplete(boolean complete) {
        this.mIsComplete = complete;
    }

    @Override
    public void setVisibility(int visibility) {

    }

    @Override
    public void setBackground(Drawable background) {

    }

    @Override
    public void setBackgroundColor(int color) {

    }

    @Override
    public void setBackgroundDrawable(Drawable background) {

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!mIsComplete) {
            mIsComplete = mCallback.onDrawEnd(this, mKey);
        }
    }
}
