package com.aliware.tianchi;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ./ignore 2019-06-25
 */
public class LocalTest {

    private static final Logger logger = LoggerFactory.getLogger(LocalTest.class);

    static {
        LogUtils.turnOnDebugLog(logger);
    }

    @Test
    public void testLog() {
        if (logger.isDebugEnabled()) {
            logger.debug("debug");
        }
        logger.info("info");
    }
}
