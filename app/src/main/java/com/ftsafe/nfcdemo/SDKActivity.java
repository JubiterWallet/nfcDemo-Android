package com.ftsafe.nfcdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ftsafe.core.NativeApi;
import com.ftsafe.core.NfcSDK;
import com.ftsafe.nfcdemo.utils.JSONParseUtils;
import com.ftsafe.nfcdemo.utils.TipDialog;
import com.ftsafe.nfcdemo.utils.VerifyPinDialog;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.Charset;
import java.util.Arrays;


public class SDKActivity extends AppCompatActivity {

    private NfcSDK mApiNfcardOTP;
    private EditText mEditApdu, mEditPin;
    private String mPINStr;
    private ProgressDialog mDialog;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk);

        mEditApdu = findViewById(R.id.edit_apdu);
        mEditPin = findViewById(R.id.edit_pin);

        mApiNfcardOTP = NfcSDK.getInstance(this);
        mDialog = new ProgressDialog(this);

        int re = mApiNfcardOTP.init(getIntent(), JSONParseUtils.getJsonStr(SDKActivity.this, "initParams.json"));
        if (re == 0) {
            Toast.makeText(this, "NFC hardware touched.", Toast.LENGTH_SHORT).show();
            boolean connect = mApiNfcardOTP.connect();
            if (connect) {
                isConnected = true;
                Toast.makeText(this, "NFC hardware connected.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int re = mApiNfcardOTP.init(intent, JSONParseUtils.getJsonStr(SDKActivity.this, "initParams.json"));
        if (re == 0) {
            Toast.makeText(this, "NFC hardware touched.", Toast.LENGTH_SHORT).show();
            boolean connect = mApiNfcardOTP.connect();
            if (connect) {
                isConnected = true;
                Toast.makeText(this, "NFC hardware connected.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "NFC init error.", Toast.LENGTH_SHORT).show();
        }
    }


    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_send_apdu:
                sendApdu();
                break;
            case R.id.btn_rest:
                reset();
                break;
            case R.id.btn_import_mnemonic:
                importMnemonic();
                break;
            case R.id.btn_export_mnemonic:
                exportMnemonic();
                break;
            case R.id.btn_generate_seed:
                generateSeed();
                break;
            case R.id.btn_change_pin:
                changePin();
                break;
            case R.id.btn_device_info:
                getDeviceInfo();
                break;
            case R.id.btn_device_cert:
                getDeviceCert();
                break;
            case R.id.btn_applets:
                enumApplets();
                break;
            case R.id.btn_get_address:
                btcGetAddress();
                break;
            case R.id.btn_btc_trans:
                btcTrans();
                break;
            case R.id.btn_verify_pin:
                btcVerifyPin();
                break;
            case R.id.btn_just_trans:
                btcTransOnly();
                break;
            default:
                break;
        }
    }

    interface PinCallback {
        void onSuccess();
    }

    private void getPin(final PinCallback callback) {
        String pin = mEditPin.getText().toString().trim().replace(" ", "");
        int length = pin.length();
        if (length >= 4 && length <= 8) {
            mPINStr = pin;
            callback.onSuccess();
        } else {
            Toast.makeText(SDKActivity.this, "Error length", Toast.LENGTH_SHORT).show();
        }
    }

    private void getPin(String title, final PinCallback callback) {
        new VerifyPinDialog(this, title, new VerifyPinDialog.callback() {
            @Override
            public void onClickListener(String pin) {
                int length = pin.length();
                if (length >= 4 && length <= 8) {
                    mPINStr = pin;
                    callback.onSuccess();
                } else {
                    Toast.makeText(SDKActivity.this, "Error length", Toast.LENGTH_SHORT).show();
                }
            }
        }).show();
    }

    private void reset() {
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    dismissProgress("Not connected.");
                    return;
                }
                int ret = NativeApi.nativeReset(mApiNfcardOTP.getDeviceHandle());
                if (ret != 0) {
                    dismissProgress("reset Error. " + parseErrorCode(ret));
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditPin.setText("5555");
                    }
                });
                dismissProgress("reset success");
            }
        }).start();
    }


    private void importMnemonic() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress();
                        if (!isConnected) {
                            dismissProgress("Not connected.");
                            return;
                        }
                        int ret = NativeApi.nativeImportMnemonic(mApiNfcardOTP.getDeviceHandle(),
                                mPINStr, "aerobic frog famous clock evolve exercise charge capable know square session slush");
                        if (ret != 0) {
                            dismissProgress("importMnemonic Error. " + parseErrorCode(ret));
                            return;
                        }
                        dismissProgress("importMnemonic Success.");
                    }
                }).start();
            }
        });
    }

    private void exportMnemonic() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress();
                        if (!isConnected) {
                            dismissProgress("Not connected.");
                            return;
                        }
                        String mnemonic = NativeApi.nativeExportMnemonic(mApiNfcardOTP.getDeviceHandle(), mPINStr);
                        if (TextUtils.isEmpty(mnemonic)) {
                            int code = NativeApi.nativeGetErrorCode();
                            if (code == 0x9B01) {
                                dismissProgress("ExportMnemonic Error PIN码错误" + parseErrorCode(code));
                            } else if (code == 0x9B02) {
                                dismissProgress("ExportMnemonic Error 助记词不能导出" + parseErrorCode(code));
                            } else {
                                dismissProgress("ExportMnemonic Error " + parseErrorCode(code));
                            }
                        } else {
                            showMsg(mnemonic);
                        }
                    }
                }).start();
            }
        });
    }

    private void generateSeed() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                showProgress();
                if (!isConnected) {
                    dismissProgress("Not connected.");
                    return;
                }
                int ret = NativeApi.nativeGenerateSeed(mApiNfcardOTP.getDeviceHandle(), mPINStr);
                if (ret != 0) {
                    dismissProgress("GenerateSeed Error. " + parseErrorCode(ret));
                    return;
                }
                dismissProgress("GenerateSeed Success.");
            }
        });
    }

    private void changePin() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                final String oldPin = mPINStr;
                getPin("输入新PIN码", new PinCallback() {
                    @Override
                    public void onSuccess() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isConnected) {
                                    dismissProgress("Not connected.");
                                    return;
                                }
                                Log.d("JUB", oldPin + " " + mPINStr);
                                int ret = NativeApi.nativeChangePIN(mApiNfcardOTP.getDeviceHandle(), oldPin.getBytes(), mPINStr.getBytes());
                                if (ret != 0) {
                                    dismissProgress("ChangePIN Error. " + parseErrorCode(ret));
                                    return;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mEditPin.setText(mPINStr);
                                    }
                                });
                                dismissProgress("ChangePIN Success.");
                            }
                        }).start();
                    }
                });
            }
        });
    }

    private void getDeviceInfo() {
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    dismissProgress("Not connected.");
                    return;
                }
                String ret = NativeApi.nativeGetDeviceInfo(mApiNfcardOTP.getDeviceHandle());
                if (!TextUtils.isEmpty(ret)) {
                    showMsg(ret);
                    return;
                }
                int code = NativeApi.nativeGetErrorCode();
                dismissProgress("GetDeviceInfo error " + parseErrorCode(code));
            }
        }).start();
    }

    private void getDeviceCert() {
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    dismissProgress("Not connected.");
                    return;
                }
                String ret = NativeApi.nativeGetDeviceCert(mApiNfcardOTP.getDeviceHandle());
                if (!TextUtils.isEmpty(ret)) {
                    showMsg(ret);
                    return;
                }
                int code = NativeApi.nativeGetErrorCode();
                dismissProgress("GetDeviceCert error " + parseErrorCode(code));
            }
        }).start();
    }

    private void enumApplets() {
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    dismissProgress("Not connected.");
                    return;
                }
                String ret = NativeApi.nativeEnumApplets(mApiNfcardOTP.getDeviceHandle());
                if (!TextUtils.isEmpty(ret)) {
                    showMsg(ret);
                    return;
                }
                int code = NativeApi.nativeGetErrorCode();
                dismissProgress("EnumApplets error " + parseErrorCode(code));
            }
        }).start();
    }


    private void sendApdu() {
        final String apduStr = mEditApdu.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(apduStr)) {
            return;
        }
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    dismissProgress("Not connected.");
                    return;
                }
                final String result = NativeApi.nativeSendApdu(mApiNfcardOTP.getDeviceHandle(), apduStr);
                if (TextUtils.isEmpty(result)) {
                    dismissProgress("send apdu error");
                } else {
                    showMsg(result);
                }
            }
        }).start();
    }

    private void btcGetAddress() {
        showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    dismissProgress("Not connected.");
                    return;
                }
                int[] contextIDs = new int[1];
                int ret = NativeApi.nativeBTCCreateContext(contextIDs,
                        JSONParseUtils.getJsonStr(SDKActivity.this, "testBTC44.json"),
                        mApiNfcardOTP.getDeviceHandle());
                if (ret != 0) {
                    dismissProgress("BTCCreateContext Error. " + parseErrorCode(ret));
                    return;
                }
                final long contextID = contextIDs[0];
                Log.d("contextID:", " " + contextID);
                final String[] strings = NativeApi.nativeBTCGetAddress(contextID, JSONParseUtils.getJsonStr(SDKActivity.this, "path.json"));
                if (strings.length == 0) {
                    dismissProgress("nativeBTCGetAddress Error");
                } else {
                    showMsg(Arrays.toString(strings));
                }
            }
        }).start();
    }


    private void btcTrans() {
        showProgress();
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isConnected) {
                            dismissProgress("Not connected.");
                            return;
                        }
                        int[] contextIDs = new int[1];
                        int ret = NativeApi.nativeBTCCreateContext(contextIDs,
                                JSONParseUtils.getJsonStr(SDKActivity.this, "testBTC44.json"),
                                mApiNfcardOTP.getDeviceHandle());
                        if (ret != 0) {
                            dismissProgress("BTCCreateContext Error. " + parseErrorCode(ret));
                            return;
                        }
                        final long contextID = contextIDs[0];
                        Log.d("SDK_DEMO", "btcTrans contextID:" + contextID);

                        ret = NativeApi.nativeVerifyPIN(contextID, mPINStr.getBytes());
                        if (ret != 0) {
                            dismissProgress("VerifyPIN Error. " + parseErrorCode(ret));
                            return;
                        }
                        Log.d("SDK_DEMO", "VerifyPIN success");

                        final String raw = NativeApi.nativeBTCTransaction(contextID, JSONParseUtils.getJsonStr(SDKActivity.this, "testBTC44.json"));
                        if (!TextUtils.isEmpty(raw)) {
                            showMsg(raw);
                            Log.d("btcTrans raw:", raw);
                        } else {
                            int code = NativeApi.nativeGetErrorCode();
                            dismissProgress("BTCTransaction Error. " + parseErrorCode(code));
                        }

                    }
                }).start();
            }
        });

    }

    private long mContextID;

    private void btcVerifyPin() {
        showProgress();
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isConnected) {
                            dismissProgress("Not connected.");
                            return;
                        }
                        int[] contextIDs = new int[1];
                        int ret = NativeApi.nativeBTCCreateContext(contextIDs,
                                JSONParseUtils.getJsonStr(SDKActivity.this, "testBTC44.json"),
                                mApiNfcardOTP.getDeviceHandle());
                        if (ret != 0) {
                            dismissProgress("BTCCreateContext Error. " + parseErrorCode(ret));
                            return;
                        }
                        mContextID = contextIDs[0];
                        Log.d("SDK_DEMO", "btcTrans contextID:" + mContextID);

                        ret = NativeApi.nativeVerifyPIN(mContextID, mPINStr.getBytes());
                        if (ret != 0) {
                            dismissProgress("VerifyPIN Error. " + parseErrorCode(ret));
                            return;
                        }
                        dismissProgress("VerifyPIN success");
                    }
                }).start();
            }
        });

    }


    private void btcTransOnly() {
        showProgress();
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isConnected) {
                            dismissProgress("Not connected.");
                            return;
                        }
                        final String raw = NativeApi.nativeBTCTransaction(mContextID, JSONParseUtils.getJsonStr(SDKActivity.this, "testBTC44.json"));
                        if (!TextUtils.isEmpty(raw)) {
                            showMsg(raw);
                            Log.d("btcTrans raw:", raw);
                        } else {
                            int code = NativeApi.nativeGetErrorCode();
                            dismissProgress("BTCTransaction Error. " + parseErrorCode(code));
                        }

                    }
                }).start();
            }
        });

    }

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final int SEED_KEY_SIZE = 512;
    private static final int SEED_ITERATIONS = 2048;

    private byte[] getnerateSeed(String mnemonic, String passphrase) {
        if (isMnemonicEmpty(mnemonic)) {
            throw new IllegalArgumentException("Mnemonic is required to generate a seed");
        }
        passphrase = passphrase == null ? "" : passphrase;
        String salt = String.format("mnemonic%s", passphrase);
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA512Digest());
        gen.init(mnemonic.getBytes(UTF_8), salt.getBytes(UTF_8), SEED_ITERATIONS);
        CipherParameters parameters1 = gen.generateDerivedParameters(SEED_KEY_SIZE);
        KeyParameter parameters2 = (KeyParameter) parameters1;
        return parameters2.getKey();
    }

    private static boolean isMnemonicEmpty(String mnemonic) {
        return mnemonic == null || mnemonic.trim().isEmpty();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        int nfcState = mApiNfcardOTP.getNfcState();
        if (nfcState == -1) {
            Toast.makeText(this, "No NFC hardware found!", Toast.LENGTH_LONG).show();
        } else if (nfcState == 0) {
            Toast.makeText(this, "NFC hardware has been disabled, Please enable it first.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApiNfcardOTP.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mApiNfcardOTP.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiNfcardOTP.onDestroy();
    }

    public void showMsg(final String msg) {
        dismissProgress();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new TipDialog().init(SDKActivity.this)
                        .setTitle("Result")
                        .setMsg(msg)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    public void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    mDialog.setMessage("通讯中....");
                    mDialog.show();
                }
            }
        });
    }

    public void dismissProgress() {
        dismissProgress(null);
    }

    public void dismissProgress(final String toastStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(toastStr)) {
                    Toast.makeText(SDKActivity.this, toastStr, Toast.LENGTH_SHORT).show();
                }
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
    }

    private String parseErrorCode(int code) {
        Log.e("ErrorCode:", code + "");
        return "0x" + Integer.toHexString(code);
    }

}
