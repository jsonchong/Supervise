package com.jason.supervise.sample;

import com.jason.supervise.SuperviseConfiguration;
import com.jason.supervise.entity.SuperViseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2017/12/5.
 */

public class SampleChain implements ISample {
    private List<ISample> samplers = new ArrayList<>();
    private List<ISample> perfSamplers = new ArrayList<>();


   public static SampleChain createSamplerChain(SuperviseConfiguration configuration) {
        SampleChain samplerChain = new SampleChain();
        samplerChain.addSampler(new CpuSampler());
        samplerChain.addSampler(new MemorySampler());
        return samplerChain;
    }

    private void addSampler(ISample sampler) {
        samplers.add(sampler);
    }

    @Override
    public void start() {
        for (ISample sampler : samplers) {
            sampler.start();
        }

    }

    @Override
    public void stop() {
        for (ISample sampler : samplers) {
            sampler.stop();
        }
    }

    @Override
    public void doSample(SuperViseEntity entity) {
        for (ISample sampler : samplers) {
            sampler.doSample(entity);
        }
    }
}
