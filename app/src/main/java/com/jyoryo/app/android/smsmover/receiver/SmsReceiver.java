package com.jyoryo.app.android.smsmover.receiver;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Telephony;
import android.text.TextUtils;

import com.jyoryo.app.android.smsmover.Constants;
import com.jyoryo.app.android.smsmover.service.SenderService;

/**
 * 短信广播接收器
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    static final Object mStartingServiceSync = new Object();
    private static SmsReceiver sInstance;
    static PowerManager.WakeLock mWakeLock;

    public static SmsReceiver getInstance() {
        if (null == sInstance) {
            sInstance = new SmsReceiver();
        }
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(null == context || null == intent || !TextUtils.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            return ;
        }
        intent.setClass(context, SenderService.class);
        intent.putExtra(Constants.KEY_BROADCASTRECEIVERRESULT, getResultCode());
        beginStartingService(context, intent);
    }

    @SuppressLint("InvalidWakeLockTag")
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (null == mWakeLock) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                mWakeLock.setReferenceCounted(false);
            }
            mWakeLock.acquire();
            context.startService(intent);
        }
    }

    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (null != mWakeLock) {
                mWakeLock.release();
            }
        }
    }
}
