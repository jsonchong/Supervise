package com.jason.supervise.entity;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jason on 2017/12/5.
 */

public class SuperViseEntity {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.CHINA);
    private static final String TAG = "HertzEntity";
    public static final String SEPARATOR = "\n";
    public static final String SPLIT_MARK = "===================================================================================";
    public static final String LOG_SEPARATOR = "\r\n";
    public static final String KEY = "=";
    public static final String CPU_KEY = "cpu";
    public static final String FPS_KEY = "fps";
    public static final String MEM_KEY = "memory";
    public static final String STACK_KEY = "stack_trace";
    public static final String START_TIME_KEY = "start_time";
    private static final String PROCESS_NAME_KEY = "process_name";
    private static final String TIME_COST_KEY = "time_cost";


    // cpu使用率
    public double cpuRate;

    // 当前进程的cpu使用率
    public double appCpuRate;

    // 用户态cpu使用率
    public double userCpuRate;

    // 核心态cpu使用率
    public double systemCpuRate;

    // 硬盘io等待时间比例
    public double ioWaitCpuRate;

    // 内存使用量，单位kb
    public long memoryUsage;

    // 内存使用率
    public double memoryUsageRate;

    // FPS
    public double fps;

    // 堆栈信息
    public List<String> threadStacks = new ArrayList<>();

    // Logcat
    public String logCats;

    //本地日志最后修改时间
    public long lastModified;

    public long timeStart;

    public String processName;

    public long timeCost;
    public File logFile;

    private String stackKey;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SPLIT_MARK).append(SEPARATOR);
        sb.append("|").append(dateFormat.format(System.currentTimeMillis()));
        if (threadStacks != null && !threadStacks.isEmpty()) {
            sb.append(SEPARATOR).append("当前卡顿堆栈信息:").append(SEPARATOR);
            for (String s : threadStacks) {
                sb.append(s);
                sb.append(SEPARATOR);
            }
            sb.append(SPLIT_MARK);
        }
        sb.append(SEPARATOR);
        if (!TextUtils.isEmpty(logCats)) {
            sb.append(SPLIT_MARK).append(SEPARATOR).append("当前LOG日志信息:").append(SEPARATOR);
            sb.append(logCats);
            sb.append(SEPARATOR);
        }
        sb.append("|当前周期采集信息:").append(SEPARATOR);
        sb.append("|cpu   : " + cpuRate + "% " + "app:" + appCpuRate + "% "
                + "user:" + userCpuRate + "% "
                + "system:" + systemCpuRate + "% "
                + "ioWait:" + ioWaitCpuRate + "%");
        sb.append(SEPARATOR);
        sb.append("|" + "fps   : " + fps);
        sb.append(SEPARATOR);
        sb.append("|" + "memory: " + memoryUsage + "kb       ").append(memoryUsageRate + "%");
        sb.append(SEPARATOR).append(SPLIT_MARK);
        return sb.toString();
    }

    public String flushString() {
        StringBuilder sb = new StringBuilder();
        sb.append(START_TIME_KEY).append(KEY).append(timeStart).append(LOG_SEPARATOR);
        sb.append(PROCESS_NAME_KEY).append(KEY).append(processName).append(LOG_SEPARATOR);
        sb.append(CPU_KEY).append(KEY).append(cpuRate).append(LOG_SEPARATOR)
                .append(FPS_KEY).append(KEY).append(fps).append(LOG_SEPARATOR)
                .append(MEM_KEY).append(KEY).append(memoryUsageRate).append(LOG_SEPARATOR)
                .append(TIME_COST_KEY).append(KEY).append(timeCost).append(LOG_SEPARATOR);
        if (threadStacks != null && threadStacks.size() > 0) {
            StringBuilder stackSb = new StringBuilder();
            for (String stack : threadStacks) {
                stackSb.append(stack).append(LOG_SEPARATOR);
            }
            sb.append(STACK_KEY).append(KEY).append(stackSb.toString()).append(LOG_SEPARATOR);
        }
        return sb.toString();
    }

    public String getKeyStackString() {
        if (TextUtils.isEmpty(stackKey)) {
            for (String stack : threadStacks) {
                if (Character.isLetter(stack.charAt(0))) {
                    String[] lines = stack.split(SEPARATOR);
                    for (String line : lines) {
                        if (!line.startsWith("com.android")
                                && !line.startsWith("java")
                                && !line.startsWith("android")
                                && !line.startsWith("dalvik")
                                && !line.startsWith("sun")
                                && !line.startsWith("libcore")) {
                            int start = 0;
                            int end = line.lastIndexOf("(");
                            if (end != -1) {
                                stackKey = line.substring(start, end);
                                return stackKey;
                            }
                        }
                    }
                }
            }
        }
        return stackKey;
    }

    public static SuperViseEntity getInstance(File file) {
        BufferedReader reader = null;
        SuperViseEntity hertzEntity = new SuperViseEntity();
        hertzEntity.lastModified = file.lastModified();
        hertzEntity.logFile = file;
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");
            reader = new BufferedReader(in);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith(PROCESS_NAME_KEY)) {
                    hertzEntity.processName = line.split(KEY)[1];
                } else if (line.startsWith(TIME_COST_KEY)) {
                    hertzEntity.timeCost = Long.parseLong(line.split(KEY)[1]);
                } else if (line.startsWith(START_TIME_KEY)) {
                    hertzEntity.timeStart = Long.parseLong(line.split(KEY)[1]);
                } else if (line.startsWith(CPU_KEY)) {
                    hertzEntity.cpuRate = Double.parseDouble(line.split(KEY)[1]);
                } else if (line.startsWith(FPS_KEY)) {
                    hertzEntity.fps = Double.parseDouble(line.split(KEY)[1]);
                } else if (line.startsWith(MEM_KEY)) {
                    hertzEntity.memoryUsageRate = Double.parseDouble(line.split(KEY)[1]);
                } else if (line.startsWith(STACK_KEY)) {
                    line = line.split(KEY)[1];
                    hertzEntity.threadStacks.add(dateFormat.format(hertzEntity.timeStart));
                    StringBuilder stackSb = new StringBuilder();
                    while (line != null) {
                        if (!TextUtils.isEmpty(line)) {
                            stackSb.append(line).append(LOG_SEPARATOR);
                        }
                        line = reader.readLine();
                    }
                    hertzEntity.threadStacks.add(stackSb.toString());
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "getInstance: ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hertzEntity;

    }

    public String getBasicString() {
        return CPU_KEY + KEY + cpuRate + LOG_SEPARATOR +
                FPS_KEY + KEY + fps + LOG_SEPARATOR +
                MEM_KEY + KEY + memoryUsageRate + LOG_SEPARATOR;
    }

    public String getTimeCostString() {
        return "time cost " + timeCost + "ms";
    }

    public boolean isValid() {
        return fps > 0 && cpuRate > 0 && fps<=60 && cpuRate<=100;
    }

    public boolean isValidForLaggy() {
        return threadStacks != null && threadStacks.size() > 0 && !TextUtils.isEmpty(getKeyStackString());
    }


}
