package com.ftsafe.nfcdemo.utils;

import java.math.BigInteger;

import static com.ftsafe.nfc.card.sdk.ApiNfc.ERROR_NFC_DISABLED;
import static com.ftsafe.nfc.card.sdk.ApiNfc.ERROR_NO_NFC;
import static com.ftsafe.nfc.card.sdk.ApiNfc.ERROR_NO_TAG;
import static com.ftsafe.nfc.card.sdk.ApiNfc.ERROR_RECV_DATA;
import static com.ftsafe.nfc.card.sdk.ApiNfc.EXCP_COMM_CONNECT;
import static com.ftsafe.nfc.card.sdk.ApiNfc.EXCP_COMM_DISCONNECT;
import static com.ftsafe.nfc.card.sdk.ApiNfc.EXCP_COMM_TRANSCEIVE;
import static com.ftsafe.nfc.card.sdk.ApiNfc.FT_RECV_LEN_ERROR;
import static com.ftsafe.nfc.card.sdk.ApiNfc.FT_SUCCESS;


public class Utils {

    public static byte[] hexString2Bytes(String hex) {
        return String2Bytes(hex, 16);
    }

    private static byte[] String2Bytes(String str, int digit) {
        byte[] bArray = new BigInteger("10" + str, digit).toByteArray();

        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bArray[i + 1];
        }

        return ret;
    }


    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * 将字节数组转换成十六进制的字符串
     * @param bt      字节数组
     * @param start   起始下标
     * @param end     终止下标
     */
    public static String byteArr2HexStr(byte[] bt, int start, int end) {
        return byteArr2HexStr(bt, start, end, "");
    }

    /**
     * 将字节数组转换成十六进制的字符串
     * @param bt      字节数组
     */
    public static String byteArr2HexStr(byte[] bt) {
        return byteArr2HexStr(bt, 0, bt.length, "");
    }

    /**
     * 将字节数组转换成十六进制的字符串
     * @param bt      字节数组
     * @param start   起始下标
     * @param end     终止下标
     * @param sep     每个字节之间的分割字符串
     */
    public static String byteArr2HexStr(byte[] bt, int start, int end, String sep) {
        if (bt == null || bt.length < end || start < 0 || start >= end)
            throw new RuntimeException("param format error");

        StringBuffer sb = new StringBuffer();
        for (int i = start; i < end; i++) {
            sb.append(byte2HexStr(bt[i])).append(sep);
        }
        return sb.toString();
    }

    /**
     *  将byte转换成对应的十六进制字符串（如：byte值0x3D转换成字符串"3D"）
     *  @return  返回字符串长度一定为2
     */
    public static String byte2HexStr(byte b) {
        int i = (b & 0xF0) >> 4;
        int j = (b & 0x0F);
        char c = (char)(i > 9 ? 'A' + i%10 : '0' + i);
        char d = (char)(j > 9 ? 'A' + j%10 : '0' + j);
        return "" + c + d;
    }

    public static String getError(int ret) {
        String msg;
        switch (ret) {
            case FT_SUCCESS:
                msg = "success";
                break;
            case FT_RECV_LEN_ERROR:
                msg = "error: receive data len error";
                break;
            case EXCP_COMM_CONNECT:
                msg = "error: connect except";
                break;
            case EXCP_COMM_DISCONNECT:
                msg = "error: disconnect except";
                break;
            case EXCP_COMM_TRANSCEIVE:
                msg = "error: transceive except";
                break;
            case ERROR_NO_TAG:
                msg = "error: no tag";
                break;
            case ERROR_NO_NFC:
                msg = "error: no nfc";
                break;
            case ERROR_NFC_DISABLED:
                msg = "error: nfc disabled";
                break;
            case ERROR_RECV_DATA:
                msg = "error: receive data error";
                break;
            default:
                msg = "error : other error";
                break;
        }
        return msg;
    }
}
