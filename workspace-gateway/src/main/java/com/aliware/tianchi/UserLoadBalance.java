package com.aliware.tianchi;

import com.aliware.tianchi.loadbalance.LeastActiveLoadBalance;
import com.aliware.tianchi.loadbalance.RandomLoadBalance;
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
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    private static final Logger logger = LoggerFactory.getLogger(UserLoadBalance.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    private LoadBalance loadBalance = new LeastActiveLoadBalance();
//    private LoadBalance loadBalance = new RandomLoadBalance();
//    private LoadBalance loadBalance = null;

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (logger.isDebugEnabled()) {
            logger.debug("Before select in load balance: {} - {}", invokers.get(0).getUrl(), url);
        }
        if (loadBalance != null) {
            return loadBalance.select(invokers, url, invocation);
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
