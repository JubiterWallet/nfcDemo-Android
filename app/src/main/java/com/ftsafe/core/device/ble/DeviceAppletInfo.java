package com.ftsafe.core.device.ble;

/**
 * @author fengshuo
 * @date 2019/4/18
 * @time 14:01
 */
public class DeviceAppletInfo {

    private String appid;
    private String version;

    public DeviceAppletInfo() {
    }

    public DeviceAppletInfo(String appletId, String appletVersion) {
        this.appid = appletId;
        this.version = appletVersion;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
