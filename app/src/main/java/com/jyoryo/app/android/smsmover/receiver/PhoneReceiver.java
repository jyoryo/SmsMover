package com.jyoryo.app.android.smsmover.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jyoryo.app.android.smsmover.Constants;
import com.jyoryo.app.android.smsmover.service.SenderService;

/**
 * 来去电广播
 */
public class PhoneReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == intent || null == context) {
            return ;
        }
        intent.setClass(context, SenderService.class);
        intent.putExtra(Constants.KEY_BROADCASTRECEIVERRESULT, getResultCode());
        context.startService(intent);
    }
}