package com.jason.supervise.sample;

import com.jason.supervise.entity.SuperViseEntity;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemorySampler implements ISample {
    DecimalFormat decimalFormat = new DecimalFormat("##.##");

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void doSample(SuperViseEntity entity) {
        long usedMemInKB = useSize();
        long totalSize = getTotalMemory();
        double useRate = usedMemInKB * 100L / totalSize;
        entity.memoryUsage = usedMemInKB;
        entity.memoryUsageRate = useRate;
    }

    /**
     * 获取当前应用可用总内存
     *
     * @return
     */
    public long getTotalMemory() {
        return Runtime.getRuntime().maxMemory() >> 10;

    }

    /**
     * 获取当前手机总内存大小
     *
     * @return
     */
    public String getTotalRAM() {

        RandomAccessFile reader = null;
        String load;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
            }
            reader.close();
            totRam = Double.parseDouble(value);
            lastValue = twoDecimalForm.format(totRam).concat(" KB");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lastValue;
    }


    public long useSize() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) >> 10;
    }


}
