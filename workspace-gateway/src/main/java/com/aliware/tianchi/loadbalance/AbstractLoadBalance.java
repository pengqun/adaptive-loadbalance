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
//        if (CollectionUtils.isEmpty(invokers)) {
//            return null;
//        }
//        if (invokers.size() == 1) {
//            return invokers.get(0);
//        }
        return doSelect(invokers, url, invocation);
    }

    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation);

    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        String providerKey = CommonUtils.getProviderKey(invoker);
        ProviderStats providerStats = ProviderStats.getStats(providerKey);
        if (providerStats.isUnavailable()) {
            return 0;
        }
        int successCounter = providerStats.getSuccessCounter();
        int totalElapsed = providerStats.getTotalElapsed();
        int active = providerStats.getActive() + 1;
        int max = providerStats.getMaxPoolSize();
        int weight = DEFAULT_WEIGHT;
        if (successCounter > 0) {
            weight = (int) ((10000L * successCounter * max) / (totalElapsed * active));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Weight for {}: {}, success - {}, elapsed - {}, active - {}, max - {}",
                    providerKey, weight, successCounter, totalElapsed, active, max);
        }
        return weight;
    }
}
