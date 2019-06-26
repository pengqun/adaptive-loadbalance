package com.aliware.tianchi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ./ignore 2019-06-26
 */
public class ProviderStats {

    private static final ConcurrentMap<String, ProviderStats> allProviderStats = new ConcurrentHashMap<>();

    private final AtomicInteger active = new AtomicInteger(0);
    private final AtomicInteger errorPenalty = new AtomicInteger(0);

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
        if (succeeded) {
            if (stats.errorPenalty.get() > 0) {
                stats.errorPenalty.decrementAndGet();
            }
        } else {
            stats.errorPenalty.set(stats.active.get() / 5);
        }
    }

    public int getActive() {
        return active.get();
    }

    public boolean isAvailable() {
        return errorPenalty.get() <= 0;
    }
}
