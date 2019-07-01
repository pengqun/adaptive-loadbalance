package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.timer.HashedWheelTimer;
import org.apache.dubbo.common.timer.Timeout;
import org.apache.dubbo.common.timer.Timer;
import org.apache.dubbo.common.timer.TimerTask;
import org.apache.dubbo.common.utils.NamedThreadFactory;
import org.apache.dubbo.remoting.exchange.Response;
import org.apache.dubbo.remoting.exchange.support.DefaultFuture;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.protocol.dubbo.FutureAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author daofeng.xjf
 *
 * 客户端过滤器
 * 可选接口
 * 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TestClientFilter.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    private static final Timer TIME_OUT_TIMER = new HashedWheelTimer(
            new NamedThreadFactory("my-future-timeout", true),
            30,
            TimeUnit.MILLISECONDS);

    private static final Map<Long, Timeout> timeoutMap = new HashMap<>();

    private static class TimeoutCheckTask implements TimerTask {

        private DefaultFuture future;

        TimeoutCheckTask(DefaultFuture future) {
            this.future = future;
        }

        @Override
        public void run(Timeout timeout) {
            if (future == null || future.isDone()) {
                return;
            }
            long requestId = future.getRequest().getId();
            timeoutMap.remove(requestId);
            if (logger.isDebugEnabled()) {
                logger.debug("Timeout for request {}", requestId);
            }

//            future.setCallback(null);
            Response timeoutResponse = new Response(requestId);
            timeoutResponse.setStatus(Response.CLIENT_TIMEOUT);
            timeoutResponse.setErrorMessage("Timeout by filter");
            DefaultFuture.received(null, timeoutResponse);
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (logger.isDebugEnabled()) {
            logger.debug("Before invoke client filter: {}", invoker.getUrl());
        }
        String providerKey = CommonUtils.getProviderKey(invoker);

        try {
            ProviderStats.beginRequest(providerKey);
            invocation.getAttachments().put("req-start", String.valueOf(System.currentTimeMillis()));
            Result result = invoker.invoke(invocation);

            if (result instanceof SimpleAsyncRpcResult) {
                FutureAdapter futureAdapter = (FutureAdapter) ((SimpleAsyncRpcResult) result).getValueFuture();
                DefaultFuture defaultFuture = (DefaultFuture) (futureAdapter.getFuture());
                long requestId = defaultFuture.getRequest().getId();

                int ewmaElapsed = (int) ProviderStats.getStats(providerKey).getEwmaElapsed();
                if (ewmaElapsed > 0) {
                    Timeout timeout = TIME_OUT_TIMER.newTimeout(new TimeoutCheckTask(defaultFuture),
                            ewmaElapsed * 6, TimeUnit.MILLISECONDS);
                    timeoutMap.put(requestId, timeout);
                    invocation.getAttachments().put("req-id", String.valueOf(requestId));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Set timeout to {} ms for request {}", timeout, requestId);
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("After invoke client filter: {}", result);
            }
            return result;
        } catch (Exception e) {
            logger.error("!!! Invoke error before response", e);
            ProviderStats.endRequest(providerKey, 0, false);
            throw e;
        }
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("On response in client filter: {}", result);
        }
        long requestStart = Long.parseLong(invocation.getAttachments().get("req-start"));
        long elapsed = System.currentTimeMillis() - requestStart;
//        long elapsed = 0;

        String requestIdStr = invocation.getAttachments().get("req-id");
        if (requestIdStr != null) {
            long requestId = Long.parseLong(requestIdStr);
            Timeout timeout = timeoutMap.get(requestId);
            if (timeout != null) {
                timeout.cancel();
                if (logger.isDebugEnabled()) {
                    logger.debug("Remove timeout for request: {}", requestId);
                }
                timeoutMap.remove(requestId);
            }
        }

        String providerKey = CommonUtils.getProviderKey(invoker);
        ProviderStats.endRequest(providerKey, elapsed, !result.hasException());
        return result;
    }

}
