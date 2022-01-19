package com.jyoryo.app.android.smsmover.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.jyoryo.app.android.smsmover.Constants;
import com.jyoryo.app.android.smsmover.service.SenderService;

/**
 * 开机广播
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == context || null == intent || TextUtils.isEmpty(intent.getAction())) {
            return ;
        }
        intent.setClass(context, SenderService.class);
        intent.putExtra(Constants.KEY_BROADCASTRECEIVERRESULT, getResultCode());
        context.startService(intent);
    }
}
