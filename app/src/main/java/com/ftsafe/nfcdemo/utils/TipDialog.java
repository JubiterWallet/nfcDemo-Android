package com.ftsafe.nfcdemo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.ftsafe.nfcdemo.R;


/**
 * @Date 2018/5/12  22:04
 * @Author ZJF
 * @Version 1.0
 */
public class TipDialog {

    private TextView mTitleTv, mMsgTv;
    private TextView mCancelTv, mOkTv;
    private AlertDialog mDialog;

    public TipDialog() {
    }

    public TipDialog init(Context context) {

        View view = LayoutInflater.from(context).inflate(R.layout.dialoh_tip, null);
        mDialog = new AlertDialog.Builder(context)
                .setCancelable(true)
                .setView(view).create();
        mDialog.setCanceledOnTouchOutside(false);
        mTitleTv = view.findViewById(R.id.dialog_tip_tv);
        mMsgTv = view.findViewById(R.id.dialog_msg);

        mCancelTv = view.findViewById(R.id.dialog_cancel);
        mOkTv = view.findViewById(R.id.dialog_ok);
        return this;
    }

    public TipDialog setTitle(String title) {
        mTitleTv.setText(title);
        mTitleTv.setVisibility(View.VISIBLE);
        return this;
    }

    public TipDialog setTitle(int id) {
        mTitleTv.setText(id);
        mTitleTv.setVisibility(View.VISIBLE);
        return this;
    }

    public TipDialog setMsg(CharSequence msg) {
        mMsgTv.setText(msg);
        mMsgTv.setVisibility(View.VISIBLE);
        return this;
    }

    public TipDialog setMsg(int id) {
        mMsgTv.setText(id);
        mMsgTv.setVisibility(View.VISIBLE);
        return this;
    }

    public TipDialog setPositiveButton(String str, final View.OnClickListener listener) {
        mOkTv.setVisibility(View.VISIBLE);
        mOkTv.setText(str);
        mOkTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        return this;
    }

    public TipDialog setPositiveButton(int id, final View.OnClickListener listener) {
        mOkTv.setVisibility(View.VISIBLE);
        mOkTv.setText(id);
        mOkTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        return this;
    }

    public TipDialog setNegativeButton(String str, final View.OnClickListener listener) {
        mCancelTv.setVisibility(View.VISIBLE);
        mCancelTv.setText(str);
        mCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        return this;
    }

    public TipDialog setNegativeButton(int id, final View.OnClickListener listener) {
        mCancelTv.setVisibility(View.VISIBLE);
        mCancelTv.setText(id);
        mCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        return this;
    }

    public TipDialog setCancelable(boolean cancelable) {
        mDialog.setCancelable(cancelable);
        return this;
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

}
