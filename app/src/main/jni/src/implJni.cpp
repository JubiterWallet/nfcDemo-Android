//
// Created by FT on 2018/4/16.
//

#include <jni.h>
#include <logUtils.h>
#include <utils.h>
#include <json/json.h>
#include "../JubiterSDK_C/include/JUB_SDK.h"

// 保存 JavaVM
JavaVM *g_vm = NULL;
jobject jInitPara;
int errorCode = 0;

JNIEXPORT jint JNICALL native_getErrorCode(JNIEnv *env, jclass obj) {
    return errorCode;
}


//================================= 蓝牙 ================================================

JNIEXPORT jint JNICALL native_initDevice(JNIEnv *env, jclass obj) {
    DEVICE_INIT_PARAM initParam;
    // 初始化参数转换
    jobjectToBTInitParam(env, g_vm, &initParam);

    JUB_RV rv = JUB_initDevice(initParam);
    if (rv != JUBR_OK) {
        LOG_ERR("JUB_initDevice ret: %08lx", rv);
        return rv;
    }
    return 0;
}

JNIEXPORT jint JNICALL native_startScan(JNIEnv *env, jclass obj, jobject scanCallback) {
    jobject objParam = env->NewGlobalRef(scanCallback);
    setScanCallbackObj(g_vm, objParam);

    JUB_RV rv = JUB_enumDevices();
    if (rv != JUBR_OK) {
        LOG_ERR("JUB_enumDevices rv: %08lx", rv);
        return rv;
    }
    return 0;
}

JNIEXPORT jint JNICALL native_stopScan(JNIEnv *env,jclass obj) {
    JUB_RV rv = JUB_stopEnumDevices();
    if (rv != JUBR_OK) {
        LOG_ERR("JUB_stopEnumDevices rv: %08lx", rv);
        return rv;
    }
    return 0;
}

JNIEXPORT jint JNICALL native_connectDevice(JNIEnv *env, jclass obj, jstring address, jint devType, jintArray handle,
                                            jint timeout, jobject disCallback) {
    JUB_BYTE_PTR pAddress  = (JUB_BYTE_PTR) (env->GetStringUTFChars(address, NULL));
    JUB_UINT16_PTR pHandle  = reinterpret_cast<JUB_UINT16_PTR>(env->GetIntArrayElements(handle, NULL));

    jobject objParam = env->NewGlobalRef(disCallback);
    setDiscCallbackObj(g_vm, objParam);

    JUB_RV rv = JUB_connectDevice(pAddress, devType, pHandle, timeout);
    env->ReleaseStringUTFChars(address, reinterpret_cast<const char *>(pAddress));
    env->ReleaseIntArrayElements(handle, reinterpret_cast<jint *>(pHandle), 0);
    if (rv != JUBR_OK) {
        LOG_ERR("native_connectDevice rv: %08lx", rv);
        return rv;
    }
    return 0;
}


JNIEXPORT jint JNICALL native_disconnectDevice(JNIEnv *env, jclass obj, jlong deviceHandle) {
    JUB_RV rv = JUB_disconnectDevice(deviceHandle);
    if (rv != 0) {
        LOG_ERR("JUB_disconnectDevice rv: %08lx", rv);
    }
    return rv;
}

JNIEXPORT jint JNICALL native_isConnectDevice(JNIEnv *env,jclass obj, jlong deviceHandle) {
    JUB_RV rv = JUB_isDeviceConnect(deviceHandle);
    if (rv != JUBR_OK) {
        LOG_ERR("JUB_isDeviceConnect rv: %08lx", rv);
    }
    return rv;
}

//==========================================NFC========================================

JNIEXPORT jint JNICALL native_nfcInit(JNIEnv *env,jclass obj, jobject initPara, jstring paramJson){

#define CRT         "crt"
#define SK          "sk"
#define KEY_LENGTH  "keyLength"
#define HOST_ID     "hostID"

    jInitPara = initPara;
    if (jInitPara == NULL){
        return -1;
    }

    if (NULL == paramJson) {
        return JUBR_ARGUMENTS_BAD;
    }

    int length = env->GetStringLength(paramJson);

    if (0 == length) {
        errorCode = JUBR_ARGUMENTS_BAD;
        return JUBR_ARGUMENTS_BAD;
    }

    NFC_DEVICE_INIT_PARAM initParam;
    jobjectToNFCInitParam(env, g_vm, &initParam);

    JUB_CHAR_PTR pJSON = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(paramJson, NULL));

    Json::Reader reader;
    Json::Value root;
    reader.parse(pJSON, root);
    initParam.crt = (char *) root[CRT].asCString();
    initParam.sk = (char *) root[SK].asCString();
    initParam.hostID = (char *) root[HOST_ID].asCString();
    initParam.keyLength = root[KEY_LENGTH].asUInt();

    return static_cast<jint>(JUB_initNFCDevice(initParam));
}

JNIEXPORT jint JNICALL native_nfcConnect(JNIEnv *env, jclass obj,  jintArray handle){
    JUB_UINT16_PTR pHandle  = reinterpret_cast<JUB_UINT16_PTR>(env->GetIntArrayElements(handle, NULL));
    JUB_RV rv = JUB_connectNFCDevice((JUB_BYTE_PTR) "123456", pHandle);
    if (rv != JUBR_OK) {
        LOG_ERR("native_connectDevice rv: %08lx", rv);
        env->ReleaseIntArrayElements(handle, reinterpret_cast<jint *>(pHandle), 0);
        return static_cast<jint>(rv);
    }
    env->ReleaseIntArrayElements(handle, reinterpret_cast<jint *>(pHandle), 0);
    return static_cast<jint>(rv);
}

JNIEXPORT jint JNICALL native_nfcDisconnect(JNIEnv *env, jclass obj, jlong deviceHandle){
    return static_cast<jint>(JUB_disconnectNFCDevice(static_cast<JUB_UINT16>(deviceHandle)));
}

JNIEXPORT jboolean JNICALL native_nfcIsConnected(JNIEnv *env, jclass obj,jlong deviceHandle){
    JUB_RV rv = JUB_isDeviceNFCConnect(static_cast<JUB_UINT16>(deviceHandle));
    if (rv != JUBR_OK) {
        LOG_ERR("JUB_isDeviceNFCConnect rv: %08lx", rv);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

//=======================================================================================

JNIEXPORT jstring JNICALL native_sendAPDU(JNIEnv *env, jclass obj, jlong deviceID,
                                          jstring jApdu) {
    JUB_CHAR_PTR pApdu = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jApdu, NULL));
    JUB_CHAR_PTR response = nullptr;
    JUB_RV ret = JUB_SendOneApdu(static_cast<JUB_UINT16>(deviceID), pApdu, &response);
    env->ReleaseStringUTFChars(jApdu, reinterpret_cast<const char *>(pApdu));
    if (ret == JUBR_OK) {
        jstring result = env->NewStringUTF(response);
        JUB_FreeMemory(response);
        return result;
    } else {
        return NULL;
    }
}

JNIEXPORT jint JNICALL native_reset(JNIEnv *env,jclass obj, jlong deviceID) {
    JUB_RV ret = JUB_Reset(static_cast<JUB_UINT16>(deviceID));
    if (ret != JUBR_OK) {
        LOG_ERR("JUB_Reset: %08x", ret);
    }
    return static_cast<jint>(ret);
}

JNIEXPORT jint JNICALL
native_verifyPIN(JNIEnv *env, jclass obj, jlong contextID, jbyteArray jPin) {
    JUB_CHAR_PTR pPin = (JUB_CHAR_PTR) (env->GetByteArrayElements(jPin, NULL));
    int length = env->GetArrayLength(jPin);

    // java数组没有结束符，jni层需补充
    *(pPin + length) = '\0';
    JUB_ULONG retry;
    JUB_RV ret = JUB_VerifyPIN(static_cast<JUB_UINT16>(contextID), pPin, &retry);
    if (ret != JUBR_OK) {
        LOG_ERR("JUB_VerifyPIN: %08x", ret);
    }
    env->ReleaseByteArrayElements(jPin, (jbyte *) pPin, JNI_ABORT);
    return static_cast<jint>(ret);
}

JNIEXPORT jint JNICALL
native_changePIN(JNIEnv *env, jclass obj, jlong deviceID, jbyteArray jPin, jbyteArray jNewPin) {
    JUB_CHAR_PTR pPin = (JUB_CHAR_PTR) (env->GetByteArrayElements(jPin, NULL));
    JUB_CHAR_PTR pNewPin = (JUB_CHAR_PTR) (env->GetByteArrayElements(jNewPin, NULL));
    int length = env->GetArrayLength(jPin);
    int nLength = env->GetArrayLength(jNewPin);

    // java数组没有结束符，jni层需补充
    *(pPin + length) = '\0';
    *(pNewPin + nLength) = '\0';

    JUB_RV ret = JUB_ChangePIN(static_cast<JUB_UINT16>(deviceID), pPin, pNewPin);
    if (ret != JUBR_OK) {
        LOG_ERR("JUB_ChangePIN: %08x", ret);
    }
    env->ReleaseByteArrayElements(jPin, (jbyte *) pPin, JNI_ABORT);
    env->ReleaseByteArrayElements(jNewPin, (jbyte *) pNewPin, JNI_ABORT);
    return static_cast<jint>(ret);
}

JNIEXPORT jint JNICALL
native_generateSeed(JNIEnv *env, jclass obj, jlong deviceID, jstring jPin) {
    JUB_CHAR_PTR pPin = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jPin, NULL));
    JUB_RV ret = JUB_GenerateSeed(static_cast<JUB_UINT16>(deviceID), pPin,
                                  JUB_ENUM_CURVES::SECP256K1);
    if (ret != JUBR_OK) {
        LOG_ERR("JUB_GenerateSeed: %08x", ret);
    }
    env->ReleaseStringUTFChars(jPin, reinterpret_cast<const char *>(pPin));
    return static_cast<jint>(ret);
}

JNIEXPORT jint JNICALL
native_importMnemonic(JNIEnv *env, jclass obj,
        jlong deviceID, jstring jPin,jstring jMnemonic) {

    JUB_CHAR_PTR pPin = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jPin, NULL));
    JUB_CHAR_PTR pMnemonic = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jMnemonic, NULL));


    JUB_RV ret = JUB_ImportMnemonic(static_cast<JUB_UINT16>(deviceID),
                                pPin, pMnemonic);
    env->ReleaseStringUTFChars(jPin, reinterpret_cast<const char *>(pPin));
    env->ReleaseStringUTFChars(jMnemonic, reinterpret_cast<const char *>(pMnemonic));

    if (ret != JUBR_OK) {
        LOG_ERR("JUB_ImportMnemonic: %08x", ret);
    }
    return static_cast<jint>(ret);
}

JNIEXPORT jstring JNICALL
native_exportMnemonic(JNIEnv *env, jclass obj,
        jlong deviceID, jstring jPin) {

    JUB_CHAR_PTR pPin = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jPin, NULL));
    JUB_CHAR_PTR mnemonic = nullptr;
    JUB_RV ret = JUB_ExportMnemonic(static_cast<JUB_UINT16>(deviceID),pPin,&mnemonic);
    if (ret == JUBR_OK) {
        jstring result = env->NewStringUTF(mnemonic);
        JUB_FreeMemory(mnemonic);
        return result;
    } else {
        errorCode = static_cast<int>(ret);
        return NULL;
    }
}

JNIEXPORT jstring JNICALL
native_getDeviceInfo(JNIEnv *env, jclass obj,
                      jlong deviceID) {
    JUB_DEVICE_INFO info;
    JUB_RV rv = JUB_GetDeviceInfo((JUB_UINT16) deviceID, &info);
    if (rv == JUBR_OK) {
        Json::FastWriter writer;
        Json::Value root;
        root["sn"] = info.sn;
        root["label"] = info.label;
        root["pinRetry"] = info.pinRetry;
        root["pinMaxRetry"] = info.pinMaxRetry;
        root["bleVersion"] = info.bleVersion;
        root["firmwareVersion"] = info.firmwareVersion;
        jstring result = env->NewStringUTF(writer.write(root).c_str());
        return  result;
    }
    errorCode = static_cast<int>(rv);
    return NULL;
}


JNIEXPORT jstring JNICALL native_getDeviceCert(JNIEnv *env, jclass obj, jlong deviceID) {
    JUB_CHAR_PTR cert = NULL;
    JUB_RV rv = JUB_GetDeviceCert((JUB_UINT16) deviceID, &cert);
    if (rv == JUBR_OK) {
        jstring result = env->NewStringUTF(cert);
        JUB_FreeMemory(cert);
        return result;
    } else {
        errorCode = static_cast<int>(rv);
        LOG_ERR("JUB_GetDeviceCert error");
        return NULL;
    }
}


JNIEXPORT jstring JNICALL native_enumApplets(JNIEnv *env, jclass obj, jlong deviceID) {

    JUB_CHAR_PTR list = NULL;
    JUB_RV rv = JUB_EnumApplets((JUB_UINT16) deviceID, &list);
    if (rv == JUBR_OK) {
        jstring result = env->NewStringUTF(list);
        JUB_FreeMemory(list);
        return result;
    } else {
        errorCode = static_cast<int>(rv);
        LOG_ERR("JUB_EnumApplets error");
        return NULL;
    }
}

//===================================== BTC ============================================

JNIEXPORT jint JNICALL
native_BTCCreateContext(JNIEnv *env, jclass obj, jintArray jContextId, jstring jJSON,
                        jlong deviceInfo) {

#define MAIN_PATH      "main_path"
#define P2SH_SEGWIT    "p2sh_segwit"
#define COIN_TYPE_BTC  "coin_type"

    if (NULL == jJSON) {
        return JUBR_ARGUMENTS_BAD;
    }

    int length = env->GetStringLength(jJSON);
    if (0 == length) {
        errorCode = JUBR_ARGUMENTS_BAD;
        return JUBR_ARGUMENTS_BAD;
    }

    JUB_UINT16 *pContextID = (JUB_UINT16 *) env->GetIntArrayElements(jContextId, NULL);
    JUB_CHAR_PTR pJSON = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jJSON, NULL));

    Json::Reader reader;
    Json::Value root;
    reader.parse(pJSON, root);

    CONTEXT_CONFIG_BTC cfg;
    cfg.mainPath = (char *) root[MAIN_PATH].asCString();
    int cointype = root[COIN_TYPE_BTC].asInt();
    switch (cointype) {
        case 0x00:
            cfg.coinType = COINBTC;
            break;
        case 0x01:
            cfg.coinType = COINBCH;
            break;
        case 0x02:
            cfg.coinType = COINLTC;
            break;
        case 0x03:
            cfg.coinType = COINUSDT;
            break;
        case 0x04:
            cfg.coinType = COINDASH;
            break;
        case 0x05:
            cfg.coinType = COINQTUM;
            break;
        default:
            cfg.coinType = COINBTC;
            break;
    }

    JUB_RV rv = JUBR_OK;
    if (COINBCH == cfg.coinType) {
        cfg.transType = p2pkh;
    } else {
        if (root[P2SH_SEGWIT].asBool()) {
            cfg.transType = p2sh_p2wpkh;
        } else {
            cfg.transType = p2pkh;
        }
    }
    rv = JUB_CreateContextBTC(cfg, deviceInfo, pContextID);

    if (rv != JUBR_OK) {
        LOG_ERR("JUB_CreateContextBTC: %08x", rv);
        errorCode = rv;
    } else {
        LOG_INF("contextID: %d", *pContextID);
    }
    env->ReleaseIntArrayElements(jContextId, (jint *) pContextID, 0);
    return rv;
}

JNIEXPORT jobjectArray JNICALL
native_BTCGetAddress(JNIEnv *env, jclass obj, jlong contextID, jstring jJSON) {

#define BIP32_PATH   "bip32_path"
#define CHANGE       "change"
#define INDEX        "addressIndex"

    jclass clazz = env->FindClass("java/lang/String");
    if (clazz == NULL) {
        LOG_ERR("clazz == NULL");
        return NULL;
    }
    JUB_CHAR_PTR pJSON = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jJSON, FALSE));

    Json::Reader reader;
    Json::Value root;
    reader.parse(pJSON, root);

    int input_number = root[BIP32_PATH].size();
    jobjectArray array = env->NewObjectArray(2 * input_number, clazz, 0);
    for (int i = 0; i < input_number; i++) {
        JUB_CHAR_PTR xpub;

        BIP44_Path path;
        path.change = (JUB_ENUM_BOOL) root[BIP32_PATH][i][CHANGE].asBool();
        path.addressIndex = static_cast<JUB_UINT64>(root[BIP32_PATH][i][INDEX].asInt());

        JUB_RV rv = JUB_GetHDNodeBTC(contextID, path, &xpub);
        if (rv != JUBR_OK) {
            LOG_ERR("JUB_GetHDNodeBTC: %08x", rv);
            errorCode = static_cast<int>(rv);
            env->SetObjectArrayElement(array, 2 * i, NULL);
            env->SetObjectArrayElement(array, 2 * i + 1, NULL);
        } else {
            jstring jsXpub = env->NewStringUTF(xpub);
            JUB_CHAR_PTR pAddress = NULL;
            rv = JUB_GetAddressBTC(contextID, path, BOOL_FALSE,
                                   &pAddress);
            if (rv != JUBR_OK) {
                LOG_ERR("JUB_GetAddressBTC: %08x", rv);
                errorCode = static_cast<int>(rv);
                env->SetObjectArrayElement(array, 2 * i, jsXpub);
                env->SetObjectArrayElement(array, 2 * i + 1, NULL);
            } else {
                jstring address = env->NewStringUTF(pAddress);
                env->SetObjectArrayElement(array, 2 * i, jsXpub);
                env->SetObjectArrayElement(array, 2 * i + 1, address);
            }
        }
    }
    return array;
}

JNIEXPORT jstring JNICALL
native_BTCTransaction(JNIEnv *env, jclass obj, jlong contextID,jstring jJSON) {
#define VERSION       "ver"
#define INPUTS       "inputs"
#define PREHASH      "preHash"
#define PREINDEX     "preIndex"
#define BIP32_PATH   "bip32_path"
#define CHANGE       "change"
#define INDEX        "addressIndex"
#define AMOUNT       "amount"

#define OUTPUTS      "outputs"
#define ADDRESS      "address"
#define CHANGE_ADDRESS "change_address"

    JUB_CHAR_PTR pJSON = const_cast<JUB_CHAR_PTR>(env->GetStringUTFChars(jJSON, NULL));
    Json::Reader reader;
    Json::Value root;
    reader.parse(pJSON, root);

    JUB_UINT32 version = root[VERSION].asInt();

    std::vector<INPUT_BTC> inputs;
    std::vector<OUTPUT_BTC> outputs;

    int input_number = root[INPUTS].size();
    for (int i = 0; i < input_number; i++) {
        INPUT_BTC input;
        // 根据全局变量赋值
        input.type = P2PKH;
//        input.type = SCRIPT_BTC_TYPE(root[INPUTS][i][MULTISIG].asInt());
        input.preHash = (char *) root[INPUTS][i][PREHASH].asCString();
        input.preIndex = static_cast<JUB_UINT16>(root[INPUTS][i][PREINDEX].asInt());
        input.path.change = (JUB_ENUM_BOOL) root[INPUTS][i][BIP32_PATH][CHANGE].asBool();
        input.path.addressIndex = static_cast<JUB_UINT64>(root[INPUTS][i][BIP32_PATH][INDEX].asInt());
        input.amount = static_cast<JUB_UINT64>(root[INPUTS][i][AMOUNT].asUInt64());
        inputs.push_back(input);
    }

    int output_number = root[OUTPUTS].size();
    for (int i = 0; i < output_number; i++) {
        OUTPUT_BTC output;
        // 根据全局变量赋值
        output.type = P2PKH;
//        output.type = SCRIPT_BTC_TYPE(root[OUTPUTS][i][MULTISIG].asInt());
        output.stdOutput.address = (char *) root[OUTPUTS][i][ADDRESS].asCString();
        output.stdOutput.amount = static_cast<JUB_UINT64>(root[OUTPUTS][i][AMOUNT].asUInt64());
        output.stdOutput.changeAddress = (JUB_ENUM_BOOL) root[OUTPUTS][i][CHANGE_ADDRESS].asBool();
        if (output.stdOutput.changeAddress) {
            output.stdOutput.path.change = (JUB_ENUM_BOOL) root[OUTPUTS][i][BIP32_PATH][CHANGE].asBool();
            output.stdOutput.path.addressIndex = static_cast<JUB_UINT64>(root[OUTPUTS][i][BIP32_PATH][INDEX].asInt());
        }
        outputs.push_back(output);
    }

    char *raw = NULL;
    JUB_SetUnitBTC(static_cast<JUB_UINT16>(contextID), BTC);

    JUB_RV rv = JUB_SignTransactionBTC(static_cast<JUB_UINT16>(contextID),
                                       version,
                                       &inputs[0], (JUB_UINT16) inputs.size(),
                                       &outputs[0], (JUB_UINT16) outputs.size(), 0, &raw);

    if (rv != JUBR_OK) {
        errorCode = static_cast<int>(rv);
        return NULL;
    }
    jstring rawString = env->NewStringUTF(raw);
    JUB_FreeMemory(raw);
    return rawString;
}


/**
 * JNINativeMethod由三部分组成:
 * (1)Java中的函数名;
 * (2)函数签名,格式为(输入参数类型)返回值类型;
 * (3)native函数名
 */
static JNINativeMethod gMethods[] = {
        {
                "nativeGetErrorCode",
                "()I",
                (void *) native_getErrorCode
        },
        {
                "nativeInitDevice",
                "()I",
                (void *) native_initDevice
        },
        {
                "nativeStartScan",
                "(Lcom/ftsafe/core/device/ble/InnerScanCallback;)I",
                (void *) native_startScan
        },
        {
                "nativeStopScan",
                "()I",
                (void *) native_stopScan
        },
        {
                "nativeConnectDevice",
                "(Ljava/lang/String;I[IILcom/ftsafe/core/device/ble/InnerDiscCallback;)I",
                (void *) native_connectDevice
        },
        {
                "nativeDisconnect",
                "(J)I",
                (void *) native_disconnectDevice
        },
        {
                "nativeIsConnected",
                "(J)I",
                (void *) native_isConnectDevice
        },
        {
                "nativeNFCInit",
                "(Lcom/ftsafe/nfc/card/sdk/InitParameter;Ljava/lang/String;)I",
                (void *) native_nfcInit
        },
        {
                "nativeNFCConnect",
                "([I)I",
                (void *) native_nfcConnect
        },
        {
                "nativeNFCDisconnect",
                "(J)I",
                (void *) native_nfcDisconnect
        },
        {
                "nativeNFCIsConnected",
                "(J)Z",
                (void *) native_nfcIsConnected
        },
        {
                "nativeReset",
                "(J)I",
                (void *) native_reset
        },{
                "nativeSendApdu",
                "(JLjava/lang/String;)Ljava/lang/String;",
                (void *) native_sendAPDU
        },
        {
                "nativeVerifyPIN",
                "(J[B)I",
                (void *) native_verifyPIN
        },
        {
                "nativeChangePIN",
                "(J[B[B)I",
                (void *) native_changePIN
        },
        {
                "nativeGenerateSeed",
                "(JLjava/lang/String;)I",
                (void *) native_generateSeed
        },
        {
                "nativeImportMnemonic",
                "(JLjava/lang/String;Ljava/lang/String;)I",
                (void *) native_importMnemonic
        },
        {
                "nativeExportMnemonic",
                "(JLjava/lang/String;)Ljava/lang/String;",
                (void *) native_exportMnemonic
        },
        {
                "nativeGetDeviceInfo",
                "(J)Ljava/lang/String;",
                (void *) native_getDeviceInfo
        },
        {
                "nativeGetDeviceCert",
                "(J)Ljava/lang/String;",
                (void *) native_getDeviceCert
        },
        {
                "nativeEnumApplets",
                "(J)Ljava/lang/String;",
                (void *) native_enumApplets
        },
        // BTC
        {
                "nativeBTCCreateContext",
                "([ILjava/lang/String;J)I",
                (void *) native_BTCCreateContext
        },
        {
                "nativeBTCGetAddress",
                "(JLjava/lang/String;)[Ljava/lang/String;",
                (void *) native_BTCGetAddress
        },
        {
                "nativeBTCTransaction",
                "(JLjava/lang/String;)Ljava/lang/String;",
                (void *) native_BTCTransaction
        },
};


#define NATIVE_API_CLASS "com/ftsafe/core/NativeApi"

/**
 * JNI_OnLoad 默认会在 System.loadLibrary 过程中自动调用到，因而可利用此函数，进行动态注册
 * JNI 版本的返回视对应 JDK 版本而定
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint ret = JNI_FALSE;

    // 获取 env 指针
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        return ret;
    }

    // 保存全局 JVM 以便在动态注册的皆空中使用 env 环境
    env->GetJavaVM(&g_vm);

    // 获取类引用
    jclass clazz = env->FindClass(NATIVE_API_CLASS);
    if (clazz == NULL) {
        return ret;
    }

    // 注册 JNI 方法
    if (env->RegisterNatives(clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0])) < JNI_OK) {
        return ret;
    }
    // 成功
    return JNI_VERSION_1_6;
}