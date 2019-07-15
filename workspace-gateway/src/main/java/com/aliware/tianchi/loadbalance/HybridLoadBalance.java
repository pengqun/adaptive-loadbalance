package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.CommonUtils;
import com.aliware.tianchi.ProviderStats;
import com.aliware.tianchi.RandomUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final int CACHE_TIMES_PHASE_1 = 5;
    private static final int CACHE_TIMES_PHASE_2 = 2;

    private Invoker cachedInvoker = null;
    private AtomicInteger cacheCounter = new AtomicInteger(0);

    private Queue<Invoker> cacheQueue = new ConcurrentLinkedQueue<>();

    @SuppressWarnings("Duplicates")
    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
//        if (cacheCounter.getAndDecrement() > 0) {
//            return cachedInvoker;
//        }
        Invoker cached = cacheQueue.poll();
        if (cached != null) {
            return cached;
        }
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
//            int offset = RandomUtils.nextInt(totalWeight);
            for (int i = 0; i < length; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    Invoker<T> result = invokers.get(i);
//                    cachedInvoker = invokers.get(i);
//                    cacheCounter.set(CACHE_TIMES_PHASE_1);
                    cacheQueue.add(result);
                    cacheQueue.add(result);
                    cacheQueue.add(result);
                    return result;
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
//                cachedInvoker = bestInvoker;
//                cacheCounter.set(Math.min(CACHE_TIMES_PHASE_2, maxCapacity - 1));
                return bestInvoker;
            }
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }
}
