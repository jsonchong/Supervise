package com.jason.supervise;

import android.content.Context;

/**
 * Created by Jason on 2017/12/5.
 */

public class SuperviseConfiguration {

    public final Context context;

    public final double samplingRate;

    public final long mainLooperBlockThreshold;

    public final String renderJsonConfig;

    public SuperviseConfiguration(final Builder builder) {
        context = builder.context;
        samplingRate = builder.samplingRate;
        mainLooperBlockThreshold = builder.mainLooperBlockThreshold;
        renderJsonConfig = builder.renderJsonConfig;
    }

    public static class Builder {
        // 应用上下文，应该传ApplicationContext
        private Context context;

        // 采样频率，单位Hz
        private double samplingRate;

        // 主线程判定为阻塞阈值，单位毫秒(ms)
        private long mainLooperBlockThreshold;

        // 页面渲染Json配置
        private String renderJsonConfig;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder samplingRate(double samplingRate) {
            this.samplingRate = samplingRate;
            return this;
        }

        public Builder mainLooperBlockThreshold(long mainLooperBlockThreshold) {
            this.mainLooperBlockThreshold = mainLooperBlockThreshold;
            return this;
        }

        public Builder renderConfig(String json) {
            this.renderJsonConfig = json;
            return this;
        }

        public SuperviseConfiguration build() {
            return new SuperviseConfiguration(this);
        }
    }
}
