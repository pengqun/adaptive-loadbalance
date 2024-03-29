package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.CommonUtils;
import com.aliware.tianchi.LogUtils;
import com.aliware.tianchi.ProviderStats;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ./ignore 2019-06-25
 */
public class MaxCapacityLoadBalance extends AbstractLoadBalance {

    private static final Logger logger = LoggerFactory.getLogger(MaxCapacityLoadBalance.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    private static final int CACHE_TIMES = 3;

    private Invoker cachedInvoker = null;
    private AtomicInteger cacheCounter = new AtomicInteger(0);

    @SuppressWarnings("Duplicates")
    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (cacheCounter.getAndDecrement() > 0) {
            return cachedInvoker;
        }
        Invoker<T> bestInvoker = null;
        int maxCapacity = -1;
//        int secondCapacity = -1;

        for (Invoker<T> invoker : invokers) {
            String providerKey = CommonUtils.getProviderKey(invoker);
            ProviderStats providerStats = ProviderStats.getStats(providerKey);
            int max = providerStats.getMaxPoolSize();
            int active = providerStats.getActive();
            int capacity = max - active;

            if (bestInvoker == null || capacity > maxCapacity) {
                bestInvoker = invoker;
//                secondCapacity = maxCapacity;
                maxCapacity = capacity;
            }
        }
        if (bestInvoker != null) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("Choose {} with capacity {}", CommonUtils.getProviderKey(bestInvoker), maxCapacity);
//            }
            cachedInvoker = bestInvoker;
            cacheCounter.set(Math.min(CACHE_TIMES, maxCapacity - 1));
//            cacheCounter.set(maxCapacity - secondCapacity - 1);
//            cacheCounter.set((maxCapacity - secondCapacity) / 2);
            return bestInvoker;
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }

    @Override
    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        return 1;
    }
}
