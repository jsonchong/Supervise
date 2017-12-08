package com.jason.supervise.render;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jason.supervise.entity.SuperViseEntity;

/**
 * Created by Jason on 2017/12/8.
 */

public class RenderRecord {
    public interface Callback {
        void onRecordEnd(RenderRecord record, String key);
    }

    private static final String LOG_PREFIX = "RenderRecord: ";

    @NonNull
    private final String mKey;

    @Nullable
    private final PageConfig mConfig;

    @Nullable
    private Callback mCallback;

    /**
     * 页面是否需要加载数据
     */
    private boolean mNeedLoadData = true;
    /**
     * 页面创建
     */
    private long mCreateTime;
    /**
     * 首次渲染结束
     */
    private long mInitialDrawEndTime;
    /**
     * 加载开始
     */
    private long mLoadStartTime;
    /**
     * 加载完成
     */
    private long mLoadEndTime;
    /**
     * 加载完成后首次渲染结束
     */
    private long mFinalDrawEndTime;

    private long mRecordStopTime;

    private long mRecordResumeTime;

    private boolean isInBackground;

    public RenderRecord(@NonNull String key, @Nullable PageConfig config) {
        mKey = key;
        mConfig = config;
        if (mConfig != null) {
            mConfig.reset();
        }
    }

    public RenderRecord setCallback(@NonNull Callback callback) {
        mCallback = callback;
        return this;
    }

    @NonNull
    public String getKey() {
        return mKey;
    }

    @Nullable
    public PageConfig getConfig() {
        return mConfig;
    }

    public boolean needLoadData() {
        if (getConfig() != null) {
            return getConfig().needLoadData();
        }
        return mNeedLoadData;
    }

    public void setNeedLoadData(boolean need) {
        mNeedLoadData = need;
    }

    /**
     * create只统计第一个时间戳
     */
    public void onCreate(long timestamp) {
        if (mCreateTime == 0) {
            mCreateTime = timestamp;
            Log.d("onCreate", timestamp + "");
        } else {
            Log.d("onCreate ignored", mCreateTime + "");
        }
    }


    /**
     * 页面销毁
     */
    public void onDestroy() {
        onRecordEnd();
    }

    /**
     * 记录被移除
     */
    public void onRemove() {

    }

    /**
     * loadStart统计最小的时间戳
     */
    public void onLoadStart(long timestamp) {
        if (mLoadStartTime == 0 || timestamp < mLoadStartTime) {
            mLoadStartTime = timestamp;
            Log.d("onLoadStart", timestamp + "");
        } else {
            Log.d("onLoadStart ignored", mLoadStartTime + "");
        }
    }

    /**
     * loadEnd统计最大的时间戳
     */
    public void onLoadEnd(long timestamp) {
        // 时间戳应该大于0；如果未设置则直接设置；如果新的时间戳更大，则以新的为准
        if (timestamp > 0 && (mLoadEndTime == 0 || timestamp > mLoadEndTime)) {
            mLoadEndTime = timestamp;
            if (isLoadFinish() && isInBackground) {
                this.mRecordStopTime = timestamp;
            }
            Log.d("onLoadEnd", timestamp + "");
        } else {
            Log.d("onLoadEnd ignored", mLoadEndTime + "");
        }
    }

    /**
     * @return isRecordEnd
     */
    public boolean onDrawEnd(long timestamp) {
        Log.d("onDrawEnd", timestamp + "");
        if (mCreateTime > 0) {
            timestamp -= getInBackgroundTime();
            if (mLoadEndTime > 0) {
                onFinalRenderEnd(timestamp);
                if (getConfig() == null || getConfig().isLoadFinish()) {
                    onRecordEnd();
                    return true;
                }
            } else if (mInitialDrawEndTime == 0) {
                onInitialRenderEnd(timestamp);
                if (!needLoadData()) {
                    onRecordEnd();
                    return true;
                }
            }
        }
        return false;
    }

    private void onInitialRenderEnd(long timestamp) {
        mInitialDrawEndTime = timestamp;
        Log.d("onInitialRenderEnd", timestamp + "");
    }

    private void onFinalRenderEnd(long timestamp) {
        mFinalDrawEndTime = timestamp;
        Log.d("onFinalRenderEnd", timestamp + "");
    }

    /**
     * 完成页面的记录
     */
    private boolean onRecordEnd() {
//        L.i(LOG_PREFIX + "onRecordEnd");
        if (mCallback != null) {
            mCallback.onRecordEnd(this, mKey);
        }
        if (getCreateTime() <= 0) {
            Log.d(LOG_PREFIX, "CreateTime is invalid.");
            return false;
        }
        if (getInitialDrawEndTime() <= 0) {
            Log.d(LOG_PREFIX, "InitialRenderEndTime is invalid.");
            return false;
        }
        if (needLoadData()) {
            if (getLoadStartTime() <= 0) {
                Log.d(LOG_PREFIX, "LoadStartTime is invalid.");
                return false;
            }
            if (getLoadEndTime() <= 0) {
                Log.d(LOG_PREFIX, "LoadEndTime is invalid.");
                return false;
            }
            if (getFinalDrawEndTime() <= 0) {
                Log.d(LOG_PREFIX, "FinalRenderEndTime is invalid.");
                return false;
            }
        }
        return true;
    }

    private long getCreateTime() {
        return mCreateTime;
    }

    private long getLoadStartTime() {
        return mLoadStartTime;
    }

    private long getLoadEndTime() {
        return mLoadEndTime;
    }

    private long getInitialDrawEndTime() {
        return mInitialDrawEndTime;
    }

    private long getFinalDrawEndTime() {
        if (mFinalDrawEndTime == 0) { // 如果没有记录FinalRenderEndTime，则取LoadEndTime
            return mLoadEndTime;
        }
        return mFinalDrawEndTime;
    }

    public long calcFullTime() {
        return needLoadData() ? getFinalDrawEndTime() - getCreateTime() : calcInitialDrawTime();
    }

    public long calcLoadTime() {
        return getLoadEndTime() - getLoadStartTime();
    }

    public long calcInitialDrawTime() {
        return getInitialDrawEndTime() > 0 ? getInitialDrawEndTime() - getCreateTime() : 0;
    }

    public long calcRefreshDrawTime() {
        return getFinalDrawEndTime() - getLoadEndTime();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SuperViseEntity.SPLIT_MARK).append(SuperViseEntity.SEPARATOR);
        sb.append("|" + SuperViseEntity.dateFormat.format(System.currentTimeMillis())).append(SuperViseEntity.SEPARATOR);
        sb.append("|当前页面测速:").append(SuperViseEntity.SEPARATOR);
        sb.append(mKey).append(SuperViseEntity.SEPARATOR);
        sb.append("|T(页面加载总时间):").append(calcFullTime()).append("ms").append(SuperViseEntity.SEPARATOR);
        sb.append("|T1(ui首次渲染时间):").append(calcInitialDrawTime()).append("ms").append(SuperViseEntity.SEPARATOR);
        if (needLoadData()) {
            sb.append("|T2(数据加载时间):").append(calcLoadTime()).append("ms").append(SuperViseEntity.SEPARATOR);
            sb.append("|T3(ui重新渲染时间):").append(calcRefreshDrawTime()).append("ms").append(SuperViseEntity.SEPARATOR);
        }
        sb.append(SuperViseEntity.SPLIT_MARK);
        return sb.toString();
    }

    public void onActivityStopTime(long time) {
        isInBackground = true;
        if (!isLoadFinish() || time < mLoadEndTime) return;
        this.mRecordStopTime = time;
    }

    public void onActivityResumeTime(long time) {
        isInBackground = false;
        this.mRecordResumeTime = time;
    }

    public long getInBackgroundTime() {
        if (isLoadFinish() && mRecordStopTime > 0 && mRecordResumeTime > mRecordStopTime) {
            return mRecordResumeTime - mRecordStopTime;
        }
        return 0;
    }

    private boolean isLoadFinish() {
        return getConfig() == null || getConfig().isLoadFinish();
    }
}
