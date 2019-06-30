package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.CommonUtils;
import com.aliware.tianchi.LogUtils;
import com.aliware.tianchi.ProviderStats;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author ./ignore 2019-06-26
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    private static final Logger logger = LoggerFactory.getLogger(LeastActiveLoadBalance.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    private static final int DEFAULT_WEIGHT = 1;

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        return doSelect(invokers, url, invocation);
    }

    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation);

    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        String providerKey = CommonUtils.getProviderKey(invoker);
        ProviderStats providerStats = ProviderStats.getStats(providerKey);
        if (providerStats.isUnavailable()) {
            return 0;
        }
//        return getWeightByRtAndActive(providerKey, providerStats);
        return getWeightByActive(providerKey, providerStats);
//        return getWeightByRt(providerKey, providerStats);
//        return getWeightByLastRt(providerKey, providerStats);
//        return getWeightByEwmaRt(providerKey, providerStats);
//        return getWeightByEwmaRtAndActive(providerKey, providerStats);
    }

    private int getWeightByRtAndActive(String providerKey, ProviderStats providerStats) {
        int successCounter = providerStats.getSuccessCounter();
        int totalElapsed = providerStats.getTotalElapsed();
        int active = providerStats.getActive() + 1;
        int max = providerStats.getMaxPoolSize();
        int weight = DEFAULT_WEIGHT;
        if (successCounter > 0) {
            weight = (int) ((10000L * successCounter * max) / (totalElapsed * active));
//            weight = (10000 * successCounter / totalElapsed) + (max / active);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Weight for {}: {}, success - {}, elapsed - {}, active - {}, max - {}",
                    providerKey, weight, successCounter, totalElapsed, active, max);
        }
        return weight;
    }

    private int getWeightByActive(String providerKey, ProviderStats providerStats) {
        int active = providerStats.getActive() + 1;
        int max = providerStats.getMaxPoolSize();
//        int weight = 10000 * max / active;
        int weight = max;
        if (logger.isDebugEnabled()) {
            logger.debug("Weight for {}: {}, active - {}, max - {}",
                    providerKey, weight, active, max);
        }
        return weight;
    }

    private int getWeightByRt(String providerKey, ProviderStats providerStats) {
        int successCounter = providerStats.getSuccessCounter();
        int totalElapsed = providerStats.getTotalElapsed();
        int weight = DEFAULT_WEIGHT;
        if (successCounter > 0) {
            weight = (int) ((10000L * successCounter) / totalElapsed);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Weight for {}: {}, success - {}, elapsed - {}",
                    providerKey, weight, successCounter, totalElapsed);
        }
        return weight;
    }

    private int getWeightByLastRt(String providerKey, ProviderStats providerStats) {
        int lastElapsed = providerStats.getLastElapsed();
        int weight = DEFAULT_WEIGHT;
        if (lastElapsed > 0) {
            weight = 10000 / lastElapsed;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Weight for {}: {}, lastElapsed - {}",
                    providerKey, weight, lastElapsed);
        }
        return weight;
    }

    private int getWeightByEwmaRt(String providerKey, ProviderStats providerStats) {
        double ewmaElapsed = providerStats.getEwmaElapsed();
        int weight = DEFAULT_WEIGHT;
        if (ewmaElapsed > 0) {
            weight = (int) (100000 / ewmaElapsed);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Weight for {}: {}, ewmaElapsed - {}",
                    providerKey, weight, ewmaElapsed);
        }
        return weight;
    }

    private int getWeightByEwmaRtAndActive(String providerKey, ProviderStats providerStats) {
        double ewmaElapsed = providerStats.getEwmaElapsed();
        int active = providerStats.getActive() + 1;
        int max = providerStats.getMaxPoolSize();
        int weight = DEFAULT_WEIGHT;
        if (ewmaElapsed > 0) {
            weight = (int) ((10000 * max) / (active * ewmaElapsed));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Weight for {}: {}, ewmaElapsed- {}, active - {}, max - {}",
                    providerKey, weight, ewmaElapsed, active, max);
        }
        return weight;
    }
}
