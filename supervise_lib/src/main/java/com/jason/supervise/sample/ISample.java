package com.jason.supervise.sample;
import com.jason.supervise.entity.SuperViseEntity;

/**
 * Created by Jason on 2017/12/5.
 */

public interface ISample {

    void start();

    void stop();

    void doSample(SuperViseEntity entity);
}
