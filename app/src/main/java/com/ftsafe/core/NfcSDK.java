package com.ftsafe.core;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;

import com.ftsafe.nfc.card.sdk.InitParameter;

import static android.nfc.NfcAdapter.EXTRA_TAG;

public class NfcSDK {
    private static NfcSDK mNfcardOTPSDK;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Activity apiActivity;
    private long mDeviceHandle;

    private static String[][] TECHLISTS;
    private static IntentFilter[] TAGFILTERS;

    static {
        try {
            TECHLISTS = new String[][]{{IsoDep.class.getName()},
                    {NfcA.class.getName()},};

            TAGFILTERS = new IntentFilter[]{new IntentFilter(
                    NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")};
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getDeviceHandle() {
        return mDeviceHandle;
    }

    private NfcSDK(Activity mActivity) {
        apiActivity = mActivity;

        nfcAdapter = NfcAdapter.getDefaultAdapter(apiActivity);
        pendingIntent = PendingIntent.getActivity(apiActivity, 0, new Intent(
                apiActivity, apiActivity.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    public synchronized static NfcSDK getInstance(Activity mActivity) {
        mNfcardOTPSDK = new NfcSDK(mActivity);
        return mNfcardOTPSDK;
    }

    public int init(Intent intent, String initParams) {
        Tag tag = (Tag) intent.getParcelableExtra(EXTRA_TAG);
        if (tag != null) {
            return NativeApi.nativeNFCInit(new InitParameter(apiActivity, tag),initParams);
        }
        return -1;
    }

    public boolean isConnect() {
        return NativeApi.nativeNFCIsConnected(mDeviceHandle);
    }

    public boolean connect() {
        int[] hld = new int[1];
        int ret = NativeApi.nativeNFCConnect(hld);
        if (ret != 0) {
            return false;
        }
        mDeviceHandle = hld[0];
        return true;
    }

    public void onPause() {
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(apiActivity);
    }

    public void onResume() {
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(apiActivity, pendingIntent,
                    TAGFILTERS, TECHLISTS);
    }

    public int getNfcState() {
        return (nfcAdapter == null) ? -1 : nfcAdapter.isEnabled() ? 1 : 0;
    }

    public void onDestroy() {
        mNfcardOTPSDK = null;
    }

    private static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }
}