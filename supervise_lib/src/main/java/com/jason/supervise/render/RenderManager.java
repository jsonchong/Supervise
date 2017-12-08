package com.jason.supervise.render;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.jason.supervise.SuperviseConfiguration;
import com.jason.supervise.core.HandlerThreadHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jason on 2017/12/8.
 */

public class RenderManager {

    private static RenderManager sInstance;

    private static final String LOG_PREFIX = "RenderManager:  ";

    /**
     * 用于保存当前所有活动页面的记录。同时可能有多个页面处于活动状态。
     */
    @NonNull
    private final ConcurrentHashMap<String, RenderRecord> mActivePage = new ConcurrentHashMap<>(1);

    /**
     * 页面配置信息
     */
    @NonNull
    private final HashMap<String, PageConfig> mConfig = new HashMap<>();

    private final IRenderView.CallBack mDrawCallback = new IRenderView.CallBack() {
        @Override
        public boolean onDrawEnd(IRenderView renderView, String key) {
            final RenderRecord record = getRenderRecord(key, false);
            boolean isRecordEnd = true;
            if (record != null) {
                isRecordEnd = record.onDrawEnd(timestamp());
                if (isRecordEnd && isHomepage(key)) {
                    //TODO onHomepageDrawEnd()
                }
            }
            return isRecordEnd;
        }
    };


    private final RenderRecord.Callback mRecordCallback = new RenderRecord.Callback() {
        @Override
        public void onRecordEnd(RenderRecord record, String key) {
            removeRecord(key);
            Log.e("renderRecord: ", record.toString());
            //TODO interceptorChain去拦截onRenderEvent(record)
        }
    };

    public static RenderManager getInstance() {
        if (sInstance == null) {
            synchronized (RenderManager.class) {
                if (sInstance == null) {
                    sInstance = new RenderManager();
                }
            }
        }
        return sInstance;
    }

    public void init(final SuperviseConfiguration configuration) {
        initRenderJsonConfig(configuration.renderJsonConfig);
    }

    public void initRenderJsonConfig(final String renderJsonConfig) {
        if (!TextUtils.isEmpty(renderJsonConfig)) {
            HandlerThreadHelper.getWorkingThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        readConfigFromJson(new JSONArray(renderJsonConfig));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private boolean readConfigFromJson(JSONArray array) {
        Log.d(LOG_PREFIX, "readConfigFromJson");
        if (array != null && array.length() > 0) {
            boolean result = false;
            for (int i = 0; i < array.length(); i++) {

                final JSONObject obj = array.optJSONObject(i);
                if (obj == null) continue;

                final String key = obj.optString("page", "");
                if (TextUtils.isEmpty(key)) continue;

                final PageConfig pageConfig = new PageConfig();
                pageConfig.setIsHomepage(obj.optBoolean("is_main_page", false));

                final JSONArray api = obj.optJSONArray("api");
                if (api != null && api.length() > 0) {
                    String[] apis = new String[api.length()];
                    if (api.length() > 0) {
                        for (int j = 0; j < api.length(); j++) {
                            apis[j] = api.optString(j);
                        }
                    }
                    pageConfig.setApiFilter(apis);
                }
                mConfig.put(key, pageConfig);
                result = true;
            }
            Log.d(LOG_PREFIX, "readConfigFromJson, result = " + result + " ,count =" + mConfig.size());
            return result;
        }
        return false;
    }


    public View wrap(String key, View view) {
        if (!isPageConfigured(key)) {
            return view;
        }
        return RenderFrameLayout.wrap(key, view, mDrawCallback);
    }

    /**
     * 页面是否配置
     */
    public boolean isPageConfigured(String key) {
        return mConfig.isEmpty() || mConfig.containsKey(key);
    }

    /**
     * 读取或创建Record
     *
     * @param tryCreateIfEmpty 如果没有找到，尝试创建（只有在配置了该页面的情况下才会创建成功）
     */
    private RenderRecord getRenderRecord(String key, boolean tryCreateIfEmpty) {
        if (tryCreateIfEmpty) {
            RenderRecord renderRecord = mActivePage.get(key);
            if (renderRecord == null) {
                PageConfig pageConfig = mConfig.get(key);
                if (pageConfig != null) {
                    renderRecord = new RenderRecord(key, pageConfig).setCallback(mRecordCallback);
                    mActivePage.put(key, renderRecord);
                    Log.d(LOG_PREFIX, "create record, key =" + key);
                }
            }
            return renderRecord;
        } else {
            return mActivePage.get(key);
        }
    }

    @Nullable
    private RenderRecord removeRecord(String key) {
        final RenderRecord record = mActivePage.remove(key);
        if (record != null) {
            record.onRemove();
        }
        Log.d(LOG_PREFIX, "remove record, key = %s, removed =" + key);
        return record;
    }

    /**
     * 获取当前的时间戳。页面渲染的埋点，统一使用此方法记录时间戳。
     */
    public static long timestamp() {
        return SystemClock.elapsedRealtime();
    }


    /**
     * 从当前活动页面找到api匹配的页面记录。暂时不考虑多个活动页面同时使用了同一个API的情况。
     */
    @Nullable
    private RenderRecord findActiveRecord(String relativeUrl) {
        for (RenderRecord record : mActivePage.values()) {
            final PageConfig config = record.getConfig();
            if (config != null && config.matchApi(relativeUrl)) {
                Log.d(LOG_PREFIX, "activeRecord found, api =," + relativeUrl + ", key =" + record.getKey());
                return record;
            }
        }
        Log.d(LOG_PREFIX, "activeRecord not found, api = " + relativeUrl);
        return null;
    }


    private boolean isHomepage(String key) {
        PageConfig record = mConfig.get(key);
        if (record != null) {
            return record.isHomepage();
        } else {
            return false;
        }
    }

    public void onActivityStop(String key) {
        RenderRecord record = getRenderRecord(key, false);
        if (record != null)
            record.onActivityStopTime(timestamp());
    }

    public void onActivityResume(String key) {
        RenderRecord record = getRenderRecord(key, false);
        if (record != null) {
            record.onActivityResumeTime(timestamp());
        }

    }

    /**
     * 页面创建。
     *
     * @param key       页面的KEY，每个页面对应唯一的KEY
     * @param timestamp 时间戳，统一从{@link #timestamp()}方法获取
     */
    public void onPageCreate(String key, long timestamp) {
        final RenderRecord record = getRenderRecord(key, true);
        if (record != null) {
            record.onCreate(timestamp);
            if (isHomepage(key)) {
            } else {
            }
        } else {

        }
    }

    /**
     * API加载开始
     *
     * @param timestamp 时间戳，统一从{@link #timestamp()}方法获取
     * @return 请求是否被处理
     */
    public boolean onApiLoadStart(String relativeUrl, long timestamp) {
        final RenderRecord record = findActiveRecord(relativeUrl);
        if (record != null) {
            record.onLoadStart(timestamp);
            return true;
        }
        return false;
    }

    /**
     * API加载结束
     *
     * @param timestamp 时间戳，统一从{@link #timestamp()}方法获取
     * @return 请求是否被处理
     */
    public boolean onApiLoadEnd(String relativeUrl, long timestamp) {
        final RenderRecord record = findActiveRecord(relativeUrl);
        if (record != null) {
            record.onLoadEnd(timestamp);
            return true;
        }
        return false;
    }

    /**
     * 数据加载开始
     *
     * @param key       页面的KEY，每个页面对应唯一的KEY
     * @param timestamp 时间戳，统一从{@link #timestamp()}方法获取
     */
    public void onPageLoadStart(String key, long timestamp) {
        final RenderRecord record = getRenderRecord(key, false);
        if (record != null) {
            record.onLoadStart(timestamp);
        } else {
            Log.d(LOG_PREFIX, "onPageLoadStart ignored, key =" + key);
        }
    }

    /**
     * 数据加载结束
     *
     * @param key       页面的KEY，每个页面对应唯一的KEY
     * @param timestamp 时间戳，统一从{@link #timestamp()}方法获取
     */
    public void onPageLoadEnd(String key, long timestamp) {
        final RenderRecord record = getRenderRecord(key, false);
        if (record != null) {
            record.onLoadEnd(timestamp);
        } else {
            Log.d(LOG_PREFIX, "onPageLoadEnd ignored, key = " + key);
        }
    }

}
