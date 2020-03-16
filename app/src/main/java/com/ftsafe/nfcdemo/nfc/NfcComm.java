package com.ftsafe.nfcdemo.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.util.Log;

import com.ftsafe.nfcdemo.utils.Utils;

import java.io.IOException;

/**
 * Created by lxl on 2019/4/22.
 */
public final class NfcComm {
    public static final int FT_SUCCESS = 0x00000000;
    public static final int FT_FAIL = 0x00000001;
    public static final int FT_RECV_DATA_SPECIAL = 0x00000002;
    public static final int FT_RECV_LEN_ERROR = 0x00000003;
    public static final int EXCP_COMM_CONNECT = 0x00000010;
    public static final int EXCP_COMM_DISCONNECT = 0x00000011;
    public static final int EXCP_COMM_TRANSCEIVE = 0x00000012;
    public static final int ERROR_NO_TAG = 0x10000000;
    public static final int ERROR_NO_NFC = 0x10000001;
    public static final int ERROR_NFC_DISABLED = 0x10000002;
    public static final int ERROR_RECV_DATA = 0x10000003;

    private IsoDep nfcTag = null;
    private Context mContext;
    private NfcAdapter nfcAdapter;

    public NfcComm(Context context) {
        mContext = context;
        nfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
    }

    public void setIsoDepTag(IsoDep tag) {
        nfcTag = tag;
    }

    public int transInstructions(byte[] instru, byte[] result, int[] resultLen) {
        int res = 0;
        byte[] COMMAND_RECV = new byte[1024];
        int[] COMMAND_RECV_LEN = new int[1];
        byte[] COMMAND = instru;
        res = transceive(COMMAND, COMMAND_RECV, COMMAND_RECV_LEN);
        if (res > 0) {
            return res;
        }
        if (COMMAND_RECV == null || COMMAND_RECV_LEN[0] == 0) {
            res = FT_FAIL;
            return res;
        }
        System.arraycopy(COMMAND_RECV, 0, result, 0, COMMAND_RECV_LEN[0]);
        if (resultLen.length != 0) {
            resultLen[0] = COMMAND_RECV_LEN[0];
        }
        if (COMMAND_RECV_LEN[0] > 1) {
			  /*if( ((COMMAND_RECV[0] &0xff) == 0x6C )&& ((COMMAND_RECV[1] &0xff) == 0x8F )){
					res = FT_RECV_DATA_SPECIAL;
			  }*/
        }
        return res;
    }

    public int connect() {
        if (nfcAdapter == null) {
            return ERROR_NO_NFC;
        } else if (!nfcAdapter.isEnabled()) {
            return ERROR_NFC_DISABLED;
        } else if (nfcTag == null) {
            return ERROR_NO_TAG;
        }
        try {
            if (!nfcTag.isConnected()) {
                nfcTag.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return EXCP_COMM_CONNECT;
        }
        return 0;
    }

    public int disConnect() {
        if (nfcTag == null) {
            return ERROR_NO_TAG;
        }
        try {
            nfcTag.close();
        } catch (IOException e) {
            e.printStackTrace();
            return EXCP_COMM_DISCONNECT;
        }
        return 0;
    }

    private static final String GET_RESPONSE = "80EA000000";
    private static final String WAIT_RESPONSE = "9100";

    private int transceive(byte[] send, byte[] recv, int[] recvLen) {
        if (nfcTag == null) {
            return ERROR_NO_TAG;
        }
        nfcTag.setTimeout(15000);
        byte[] rsp;
        byte[] sendData = send;
        byte[] getResponse = Utils.hexString2Bytes(GET_RESPONSE);
        int loopTimes = 0;
        while (true) {
            try {
                rsp = nfcTag.transceive(sendData);
            } catch (IOException e) {
                Log.e("NfcComm", e.getMessage());
                return EXCP_COMM_TRANSCEIVE;
            }
            if (rsp == null || rsp.length <= 0) {
                return ERROR_RECV_DATA;
            }
            String tempRe = Utils.byteArr2HexStr(rsp);
            if (WAIT_RESPONSE.equals(tempRe)) {
                sendData = getResponse;
                loopTimes++;
                continue;
            }
            break;
        }

        System.arraycopy(rsp, 0, recv, 0, rsp.length);
        recvLen[0] = rsp.length;
        return -loopTimes;
    }
}
