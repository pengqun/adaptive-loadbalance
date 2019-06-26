package com.aliware.tianchi;

import org.apache.dubbo.rpc.Invoker;

/**
 * @author ./ignore 2019-06-26
 */
public class CommonUtils {

    public static String getProviderKey(Invoker<?> invoker) {
        return invoker.getUrl().getHost();
    }
}
