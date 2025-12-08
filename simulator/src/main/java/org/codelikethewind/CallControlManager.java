package org.codelikethewind;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CallControlManager {

    private volatile double dropRate = 0.0;
    private volatile boolean emitterEnabled = true;

    public double getDropRate() {
        return dropRate;
    }

    public void setDropRate(double dropRate) {
        this.dropRate = dropRate;
    }

    public boolean isEmitterEnabled() {
        return emitterEnabled;
    }

    public void setEmitterEnabled(boolean emitterEnabled) {
        this.emitterEnabled = emitterEnabled;
    }
}
