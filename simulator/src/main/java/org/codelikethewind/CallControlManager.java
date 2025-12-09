package org.codelikethewind;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class CallControlManager {

    private volatile double dropRate = 0.0;
    private volatile boolean emitterEnabled = true;
    private final AtomicInteger activeCallCount = new AtomicInteger(0);

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

    public int getActiveCallCount() {
        return activeCallCount.get();
    }

    public int incrementActiveCalls() {
        return activeCallCount.incrementAndGet();
    }

    public int decrementActiveCalls() {
        return activeCallCount.updateAndGet(count -> Math.max(0, count - 1));
    }

    public void resetActiveCalls() {
        activeCallCount.set(0);
    }
}
