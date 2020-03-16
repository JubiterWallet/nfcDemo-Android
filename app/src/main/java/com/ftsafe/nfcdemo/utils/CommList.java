package com.ftsafe.nfcdemo.utils;


import java.util.ArrayList;
import java.util.List;

public class CommList {

    public static final String BOOTLOADER =         "00A40400";
    public static final String GET_APP_LIST =       "80CB800005DFFF028106";
    public static final String APDU_HEADER =        "80CB8000";
    public static final String IMPORT_MNEMONIC =    "DFFE";
    public static final String IMPORT_MNEMONIC_DATA_TAG = "8202";
    public static final String IMPORT_MNEMONIC_DATA_ENTROPY =   "1000000000000000000000000000000000";
    //public static final String IMPORT_MNEMONIC_DATA_ENTROPY =   "10042BA54A95B4E29E89A10F7BFA6F1166";

    public static final String EXPORT_MNEMONIC =    "DFFF";
    public static final String EXPORT_MNEMONIC_DATA_TAG = "8202";
    public static final String GENERATE_SEED =      "DFFE";
    public static final String GENERATE_SEED_DATA_TAG =  "8203";
    public static final String CHANGE_PIN =         "80CB800010DFFE0D82040A";
    public static final String RESET_WALLET =       "80CB800005DFFE02820500";
    public static final String GET_AVA_MEMORY =     "80CB800005DFFE028146";
    public static final String VERIFY_PIN =         "00200300";
    public static final String GET_DATA =           "00f70000";
    public static final String GET_DATA_LEN_TAG =   "08";
    public static final String GET_DATA_TIME_TAG =  "07";

    public static final String SELECT_NFC =         "00A4040010D1560001328300424C4400006E666301";

    public static final List<String> BTC_GET_XPUB_KEYS = new ArrayList<>();
    public static final List<String> BTC_GET_XPUB_KEYS_NEW = new ArrayList<>();
    public static final List<String> BTC_SIGN = new ArrayList<>();
    public static final List<String> BTC_SIGN_NEW = new ArrayList<>();

    public static final List<String> ETH_GET_XPUB_KEYS = new ArrayList<>();
    public static final List<String> ETH_GET_XPUB_KEYS_NEW = new ArrayList<>();
    public static final List<String> ETH_SIGN = new ArrayList<>();
    public static final List<String> ETH_SIGN_NEW = new ArrayList<>();

    static {
        BTC_GET_XPUB_KEYS.add("00e6000011080f6d2f3434272f30272f30272f302f30");
        BTC_GET_XPUB_KEYS.add("00e6000011080f6d2f3434272f30272f30272f312f30");
        BTC_GET_XPUB_KEYS.add("00E6000011080F6d2f3434272f30272f30272f322f30");
        BTC_GET_XPUB_KEYS.add("00E6000011080F6d2f3434272f30272f30272f332f30");
        BTC_GET_XPUB_KEYS.add("00E6000011080F6d2f3434272f30272f30272f342f30");

        BTC_GET_XPUB_KEYS_NEW.add("00f6000011080f6d2f3434272f30272f30272f302f30");
        BTC_GET_XPUB_KEYS_NEW.add("00f6000011080f6d2f3434272f30272f30272f312f30");
        BTC_GET_XPUB_KEYS_NEW.add("00f6000011080F6d2f3434272f30272f30272f322f30");
        BTC_GET_XPUB_KEYS_NEW.add("00f6000011080F6d2f3434272f30272f30272f332f30");
        BTC_GET_XPUB_KEYS_NEW.add("00f6000011080F6d2f3434272f30272f30272f342f30");

        BTC_SIGN.add("00e6000011080f6d2f3434272f30272f30272f302f30");
        BTC_SIGN.add("00e6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN.add("00e6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN.add("00e6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN.add("00e6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN.add("00f80000a9050781a52004b02833ac3bbd6fcae8469454734be6360bf8292037a5dbe08582e6893fa74f204bede1b678791efc96da6d8dcce53ab178a71e7b1fe92534fa326a554041abba20b2fb8705e61b29aaf89086c5b725442bf9d358038361c058ea78ed45c3bbe2ac205e8968c5656e023d24ef89a43a4b724630ee10d9940a30e683d7da54548c0c25203ad6228caed1b43793ca8c016b703abad9f36db0c0d8b15dc57817ee8af955c8");
        BTC_SIGN.add("00f80300520f500f6d2f3434272f30272f30272f302f300f6d2f3434272f30272f30272f312f300f6d2f3434272f30272f30272f312f300f6d2f3434272f30272f30272f312f300f6d2f3434272f30272f30272f312f30");
        BTC_SIGN.add("00ca0000");
        BTC_SIGN.add("00f9000042");
        BTC_SIGN.add("00f9004242");
        BTC_SIGN.add("00f9008442");
        BTC_SIGN.add("00f900c642");
        BTC_SIGN.add("00f9010842");

        BTC_SIGN_NEW.add("00f6000011080f6d2f3434272f30272f30272f302f30");
        BTC_SIGN_NEW.add("00f6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN_NEW.add("00f6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN_NEW.add("00f6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN_NEW.add("00f6000011080f6d2f3434272f30272f30272f312f30");
        BTC_SIGN_NEW.add("00f9000025050107212004b02833ac3bbd6fcae8469454734be6360bf8292037a5dbe08582e6893fa74f");
        BTC_SIGN_NEW.add("00f903001208100f6d2f3434272f30272f30272f302f30");
        BTC_SIGN_NEW.add("00cb0000");

        ETH_GET_XPUB_KEYS.add("00e600001208106d2f3434272f3630272f30272f302f30");
        ETH_GET_XPUB_KEYS.add("00E600001208106d2f3434272f3630272f30272f312f30");
        ETH_GET_XPUB_KEYS.add("00E600001208106d2f3434272f3630272f30272f322f30");
        ETH_GET_XPUB_KEYS.add("00E600001208106d2f3434272f3630272f30272f332f30");
        ETH_GET_XPUB_KEYS.add("00E600001208106d2f3434272f3630272f30272f342f30");
        ETH_GET_XPUB_KEYS.add("00E600001208106d2f3434272f3630272f30272f352f30");

        ETH_GET_XPUB_KEYS_NEW.add("00f600001208106d2f3434272f3630272f30272f302f30");
        ETH_GET_XPUB_KEYS_NEW.add("00f600001208106d2f3434272f3630272f30272f312f30");
        ETH_GET_XPUB_KEYS_NEW.add("00f600001208106d2f3434272f3630272f30272f322f30");
        ETH_GET_XPUB_KEYS_NEW.add("00f600001208106d2f3434272f3630272f30272f332f30");
        ETH_GET_XPUB_KEYS_NEW.add("00f600001208106d2f3434272f3630272f30272f342f30");
        ETH_GET_XPUB_KEYS_NEW.add("00f600001208106d2f3434272f3630272f30272f352f30");

        ETH_SIGN.add("00e600001208106d2f3434272f3630272f30272f302f30");
        ETH_SIGN.add("00f80000240107212048a34cc86c2a427148099e4f417ec2cda741560f157a1e36afa91935a779c00d");
        ETH_SIGN.add("00f80300130f11106d2f3434272f3630272f30272f302f30");
        ETH_SIGN.add("00ca00ee");

        ETH_SIGN_NEW.add("00e600001208106d2f3434272f3630272f30272f302f30");
        ETH_SIGN_NEW.add("00f80000240107212048a34cc86c2a427148099e4f417ec2cda741560f157a1e36afa91935a779c00d");
        ETH_SIGN_NEW.add("00f80300130f11106d2f3434272f3630272f30272f302f30");
        ETH_SIGN_NEW.add("00ca00ee");
    }
}
