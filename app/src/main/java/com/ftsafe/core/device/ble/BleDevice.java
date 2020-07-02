package com.ftsafe.core.device.ble;

/**
 * fengshuo
 */
public class BleDevice {

    private String mDevName;
    private String mDevAddress;
    private int mDevType;
    private long mDeviceHandle;
    // 暂未使用
    private int mRssi;

    public BleDevice(String name, String address, int devType) {
        this.mDevName = name;
        this.mDevAddress = address;
        this.mDevType = devType;
    }

    public String getDevName() {
        return mDevName;
    }

    public String getDevAddress() {
        return mDevAddress;
    }

    public int getDevType() {
        return mDevType;
    }

    public long getDeviceHandle() {
        return mDeviceHandle;
    }

    public void setDeviceHandle(long mDeviceHandle) {
        this.mDeviceHandle = mDeviceHandle;
    }
}
