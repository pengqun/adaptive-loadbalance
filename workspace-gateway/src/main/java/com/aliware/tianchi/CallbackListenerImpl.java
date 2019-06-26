package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {

    private static final Logger logger = LoggerFactory.getLogger(CallbackListenerImpl.class);

    static {
//        LogUtils.turnOnDebugLog(logger);
    }

    @Override
    public void receiveServerMsg(String msg) {
//        System.out.println("receive msg from server :" + msg);
        if (logger.isDebugEnabled()) {
            logger.debug("Receive msg: {}", msg);
        }
        ServerStatus serverStatus = ServerStatus.fromString(msg);
        int threadPoolMaxSize = serverStatus.getThreadPoolMaxSize();
        if (threadPoolMaxSize > 0) {
            String hostName = serverStatus.getHostName();
            ProviderStats providerStats = ProviderStats.getStats(hostName);
            if (providerStats != null) {
                providerStats.setMaxPoolSize(threadPoolMaxSize);
                if (logger.isDebugEnabled()) {
                    logger.debug("Set max pool size for {} to {}", hostName, threadPoolMaxSize);
                }
            } else {
                logger.warn("No provider stats found for {}", hostName);
            }
        } else {
            logger.warn("No valid thread pool max size found: {}", serverStatus);
        }
    }

}
