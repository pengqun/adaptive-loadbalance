package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.CommonUtils;
import com.aliware.tianchi.ProviderStats;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Adapted from org.apache.dubbo.rpc.cluster.loadbalance.RandomLoadBalance
 *
 * @author ./ignore 2019-06-26
 */
public class HybridLoadBalance extends AbstractLoadBalance {

    private static final Logger logger = LoggerFactory.getLogger(HybridLoadBalance.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int length = invokers.size();
        int[] weights = new int[length];
        int firstWeight = getWeight(invokers.get(0), invocation);
        weights[0] = firstWeight;
        int totalWeight = firstWeight;
        for (int i = 1; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            weights[i] = weight;
            totalWeight += weight;
        }
        if (totalWeight > 0) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int i = 0; i < length; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        } else {
            Invoker<T> bestInvoker = null;
            int maxCapacity = -1;
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
            if (bestInvoker != null) {
                return bestInvoker;
            }
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }
}