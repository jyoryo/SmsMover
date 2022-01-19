package com.jyoryo.app.android.smsmover.receiver;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.jyoryo.app.android.smsmover.Constants;
import com.jyoryo.app.android.smsmover.service.SenderService;
import com.jyoryo.app.android.smsmover.util.Logs;

/**
 * 电池电量广播接收器
 */
public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";

    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mWakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logs.d(TAG, "BatteryReceiver Action:" + intent.getAction());
        if (null == context || null == intent) {
            return ;
        }
        intent.setClass(context, SenderService.class);
        intent.putExtra(Constants.KEY_BROADCASTRECEIVERRESULT, getResultCode());
        context.startService(intent);
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
