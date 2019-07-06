package com.aliware.tianchi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ./ignore 2019-06-26
 */
public class ProviderStats {

    private static final Logger logger = LoggerFactory.getLogger(ProviderStats.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    private static final ConcurrentMap<String, ProviderStats> allProviderStats = new ConcurrentHashMap<>();

    private static final int RESET_COUNTER_INTERVAL = 100;
    private static final double EWMA_ALPHA = 0.001;

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

//        if (succeeded) {
            if (stats.ewmaElapsed == -1) {
                stats.ewmaElapsed = elapsed;
            } else {
                stats.ewmaElapsed = stats.ewmaElapsed + EWMA_ALPHA * (elapsed - stats.ewmaElapsed);
            }
//            if (logger.isDebugEnabled()) {
//                logger.debug("Update ewma for {} to {} by {}", providerKey, stats.ewmaElapsed, elapsed);
//            }
//        }

//        if (succeeded) {
//            stats.lastElapsed = (int) elapsed;
//        } else {
//            stats.lastElapsed = stats.lastElapsed + (int) elapsed;
//        }

        if (stats.errorPenalty.get() > 0) {
            stats.errorPenalty.decrementAndGet();
        } else if (stats.active.get() > stats.maxPoolSize / 2
                && stats.ewmaElapsed > 0 && elapsed > stats.ewmaElapsed * 6) {
            stats.errorPenalty.set(stats.active.get() / 5);
        }

//        if (succeeded && (stats.ewmaElapsed == -1 || elapsed < stats.ewmaElapsed * 6)) {
//            if (stats.errorPenalty.get() > 0) {
//                stats.errorPenalty.decrementAndGet();
//            }
//        } else if (stats.active.get() > stats.maxPoolSize / 2) {
//            stats.errorPenalty.set(stats.active.get() / 5);
//        }
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
        return errorPenalty.get() > 0;
//        return active.get() >= maxPoolSize || errorPenalty.get() > 0;
//        return active.get() >= maxPoolSize;
    }
}
