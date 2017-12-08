package com.jason.supervise.sample;

import android.util.Log;

import com.jason.supervise.entity.SuperViseEntity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CpuSampler implements ISample {
    private static final String TAG = "CpuSampler";
    private int mPid = 0;
    private long mUserLast = 0;
    private long mSystemLast = 0;
    private long mIdleLast = 0;
    private long mIoWaitLast = 0;
    private long mTotalLast = 0;
    private long mAppCpuTimeLast = 0;

    @Override
    public void start() {
        mUserLast = 0;
        mSystemLast = 0;
        mIdleLast = 0;
        mIoWaitLast = 0;
        mTotalLast = 0;
        mAppCpuTimeLast = 0;
    }

    @Override
    public void stop() {

    }

    @Override
    public void doSample(SuperViseEntity entity) {
        BufferedReader cpuReader = null;
        BufferedReader pidReader = null;
        try {
            cpuReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String cpuRate = cpuReader.readLine();
            if (cpuRate == null) {
                cpuRate = "";
            }

            if (mPid == 0) {
                mPid = android.os.Process.myPid();
            }
            pidReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + mPid + "/stat")), 1000);
            String pidCpuRate = pidReader.readLine();
            if (pidCpuRate == null) {
                pidCpuRate = "";
            }

            parseCpuRate(cpuRate, pidCpuRate, entity);
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "doSample: ", e);
            }
        }
    }


    private void parseCpuRate(String cpuRate, String pidCpuRate, SuperViseEntity entity) {
        String[] cpuInfoArray = cpuRate.split(" ");
        if (cpuInfoArray.length < 9) {
            return;
        }
        // 从系统启动开始累计到当前时刻，用户态的CPU时间，不包含 nice值为负进程
        long user = Long.parseLong(cpuInfoArray[2]);
        // 从系统启动开始累计到当前时刻，nice值为负的进程所占用的CPU时间
        long nice = Long.parseLong(cpuInfoArray[3]);
        // 从系统启动开始累计到当前时刻，核心时间
        long system = Long.parseLong(cpuInfoArray[4]);
        // 从系统启动开始累计到当前时刻，除硬盘IO等待时间以外其它等待时间
        long idle = Long.parseLong(cpuInfoArray[5]);
        // 从系统启动开始累计到当前时刻，硬盘IO等待时间
        long ioWait = Long.parseLong(cpuInfoArray[6]);
        // CPU总时间 = 以上所有加上irq（硬中断）和softirq（软中断）的时间
        long total = user + nice + system + idle + ioWait + Long.parseLong(cpuInfoArray[7]) + Long.parseLong(cpuInfoArray[8]);

        String[] pidCpuInfos = pidCpuRate.split(" ");
        if (pidCpuInfos.length < 17) {
            return;
        }

        long appCpuTime = Long.parseLong(pidCpuInfos[13]) + Long.parseLong(pidCpuInfos[14])
                + Long.parseLong(pidCpuInfos[15]) + Long.parseLong(pidCpuInfos[16]);

        if (mTotalLast != 0) {
            long idleTime = idle - mIdleLast;
            long totalTime = total - mTotalLast;
            entity.cpuRate = (totalTime - idleTime) * 100L / totalTime;
            entity.appCpuRate = (appCpuTime - mAppCpuTimeLast) * 100L / totalTime;
            entity.userCpuRate = (user - mUserLast) * 100L / totalTime;
            entity.systemCpuRate = (system - mSystemLast) * 100L / totalTime;
            entity.ioWaitCpuRate = (ioWait - mIoWaitLast) * 100L / totalTime;


        }
        mUserLast = user;
        mSystemLast = system;
        mIdleLast = idle;
        mIoWaitLast = ioWait;
        mTotalLast = total;

        mAppCpuTimeLast = appCpuTime;
    }

}
