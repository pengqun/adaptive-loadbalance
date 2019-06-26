package com.aliware.tianchi;

/**
 * @author ./ignore 2019-06-26
 */
public class ServerStatus {

    private String hostName;
    private int threadPoolMaxSize = 0;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getThreadPoolMaxSize() {
        return threadPoolMaxSize;
    }

    public void setThreadPoolMaxSize(Integer threadPoolMaxSize) {
        this.threadPoolMaxSize = threadPoolMaxSize;
    }

    public static ServerStatus fromString(String str) {
        ServerStatus serverStatus = new ServerStatus();
        serverStatus.setHostName(str.substring(0, str.indexOf(":")));
        serverStatus.setThreadPoolMaxSize(Integer.valueOf(str.substring(str.indexOf(":") + 1)));
        return serverStatus;
    }

    @Override
    public String toString() {
        return hostName + ":" + threadPoolMaxSize;
    }
}
