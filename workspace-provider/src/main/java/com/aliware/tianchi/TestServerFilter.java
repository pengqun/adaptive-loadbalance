package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author daofeng.xjf
 *
 * 服务端过滤器
 * 可选接口
 * 用户可以在服务端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TestServerFilter.class);

    static {
        LogUtils.turnOnDebugLog(logger);
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            if (logger.isDebugEnabled()) {
                logger.debug("Before invoke server filter: {}", invoker.getUrl());
            }
            Result result = invoker.invoke(invocation);
            if (logger.isDebugEnabled()) {
                logger.debug("After invoke server filter: {}", result);
            }
            return result;
        }catch (Exception e){
            throw e;
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("On response in server filter: {}", result);
        }
        return result;
    }

}
