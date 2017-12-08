package com.jason.supervise.render;

import android.text.TextUtils;

/**
 * 页面配置：当配置了页面但API列表为空，表示页面不需要加载数据；一个页面的API可以配置多条。
 * Created by Jason on 2017/12/6.
 */

public class PageConfig {

    private String[] mRelativeUrl;
    private int mCount = 0;
    private boolean mIsHomepage = false;

    public void reset() {
        mCount = 0;
    }

    public PageConfig setApiFilter(String[] apiFilter) {
        mRelativeUrl = apiFilter;
        return this;
    }

    public void setIsHomepage(boolean isHomepage) {
        mIsHomepage = isHomepage;
    }

    public boolean needLoadData() {
        return mRelativeUrl != null && mRelativeUrl.length > 0;
    }

    /**
     * 当API被调用次数和API总数相同，视为加载完成。暂不考虑一个API被多次调用。
     */
    public boolean isLoadFinish() {
        return mRelativeUrl == null || mCount >= mRelativeUrl.length * 2;// 乘2:每次请求发起和完成分别会匹配一次
    }

    public boolean isHomepage() {
        return mIsHomepage;
    }


    public boolean matchApi(String requestRelativeUrl) {
        if (mRelativeUrl != null) {
            for (String s : mRelativeUrl) {
                if (TextUtils.equals(s, requestRelativeUrl)) {
                    ++mCount;
                    return true;
                }
            }
        }
        return false;
    }
}
