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
        String providerKey = CommonUtils.getProviderKey(invoker);

        try {
            ProviderStats.beginRequest(providerKey);
//            invocation.getAttachments().put("req-start", String.valueOf(System.currentTimeMillis()));
            Result result = invoker.invoke(invocation);
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
//        long requestStart = Long.parseLong(invocation.getAttachments().get("req-start"));
//        long elapsed = System.currentTimeMillis() - requestStart;
        long elapsed = 0;

        String providerKey = CommonUtils.getProviderKey(invoker);
        ProviderStats.endRequest(providerKey, elapsed, !result.hasException());
        return result;
    }
}
