package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (logger.isDebugEnabled()) {
            logger.debug("Before invoke client filter: {}", invoker.getUrl());
        }
        URL url = invoker.getUrl();
        String methodName = invocation.getMethodName();
        int max = Integer.MAX_VALUE;
        RpcStatus rpcStatus = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName());

        try {
            if (!RpcStatus.beginCount(url, methodName, max)) {
//            long timeout = invoker.getUrl().getMethodParameter(invocation.getMethodName(), TIMEOUT_KEY, 0);
                long timeout = 5000;
                long start = System.currentTimeMillis();
                long remain = timeout;
                synchronized (rpcStatus) {
                    while (!RpcStatus.beginCount(url, methodName, max)) {
                        try {
                            rpcStatus.wait(remain);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        long elapsed = System.currentTimeMillis() - start;
                        remain = timeout - elapsed;
                        if (remain <= 0) {
                            throw new RpcException("Waiting concurrent invoke timeout in client-side for service:  "
                                    + invoker.getInterface().getName() + ", method: " + invocation.getMethodName()
                                    + ", elapsed: " + elapsed + ", timeout: " + timeout + ". concurrent invokes: "
                                    + rpcStatus.getActive() + ". max concurrent invoke limit: " + max);
                        }
                    }
                }
            }

            Result result = invoker.invoke(invocation);
            if (logger.isDebugEnabled()) {
                logger.debug("After invoke client filter: {}", result);
            }
            return result;

        } catch (Exception e) {
            RpcStatus.endCount(url, methodName, 0, false);
            throw e;
        }
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("On response in client filter: {}", result);
        }

        String methodName = invocation.getMethodName();
        URL url = invoker.getUrl();
        RpcStatus.endCount(url, methodName, 0, result.hasException());

        return result;
    }

//    private void beforeInvoke(Invoker<?> invoker, Invocation invocation) {
//        invocation.getAttachments().put("ch-id", String.valueOf(System.currentTimeMillis()));
//    }
}
