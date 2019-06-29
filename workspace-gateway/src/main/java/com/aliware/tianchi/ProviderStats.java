package com.aliware.tianchi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ./ignore 2019-06-26
 */
public class ProviderStats {

    private static final ConcurrentMap<String, ProviderStats> allProviderStats = new ConcurrentHashMap<>();

    private static final int RESET_COUNTER_INTERVAL = 10;
    private static final double EWMA_ALPHA = 0.01;

    private int maxPoolSize = Integer.MAX_VALUE;

    private final AtomicInteger active = new AtomicInteger(0);
    private final AtomicInteger errorPenalty = new AtomicInteger(0);

    private final AtomicInteger successCounter = new AtomicInteger(0);
    private final AtomicInteger totalElapsed = new AtomicInteger(0);

    private int lastElapsed = 0;

    private double ewmaElapsed = -1;

    public static ProviderStats getStats(String providerKey) {
        return allProviderStats.computeIfAbsent(providerKey, (k -> new ProviderStats()));
    }

    public static void beginRequest(String providerKey) {
        ProviderStats stats = getStats(providerKey);
        stats.active.incrementAndGet();
    }

    public static void endRequest(String providerKey, long elapsed, boolean succeeded) {
        ProviderStats stats = getStats(providerKey);
        stats.active.decrementAndGet();

//        if (succeeded) {
//            int count = stats.successCounter.incrementAndGet();
//            if (count == RESET_COUNTER_INTERVAL) {
//                stats.successCounter.set(1);
//                stats.totalElapsed.set((int) elapsed);
//            } else {
//                stats.totalElapsed.addAndGet((int) elapsed);
//            }
//        } else {
//            stats.totalElapsed.addAndGet((int) elapsed);
//        }

        if (succeeded) {
            if (stats.ewmaElapsed == -1) {
                stats.ewmaElapsed = elapsed;
            } else {
                stats.ewmaElapsed = stats.ewmaElapsed + EWMA_ALPHA * (elapsed - stats.ewmaElapsed);
            }
        }

//        if (succeeded) {
//            stats.lastElapsed = (int) elapsed;
//        } else {
//            stats.lastElapsed = stats.lastElapsed + (int) elapsed;
//        }

        if (succeeded) {
            if (stats.errorPenalty.get() > 0) {
                stats.errorPenalty.decrementAndGet();
            }
        } else {
            stats.errorPenalty.set(stats.active.get() / 5);
        }
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getActive() {
        return active.get();
    }

    public int getSuccessCounter() {
        return successCounter.get();
    }

    public int getTotalElapsed() {
        return totalElapsed.get();
    }

    public int getLastElapsed() {
        return lastElapsed;
    }

    public double getEwmaElapsed() {
        return ewmaElapsed;
    }

    public boolean isUnavailable() {
        return errorPenalty.get() > 0 || active.get() >= maxPoolSize;
//        return active.get() >= maxPoolSize;
    }
}
