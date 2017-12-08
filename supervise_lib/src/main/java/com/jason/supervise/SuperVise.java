package com.jason.supervise;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;

import com.jason.supervise.core.SuperViseCore;
import com.jason.supervise.render.RenderManager;

/**
 * Created by Jason on 2017/12/5.
 */

public class SuperVise {

    private static SuperVise sInstance;

    private SuperViseCore superViseCore;

    private boolean callStart;

    private boolean looperStarted = false;

    private RenderManager render;

    /**
     * 获取Hertz单例实例，提供外部调用性能采集和监控的接口方法
     *
     * @return
     */
    public static SuperVise getInstance() {
        if (sInstance == null) {
            synchronized (SuperVise.class) {
                if (sInstance == null) {
                    sInstance = new SuperVise();
                }
            }
        }
        return sInstance;
    }

    public synchronized void init(final SuperviseConfiguration initConfiguration) {
        initSuperVise(initConfiguration);
    }

    private void initSuperVise(SuperviseConfiguration configuration) {
        superViseCore = new SuperViseCore(configuration, true);
        render = RenderManager.getInstance();
        render.init(configuration);
        realStart();
    }

    /**
     * 开始性能采集和监控
     */
    public void start() {
        realStart();
    }

    private void realStart() {
        if (!looperStarted && superViseCore != null) {
            looperStarted = true;
            superViseCore.start();
        }
    }

    /**
     * 页面创建ContentView时调用此方法获取WrappedContentView.<br/>
     *
     * @param key 页面的KEY，每个页面对应唯一的KEY
     */
    public View wrapWithTrackView(String key, View view) {
        if (render == null) return view;
        return render.wrap(key, view);
    }

    public View wrapWithTrackView(String key, LayoutInflater inflater, @LayoutRes int resource) {
        if (render == null) return inflater.inflate(resource, null);
        return render.wrap(key, inflater.inflate(resource, null));
    }

    /**
     * 获取用于track的当前timestamp
     */
    public static long getTrackTimestamp() {
        return RenderManager.timestamp();
    }

    /**
     * 记录页面创建事件
     */
    public void trackPageCreateEvent(String key) {
        if (render != null) {
            render.onPageCreate(key, getTrackTimestamp());
        }
    }

    /**
     * API请求开始
     */
    public void trackApiLoadStartEvent(String relativeUrl) {
        if (render != null)
            render.onApiLoadStart(relativeUrl, getTrackTimestamp());
    }

    /**
     * API请求结束
     */
    public void trackApiLoadEndEvent(String relativeUrl) {
        if (render != null)
            render.onApiLoadEnd(relativeUrl, getTrackTimestamp());
    }

    /**
     * 记录页面数据加载开始事件
     */
    public void trackLoadStartEvent(String key) {
        if (render != null)
            render.onPageLoadStart(key, getTrackTimestamp());
    }

    /**
     * 记录页面数据加载结束事件
     */
    public void trackLoadEndEvent(String key) {
        if (render != null)
            render.onPageLoadEnd(key, getTrackTimestamp());
    }

    public void appendRenderConfig(String config) {
        if (render != null) {
            render.initRenderJsonConfig(config);
        }
    }
}
