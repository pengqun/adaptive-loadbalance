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
public class LeastRtLoadBalance extends AbstractLoadBalance {

    private static final Logger logger = LoggerFactory.getLogger(LeastRtLoadBalance.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Invoker<T> bestInvoker = null;
        int leastRt = -1;

        for (Invoker<T> invoker : invokers) {
            String providerKey = CommonUtils.getProviderKey(invoker);
            ProviderStats providerStats = ProviderStats.getStats(providerKey);
            if (providerStats.isUnavailable()) {
                continue;
            }

            int successCounter = providerStats.getSuccessCounter();
            int totalElapsed = providerStats.getTotalElapsed();
            if (successCounter == 0 || totalElapsed == 0) {
                continue;
            }
            int rt = totalElapsed * 1000 / successCounter;
            if (logger.isDebugEnabled()) {
                logger.debug("Rt for {}: {} * 1000 / {} = {}", providerKey, totalElapsed, successCounter, rt);
            }

            if (bestInvoker == null || rt < leastRt) {
                bestInvoker = invoker;
                leastRt = rt;
            }
        }
        if (bestInvoker != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Choose {} with rt {}", CommonUtils.getProviderKey(bestInvoker), leastRt);
            }
            return bestInvoker;
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }

    @Override
    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        return 1;
    }
}
