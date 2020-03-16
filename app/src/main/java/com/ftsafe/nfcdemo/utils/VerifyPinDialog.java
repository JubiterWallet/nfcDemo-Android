package com.ftsafe.nfcdemo.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ftsafe.nfcdemo.R;

/**
 * Copyright © FEITIAN Technologies Co., Ltd. All Rights Reserved.
 *
 * @Date 2018/5/18  13:40
 * @Author ZJF
 * @Version 1.0
 */
public class VerifyPinDialog {

    private AlertDialog mDialog;
    private EditText mEditText;


    public VerifyPinDialog(Context context, String title, final callback listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_verify_pin, null);

        mEditText = view.findViewById(R.id.verify_et);
        mDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .create();
        ((Button) view.findViewById(R.id.btn_sure)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                listener.onClickListener(getPin());
            }
        });
    }

    public interface callback {
        void onClickListener(String pin);
    }

    public String getPin() {
        return mEditText.getText().toString().trim();
    }

    public void show() {
        mDialog.show();
    }
}
