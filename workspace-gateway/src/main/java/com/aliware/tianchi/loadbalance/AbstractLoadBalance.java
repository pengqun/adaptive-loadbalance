package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.CommonUtils;
import com.aliware.tianchi.ProviderStats;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;

/**
 * @author ./ignore 2019-06-26
 */
public abstract class AbstractLoadBalance implements LoadBalance {

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
        if (successCounter > 0) {
            int totalElapsed = providerStats.getTotalElapsed();
            return 10000 * successCounter / totalElapsed;
        }
        return DEFAULT_WEIGHT;
    }
}
