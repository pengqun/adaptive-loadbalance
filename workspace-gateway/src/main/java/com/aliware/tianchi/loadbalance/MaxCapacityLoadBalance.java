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

/**
 * @author ./ignore 2019-06-25
 */
public class MaxCapacityLoadBalance extends AbstractLoadBalance {

    private static final Logger logger = LoggerFactory.getLogger(MaxCapacityLoadBalance.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Invoker<T> bestInvoker = null;
        int maxCapacity = -1;

        for (Invoker<T> invoker : invokers) {
            String providerKey = CommonUtils.getProviderKey(invoker);
            ProviderStats providerStats = ProviderStats.getStats(providerKey);
            if (providerStats.isUnavailable()) {
                continue;
            }
            int max = providerStats.getMaxPoolSize();
            int active = providerStats.getActive();
            int capacity = max - active;

            if (bestInvoker == null || capacity > maxCapacity) {
                bestInvoker = invoker;
                maxCapacity = capacity;
            }
        }
        if (bestInvoker == null) {
            maxCapacity = -1;
            for (Invoker<T> invoker : invokers) {
                String providerKey = CommonUtils.getProviderKey(invoker);
                ProviderStats providerStats = ProviderStats.getStats(providerKey);
                int max = providerStats.getMaxPoolSize();
                int active = providerStats.getActive();
                int capacity = max - active;

                if (bestInvoker == null || capacity > maxCapacity) {
                    bestInvoker = invoker;
                    maxCapacity = capacity;
                }
            }
        }
        if (bestInvoker != null) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("Choose {} with capacity {}", CommonUtils.getProviderKey(bestInvoker), maxCapacity);
//            }
            return bestInvoker;
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }

    @Override
    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        return 1;
    }
}
