package com.ftsafe.nfcdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ftsafe.nfcdemo.listener.ApiAsyncListener;
import com.ftsafe.nfcdemo.nfc.ApiNfc;
import com.ftsafe.nfcdemo.utils.FileUtils;
import com.ftsafe.nfcdemo.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.ftsafe.nfcdemo.MainActivity.LOG_ERROR;
import static com.ftsafe.nfcdemo.MainActivity.LOG_RECV;
import static com.ftsafe.nfcdemo.MainActivity.LOG_SEND;
import static com.ftsafe.nfcdemo.utils.Utils.hexString2Bytes;

public class SimpleTestActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final static int REQUEST_PERMISSION = 0x1002;
    private ApiNfc mApiNfcardOTP;
    private SimpleDateFormat mFormat;

    private TextView mTxtLog;
    private ScrollView mScrollView;
    private File mCurrentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_test);

        mTxtLog = findViewById(R.id.tv_message);
        mScrollView = findViewById(R.id.scrollView);

        mFormat = new SimpleDateFormat("yyyy-MM-dd   hh:mm:ss");
        mApiNfcardOTP = ApiNfc.getInstance(this);

        mApiNfcardOTP.setCardTag(getIntent());

        if (!hasPermissions()) {
            requestPermissions("Permission request", REQUEST_PERMISSION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        FileUtils.init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int re = mApiNfcardOTP.setCardTag(intent);
        if (re == 0) {
            Toast.makeText(this, "NFC hardware touched.", Toast.LENGTH_SHORT).show();
        }
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

    public void onClick(View view) {
        if(apduing){
            return;
        }
        List<String> apdu = FileUtils.getApdu();
        if (apdu.size() == 0) {
            Toast.makeText(this, "No apdus.", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentFile = FileUtils.makeLogFile();
        sendApdu(apdu);
    }

    boolean apduing = false;

    private void sendApdu(List<String> preApdus) {
        final ArrayList<String> apdus = new ArrayList<>(preApdus);
        if (apdus.size() == 0) {
            apduing = false;
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
                if (result == null) {
                    sendToMain(Utils.getError(errorCode), LOG_ERROR);
                    apduing = false;
                    return;
                }
                sendToMain(result, LOG_RECV);
                if (result.endsWith("9000")) {
                    sendApdu(apdus);
                }
            }
        });

    }

    public boolean hasPermissions() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestPermissions(@NonNull String rationale,
                                   int requestCode, @Size(min = 1) @NonNull String... perms) {
        EasyPermissions.requestPermissions(this, rationale, requestCode, perms);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        FileUtils.init();
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


    private void sendToMain(final String s, final int logSend) {
        FileUtils.saveLog(logSend, mCurrentFile, s);
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
                    mTxtLog.append(Html.fromHtml("<p><font color=\"#55c0e4\">接收:<br>" + s + "</p>"));
                } else if (logSend == LOG_ERROR) {
                    Log.d("LOG_ERROR", s);
                    mTxtLog.append(Html.fromHtml("<p><font color=\"#f92743\">ERROR:<br>" + s + "</p>"));
                }
                scrollBottom(mScrollView, mTxtLog);
            }
        });
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
}
