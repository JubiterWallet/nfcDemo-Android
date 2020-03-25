package com.ftsafe.nfcdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ftsafe.nfcdemo.listener.ApiAsyncListener;
import com.ftsafe.nfcdemo.nfc.ApiNfc;
import com.ftsafe.nfcdemo.utils.CommList;
import com.ftsafe.nfcdemo.utils.Utils;
import com.ftsafe.nfcdemo.utils.VerifyPinDialog;
import com.uuzuche.libzxing.activity.CodeUtils;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.ftsafe.nfcdemo.utils.Utils.hexString2Bytes;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private ApiNfc mApiNfcardOTP;
    private SimpleDateFormat mFormat;

    private EditText mEditApdu, mEditPin, mEditGetDataLen, mEditGetDataTimes, mEditGetDataDalay;
    private TextView mTxtLog, mTxtGetDataLen;
    private ScrollView mScrollView;

    private final static int REQUEST_PERMISSION = 0x1001;

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final int SEED_KEY_SIZE = 512;
    private static final int SEED_ITERATIONS = 2048;
    private boolean mGetMnemonic;
    private String mMnemonic;
    private String mApduPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtLog = findViewById(R.id.tv_message);
        mScrollView = findViewById(R.id.scrollView);
        mEditApdu = findViewById(R.id.edit_apdu);
        mEditPin = findViewById(R.id.edit_pin);
        mTxtGetDataLen = findViewById(R.id.txt_get_data_len);
        mEditGetDataLen = findViewById(R.id.edit_get_data_len);
        mEditGetDataTimes = findViewById(R.id.edit_get_data_time);
        mEditGetDataDalay = findViewById(R.id.edit_get_data_delay);


        mFormat = new SimpleDateFormat("yyyy-MM-dd   hh:mm:ss");
        mApiNfcardOTP = ApiNfc.getInstance(this);

        mApiNfcardOTP.setCardTag(getIntent());

        if (!hasPermissions()) {
            requestPermissions("Permission request", REQUEST_PERMISSION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE);
            return;
        }
    }

    public boolean hasPermissions() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void requestPermissions(@NonNull String rationale,
                                   int requestCode, @Size(min = 1) @NonNull String... perms) {
        EasyPermissions.requestPermissions(this, rationale, requestCode, perms);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int re = mApiNfcardOTP.setCardTag(intent);
        if (re == 0) {
            Toast.makeText(this, "NFC hardware touched.", Toast.LENGTH_SHORT).show();
            if (mGetMnemonic) {
                importMnemonic(mMnemonic);
                mGetMnemonic = false;
            }
        }
    }


    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_send_apdu:
                String apduStr = mEditApdu.getText().toString().trim().replace(" ", "");
                if (TextUtils.isEmpty(apduStr)) {
                    return;
                }
                sendApdu(apduStr);
                break;
            case R.id.btn_is_bootloader:
                sendApdu(CommList.BOOTLOADER);
                break;
            case R.id.btn_rest:
                sendApdu(CommList.RESET_WALLET);
                mEditPin.setText("5555");
                break;
            case R.id.btn_import_mnemonic:
                openCamera();
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
            case R.id.btn_get_app_list:
                sendApdu(CommList.GET_APP_LIST);
                break;
            case R.id.btn_get_available_memory:
                sendApdu(CommList.GET_AVA_MEMORY);
                break;
            case R.id.btn_verify_pin:
                getPin(new PinCallback() {
                    @Override
                    public void onSuccess() {
                        sendApdu(CommList.VERIFY_PIN + mApduPin);
                    }
                });
                break;
            case R.id.btn_select_nfc:
                sendApdu(CommList.SELECT_NFC);
                break;
            case R.id.btn_get_data:
                getData();
                break;
            case R.id.btn_btc_get_xpub:
                btcGetXpub(false);
                break;
            case R.id.btn_btc_trans:
                btcTrans(false);
                break;
            case R.id.btn_btc_get_xpub_new:
                btcGetXpub(true);
                break;
            case R.id.btn_btc_trans_new:
                btcTrans(true);
                break;
            case R.id.btn_eth_get_xpub:
                ethGetXpub(false);
                break;
            case R.id.btn_eth_trans:
                ethTrans(false);
                break;
            case R.id.btn_eth_get_xpub_new:
                ethGetXpub(true);
                break;
            case R.id.btn_eth_trans_new:
                ethTrans(true);
                break;
            default:
                break;
        }
    }

    private void getData() {
        String lenStr = mEditGetDataLen.getText().toString().trim().replace(" ", "");
        int lenInt = Integer.parseInt(lenStr);
        if (lenInt < 1 || lenInt > 255) {
            Toast.makeText(MainActivity.this, "Error length.", Toast.LENGTH_SHORT).show();
            return;
        }
        String binaryLen = Integer.toBinaryString(lenInt);
        mTxtGetDataLen.setText("Data length in binary:" + binaryLen);
        StringBuilder lenApdu = new StringBuilder(Integer.toHexString(lenInt));
        while (lenApdu.length() < 4) {
            lenApdu.insert(0, "0");
        }
        lenApdu.insert(0, CommList.GET_DATA_LEN_TAG + getApduHexLen(lenApdu.toString()));

        String delayStr = mEditGetDataDalay.getText().toString().trim().replace(" ", "");
        int delayInt = Integer.parseInt(delayStr);
        if (delayInt < 0) {
            Toast.makeText(MainActivity.this, "Error delay time.", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder delayApdu = new StringBuilder(Integer.toHexString(delayInt));
        while (delayApdu.length() < 4) {
            delayApdu.insert(0, "0");
        }
        delayApdu.insert(0, CommList.GET_DATA_TIME_TAG + getApduHexLen(delayApdu.toString()));

        String apdu = lenApdu + delayApdu.toString();
        apdu = CommList.GET_DATA + getApduHexLen(apdu) + apdu;

        int times = getLoopTimes();
        if (times < 1) {
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            list.add(apdu);
        }
        sendApdu(list);
    }

    private int getLoopTimes() {
        String timesStr = mEditGetDataTimes.getText().toString().trim().replace(" ", "");
        int times = Integer.parseInt(timesStr);
        if (times < 1) {
            Toast.makeText(MainActivity.this, "Error cycle times.", Toast.LENGTH_SHORT).show();
        }
        return times;
    }

    private void generateSeed() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                String apdu = CommList.GENERATE_SEED_DATA_TAG + getApduHexLen(mApduPin) + mApduPin;
                apdu = CommList.GENERATE_SEED + getApduHexLen(apdu) + apdu;
                apdu = CommList.APDU_HEADER + getApduHexLen(apdu) + apdu;
                sendApdu(apdu);
            }
        });
    }

    private void exportMnemonic() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                String apdu = CommList.EXPORT_MNEMONIC_DATA_TAG + getApduHexLen(mApduPin) + mApduPin;
                apdu = CommList.EXPORT_MNEMONIC + getApduHexLen(apdu) + apdu;
                apdu = CommList.APDU_HEADER + getApduHexLen(apdu) + apdu;
                sendApdu(apdu);
            }
        });
    }

    interface PinCallback {
        void onSuccess();
    }

    private void getPin(final PinCallback callback) {
        String pin = mEditPin.getText().toString().trim().replace(" ", "");
        int length = pin.length();
        if (length >= 4 && length <= 8) {
            String hexLen = Utils.byte2HexStr(Byte.parseByte(length + ""));
            String hexPin = Utils.byteArr2HexStr(pin.getBytes());
            mApduPin = hexLen + hexPin;
            callback.onSuccess();
        } else {
            Toast.makeText(MainActivity.this, "Error length", Toast.LENGTH_SHORT).show();
        }
    }

    private void getPin(String title, final PinCallback callback) {
        new VerifyPinDialog(this, title, new VerifyPinDialog.callback() {
            @Override
            public void onClickListener(String pin) {
                int length = pin.length();
                if (length >= 4 && length <= 8) {
                    String hexLen = Utils.byte2HexStr(Byte.parseByte(length + ""));
                    String hexPin = Utils.byteArr2HexStr(pin.getBytes());
                    mApduPin = hexLen + hexPin;
                    callback.onSuccess();
                } else {
                    Toast.makeText(MainActivity.this, "Error length", Toast.LENGTH_SHORT).show();
                }
            }
        }).show();
    }


    private void btcGetXpub(boolean useNew) {
        int times = getLoopTimes();
        if (times < 1) {
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            if (useNew) {
                list.addAll(CommList.BTC_GET_XPUB_KEYS_NEW);
            } else {
                list.addAll(CommList.BTC_GET_XPUB_KEYS);
            }
        }
        sendApdu(list);
    }


    private void btcTrans(final boolean useNew) {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                ArrayList<String> list = new ArrayList<>();
                list.add(CommList.VERIFY_PIN + mApduPin);
                int times = getLoopTimes();
                if (times < 1) {
                    return;
                }
                if (useNew) {
                    for (int i = 0; i < times; i++) {
                        list.addAll(CommList.BTC_SIGN_NEW);
                    }
                } else {//BTC 非优化签名 不循环
                    list.addAll(CommList.BTC_SIGN);
                }
                sendApdu(list);
            }
        });
    }

    private void ethGetXpub(boolean useNew) {
        int times = getLoopTimes();
        if (times < 1) {
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            if (useNew) {
                list.addAll(CommList.ETH_GET_XPUB_KEYS_NEW);
            } else {
                list.addAll(CommList.ETH_GET_XPUB_KEYS);
            }
        }
        sendApdu(list);
    }

    private void ethTrans(final boolean useNew) {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                ArrayList<String> list = new ArrayList<>();
                list.add(CommList.VERIFY_PIN + mApduPin);
                int times = getLoopTimes();
                if (times < 1) {
                    return;
                }
                for (int i = 0; i < times; i++) {
                    if (useNew) {
                        list.addAll(CommList.ETH_SIGN_NEW);
                    } else {
                        list.addAll(CommList.ETH_SIGN);
                    }
                }
                sendApdu(list);
            }
        });
    }

    private void changePin() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                final String oldPin = mApduPin;
                getPin("输入新PIN码", new PinCallback() {
                    @Override
                    public void onSuccess() {
                        String apdu = CommList.CHANGE_PIN + oldPin + mApduPin;
                        sendApdu(apdu);
                    }
                });
            }
        });
    }

    private String getApduHexLen(String apdu) {
        String result = Utils.byte2HexStr(Byte.parseByte(apdu.length() / 2 + ""));
        if (result.length() % 2 != 0) {
            result = "0" + result;
        }
        return result;
    }

    private void importMnemonic(String mnemonic) {
        byte[] seed = getnerateSeed(mnemonic, null);
        String seedStr = Utils.byteArr2HexStr(seed);
        String apdu = mApduPin + CommList.IMPORT_MNEMONIC_DATA_ENTROPY + "40" + seedStr;
        apdu = CommList.IMPORT_MNEMONIC_DATA_TAG + getApduHexLen(apdu) + apdu;
        apdu = CommList.IMPORT_MNEMONIC + getApduHexLen(apdu) + apdu;
        apdu = CommList.APDU_HEADER + getApduHexLen(apdu) + apdu;
        sendApdu(apdu);
    }

    public static final int REQUEST_CODE = 0x1001;

    private void openCamera() {
        getPin(new PinCallback() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(MainActivity.this, QRScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                mMnemonic = bundle.getString(CodeUtils.RESULT_STRING);

            }
            mApiNfcardOTP.checkConnect(new ApiAsyncListener<Void>() {
                @Override
                public void onUiChange() {

                }

                @Override
                public void onResult(int errorCode, Void result) {
                    if (errorCode == 0) {
                        importMnemonic(mMnemonic);
                    } else {
                        mGetMnemonic = true;
                        Toast.makeText(MainActivity.this, "请贴合NFC设备", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    boolean apduing = false;


    private void sendApdu(List<String> preApdus) {
        final ArrayList<String> apdus = new ArrayList<>(preApdus);
        if (apdus.size() == 0 || apduing) {
            Toast.makeText(this, "Done.", Toast.LENGTH_SHORT).show();
            return;
        }
        apduing = true;
        final String apdu = apdus.remove(0);
        sendToMain(apdu, LOG_SEND);
        byte[] apduBytes = hexString2Bytes(apdu);
        mApiNfcardOTP.transInstructions(apduBytes, new ApiAsyncListener<String>() {
            @Override
            public void onUiChange() {

            }

            @Override
            public void onResult(int errorCode, String result) {
                apduing = false;
                if (result == null) {
                    sendToMain(Utils.getError(errorCode), LOG_ERROR);
                    return;
                }
                sendToMain(result, LOG_RECV);
                if (result.endsWith("9000")) {
                    sendApdu(apdus);
                }
            }
        });

    }

    private void sendApdu(String apdu) {
        ArrayList<String> list = new ArrayList<>();
        list.add(apdu);
        sendApdu(list);
    }

    public static final int LOG_SEND = 0;
    public static final int LOG_RECV = 1;
    public static final int LOG_ERROR = 2;

    private void sendToMain(final String s, final int logSend, final long totalTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                mTxtLog.append("Time:\t" + mFormat.format(new Date(currentTimeMillis)) + "\n");
                if (logSend == LOG_SEND) {
                    Log.d("LOG_SEND", s);
                    mTxtLog.append(Html.fromHtml("<p><font color=\"#85d46f\">发送:<br>" + s + "</p>"));
                } else if (logSend == LOG_RECV) {
                    Log.d("LOG_RECV", s);
                    if (totalTime != -1) {
                        mTxtLog.append(Html.fromHtml("<p><font color=\"#55c0e4\">接收:<br>" + s + "<br>" + totalTime + "ms</p>"));
                    } else {
                        mTxtLog.append(Html.fromHtml("<p><font color=\"#55c0e4\">接收:<br>" + s + "</p>"));
                    }
                } else if (logSend == LOG_ERROR) {
                    Log.d("LOG_ERROR", s);
                    mTxtLog.append(Html.fromHtml("<p><font color=\"#f92743\">ERROR:<br>" + s + "</p>"));
                }
                scrollBottom(mScrollView, mTxtLog);
            }
        });
    }

    private void sendToMain(final String s, final int logSend) {
        sendToMain(s, logSend, -1);
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

    public void scrollBottom(final ScrollView scroll, final View inner) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                // 内层高度超过外层
                int offset = inner.getMeasuredHeight()
                        - scroll.getMeasuredHeight();
                if (offset < 0) {
                    offset = 0;
                }
                scroll.scrollTo(0, offset);
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

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
}
