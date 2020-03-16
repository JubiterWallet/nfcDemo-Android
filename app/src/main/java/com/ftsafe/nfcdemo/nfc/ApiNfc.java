package com.ftsafe.nfcdemo.nfc;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.util.Log;


import com.ftsafe.nfcdemo.listener.ApiAsyncListener;
import com.ftsafe.nfcdemo.utils.Utils;

import static android.nfc.NfcAdapter.EXTRA_TAG;

/**
 * Created by lxl on 2019/4/22.
 */
public class ApiNfc {
    private static ApiNfc mApiNfcardOTP;
    private static ImplNfc apiImplNfcardOTP;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private static Context apiContext;
    private Activity apiActivity;

    private IsoDep apiIsodep = null;
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

    private ApiNfc(Activity mActivity) {
        apiContext = mActivity;
        apiActivity = mActivity;

        nfcAdapter = NfcAdapter.getDefaultAdapter(apiActivity);
        pendingIntent = PendingIntent.getActivity(apiActivity, 0, new Intent(
                apiActivity, apiActivity.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        apiImplNfcardOTP = new ImplNfc(apiContext);
    }

    public synchronized static ApiNfc getInstance(Activity mActivity) {
        mApiNfcardOTP = new ApiNfc(mActivity);
        return mApiNfcardOTP;
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

    public String getSdkVersion() {
        return apiImplNfcardOTP.getVersion();
    }

    public int setCardTag(Intent intent) {
        Tag tag = (Tag) intent.getParcelableExtra(EXTRA_TAG);
        if (tag != null) {
            apiIsodep = IsoDep.get(tag);
            apiImplNfcardOTP.ImplsetIsoDepTag(apiIsodep);
            return 0;
        }
        return -1;
    }

    public void transInstructions(byte[] instructions, ApiAsyncListener<String> listener) {
        TransInstructionsTask transInstructionsTask = new TransInstructionsTask(instructions, listener);
        transInstructionsTask.execute();
    }

    public void checkConnect(ApiAsyncListener<Void> listener){
        CheckConnectTask connectTask = new CheckConnectTask(listener);
        connectTask.execute();
    }

    public byte[] transInstructionsAsync(byte[] instructions) throws Exception {
        byte[] resultInput = new byte[1024];
        byte[] result;
        int[] resultLen = new int[1];

        int re;
        re = apiImplNfcardOTP.connect();
        if (re != 0) {
            throw new Exception("not connect");
        }
        re = apiImplNfcardOTP.transInstructions(instructions, resultInput, resultLen);
        if (re > 0) {
            throw new Exception("error " + re);
        }
        result = new byte[resultLen[0]];
        System.arraycopy(resultInput, 0, result, 0, resultLen[0]);
        return result;
    }

    static class TransInstructionsTask extends AsyncTask<Void, Void, Integer> {
        private byte[] transInstru;
        private byte[] resultInput = new byte[1024];
        private byte[] result;
        private int[] resultLen = new int[1];
        private ApiAsyncListener<String> mListener;

        public TransInstructionsTask(byte[] instru, ApiAsyncListener<String> listener) {
            mListener = listener;
            transInstru = instru;
        }

        @Override
        protected void onPreExecute() {
            mListener.onUiChange();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int re;
            re = apiImplNfcardOTP.connect();
            if (re != 0) {
                Log.e("connect error", re + "");
                return re;
            }
            re = apiImplNfcardOTP.transInstructions(transInstru, resultInput, resultLen);
            //apiImplNfcardOTP.disConnect();
            return re;
        }

        @Override
        protected void onPostExecute(Integer res) {

            if (resultLen[0] != 0) {
                result = new byte[resultLen[0]];
                System.arraycopy(resultInput, 0, result, 0, resultLen[0]);
                mListener.onResult(res, Bytes2HexString(result));
            } else {
                mListener.onResult(res, null);
            }
        }
    }

    static class CheckConnectTask extends AsyncTask<Void, Void, Integer> {
        private ApiAsyncListener<Void> mListener;

        public CheckConnectTask( ApiAsyncListener<Void> listener) {
            mListener = listener;
        }

        @Override
        protected void onPreExecute() {
            mListener.onUiChange();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int re;
            re = apiImplNfcardOTP.connect();
            return re;
        }

        @Override
        protected void onPostExecute(Integer res) {
            mListener.onResult(res, null);
        }
    }

    public static String Bytes2HexString(byte[] b) {
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

    public void onDestroy() {
        mApiNfcardOTP = null;
        apiContext = null;
    }
}