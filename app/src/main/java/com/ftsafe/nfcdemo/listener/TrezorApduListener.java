package com.ftsafe.nfcdemo.listener;

/**
 * Created by lxl on 2019/4/22.
 */
public interface TrezorApduListener {
    public void onSend(String apdu);

    public void onRecv(String apdu);
}
