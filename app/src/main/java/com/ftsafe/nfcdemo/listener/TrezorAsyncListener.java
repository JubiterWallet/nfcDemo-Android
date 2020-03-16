package com.ftsafe.nfcdemo.listener;

/**
 * Created by lxl on 2019/4/22.
 */
public interface TrezorAsyncListener<T> {
    public void onResult(String id, T result);

    public void onUiChanged(String apdu, int type);
}
