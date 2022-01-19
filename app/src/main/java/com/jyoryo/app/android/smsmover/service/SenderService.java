package com.jyoryo.app.android.smsmover.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.jyoryo.app.android.smsmover.BuildConfig;
import com.jyoryo.app.android.smsmover.Constants;
import com.jyoryo.app.android.smsmover.Constants.MessageType;
import com.jyoryo.app.android.smsmover.Constants.PhoneType;
import com.jyoryo.app.android.smsmover.R;
import com.jyoryo.app.android.smsmover.RdsOpenHelper;
import com.jyoryo.app.android.smsmover.SmsMoverApplication;
import com.jyoryo.app.android.smsmover.dto.PhoneBean;
import com.jyoryo.app.android.smsmover.dto.SmsBean;
import com.jyoryo.app.android.smsmover.dto.message.AbstractSendMessage;
import com.jyoryo.app.android.smsmover.dto.message.PhoneSendMessage;
import com.jyoryo.app.android.smsmover.dto.message.SimpleSendMessage;
import com.jyoryo.app.android.smsmover.dto.message.SmsSendMessage;
import com.jyoryo.app.android.smsmover.receiver.BatteryReceiver;
import com.jyoryo.app.android.smsmover.receiver.SmsReceiver;
import com.jyoryo.app.android.smsmover.util.AndroidUtil;
import com.jyoryo.app.android.smsmover.util.HttpUtil;
import com.jyoryo.app.android.smsmover.util.Logs;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送消息服务
 */
public class SenderService extends Service {
    private static final String TAG = "SenderService";

    /**
     * 获取服务器host的API接口地址
     */
    public static final String API_URL_HOST = "%s/api/common/host?name=jyoryo_home";
    /**
     * 服务器host内容
     */
    public static String SERVER_HOST;

    /**
     * Server酱API HOST
     */
    private static final String ApiHostServerChan = "https://sc.ftqq.com/%s.send";

    /**
     * 实际提供服务的URL。完整URL：{serverUrl}/api/lgntkn/msg   "%s/api/lgntkn/msg"
     */
    public static final String API_URL_SMS = "%s/api/lgntkn/sms";
    public static final String API_URL_PHONE_RECORD = "%s/api/lgntkn/phoneRecord";

    private static String mineMobile;
    private static String mApiHost;
    private static String mLoginToken;
    private static String mServerChanSckey;
    private static int mMaxRetry;
    private static long mRetryInterval;
    private static boolean mBatteryNotifyFlag;

    private static RdsOpenHelper mRdsHelper;
    private static SharedPreferences mPref;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.d(TAG, "==========>SenderService onCreate");
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mRdsHelper = RdsOpenHelper.getInstance();
        loadConfig();
    }

    public static void loadConfig() {
        if (null == mPref) {
            mPref = SmsMoverApplication.getContext().getSharedPreferences(Constants.FILE_NAME_SETTING, Context.MODE_PRIVATE);
        }
        if(TextUtils.isEmpty(mineMobile)) { mineMobile = mPref.getString(Constants.Config_MineMobile, null); }
        if(TextUtils.isEmpty(mApiHost)) { mApiHost = mPref.getString(Constants.Config_ApiHost, BuildConfig.ApiHost); }
        if(TextUtils.isEmpty(mLoginToken)) { mLoginToken = mPref.getString(Constants.Config_LoginToken, null); }
        if(TextUtils.isEmpty(mServerChanSckey)) { mServerChanSckey = mPref.getString(Constants.Config_ServerChanSckey, null); }
        if(0 == mMaxRetry) { mMaxRetry =  mPref.getInt(Constants.Config_MaxRetry, Constants.Default_MaxRetry); }
        if(0L == mRetryInterval) { mPref.getLong(Constants.Config_RetryInterval, Constants.Default_RetryInterval); }
        mBatteryNotifyFlag = mPref.getBoolean(Constants.Config_BatteryNotifyFlag, Constants.Default_BatteryNotifyFlag);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.d(TAG, "==========>SenderService onStartCommand");
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.arg2 = flags;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logs.d(TAG, "==========>SenderService onDestroy");
        super.onDestroy();
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 服务处理器
     */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int serviceId = msg.arg1, flags = msg.arg2;
            Intent intent = (Intent) msg.obj;
            if(null == intent) {
                return ;
            }
            final String action = intent.getAction();
            Logs.d(TAG, "==============SenderService ServiceHandler Hander Action：" + action);
            Logs.d(TAG, "==============SenderService ServiceHandler handleMessage (args1:" + msg.arg1 + "_args2:" + msg.arg2);
            if(!TextUtils.isEmpty(action)) {
                int error = intent.getIntExtra("errorCode", 0);
                // 开机完成
                if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                    handleBootCompleted();
                }
                // 收到短信
                else if("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
                    try {
                        handlerSmsReceived(intent, error);
                    } catch(Exception e) {
                        Logs.e(TAG, e.getMessage());
                    } finally {
                        SmsReceiver.finishStartingService(SenderService.this, serviceId);
                    }
                }
                // 来去电
                else if(Intent.ACTION_NEW_OUTGOING_CALL.equals(action) || "android.intent.action.PHONE_STATE".equals(action)) {
                    // 去电
                    if(Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
                        handerNewOutGoingCall(intent, error);
                    }
                    // 来电
                    else {  // if("android.intent.action.PHONE_STATE".equals(action))
                        handerPhoneState(intent, error);
                    }
                }
                // 电池状态
                else if(Intent.ACTION_BATTERY_LOW.equals(action) || Intent.ACTION_BATTERY_OKAY.equals(action) || Intent.ACTION_POWER_CONNECTED.equals(action) || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                    try {
                        // 不通知
                        if(!mBatteryNotifyFlag) {
                            return ;
                        }
                        String deviceModel = Build.MODEL;
                        Context context = SmsMoverApplication.getContext();
                        String sender = context.getString(R.string.senderSystem), batteryStatus = context.getString(R.string.batteryStatus);
                        String title = String.format("%s %s", deviceModel, batteryStatus);
                        // 电量低
                        if(Intent.ACTION_BATTERY_LOW.equals(action)) {
                            send(new SimpleSendMessage(sender, title, context.getString(R.string.batteryLow), new Date().getTime()), true);
                            return ;
                        }
                        long dateline = new Date().getTime();
                        long lastNotifyTimestamp = mPref.getLong(Constants.Config_LastBatteryNotifyTimestamp, 0L);
                        long offset = dateline - lastNotifyTimestamp;
                        // 至少间隔1小时
                        if (3600000L > offset) {
                            return;
                        }
                        String content = null;
                        // 电量充满
                        if(Intent.ACTION_BATTERY_OKAY.equals(action)) { content = context.getString(R.string.batteryOkay); }
                        // 连接电源
                        else if(Intent.ACTION_POWER_CONNECTED.equals(action)) { content = context.getString(R.string.powerConnected); }
                        // 断开电源
                        else if(Intent.ACTION_POWER_DISCONNECTED.equals(action)) { content = context.getString(R.string.powerDisConnected); }
                        if(!TextUtils.isEmpty(content)) {
                            send(new SimpleSendMessage(sender, title, content, new Date().getTime()), true);
                        }
                        // 保存最新发送电池通知时间
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putLong(Constants.Config_LastBatteryNotifyTimestamp, dateline);
                        editor.apply();
                    } catch(Exception e) {
                        Logs.e(TAG, e.getMessage());
                    } finally {
                        BatteryReceiver.finishStartingService(SenderService.this, serviceId);
                    }
                }
            }
            else {
                AbstractSendMessage sendMessage = (AbstractSendMessage) intent.getSerializableExtra(Constants.KEY_SENDMESSAGE);
                Logs.d(TAG, String.format("Action was empty! SendMessage:%s", (null == sendMessage) ? "null" : sendMessage.getMessageType() + "___" + sendMessage.toString()));
                if(null != sendMessage) {
                    send(sendMessage, true);
                }
            }
        }
    }   //end class ServiceHandler

    // 开机后服务处理
    private void handleBootCompleted() {
        Logs.d(TAG, "==============SendService Called By BootCompleted");
    }

    // 接收短信处理
    private void handlerSmsReceived(Intent intent, int error) {
        Logs.d(TAG, "==============SendService Called By SMS_RECEIVED");
        Bundle bundle = intent.getExtras();
        if (null == bundle) {
            return;
        }
        Object[] pdus = (Object[]) bundle.get("pdus");
        int pduCount = pdus.length;
        String from = null;
        long sendTimestamp = 0L;
        StringBuilder contentBuilder = new StringBuilder();
        //解析
        for (int i = 0; i < pduCount; i++) {
            SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[i]);
            contentBuilder.append(messages.getMessageBody());
            long smsTimestamp = messages.getTimestampMillis();
            if(0 == i) {
                from = messages.getOriginatingAddress();
            }
            if(0L == sendTimestamp || sendTimestamp > smsTimestamp) {
                sendTimestamp = smsTimestamp;
            }
        }
        String content = contentBuilder.toString();
        // 保存数据库
        long smsId = mRdsHelper.saveSms(from, mineMobile, content, sendTimestamp);
        send(new SmsSendMessage(smsId, from, mineMobile, content, sendTimestamp, false), true);
    }

    // 去电处理
    private void handerNewOutGoingCall(Intent intent, int error) {
        Logs.d(TAG, "==============SendService Called By NEW_OUTGOING_CALL");
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Logs.d(TAG, "handerNewOutGoingCall RelatedPhone:" + phoneNumber);
        final long dateline = new Date().getTime();
        // 保存数据库
        long pId = mRdsHelper.savePhone(PhoneType.CALL_OUT, mineMobile, phoneNumber, dateline);
        // 发送服务器
        send(new PhoneSendMessage(pId, PhoneType.CALL_OUT, mineMobile, phoneNumber, dateline, false), true);
    }

    // 来电处理
    private void handerPhoneState(Intent intent, int error) {
        Logs.d(TAG, "==============SendService Called By PhoneState");
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE),
                incomingPhoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (TextUtils.isEmpty(incomingPhoneNumber)) {
            return ;
        }
        if(TextUtils.equals(TelephonyManager.EXTRA_STATE_RINGING, state)) {
            Logs.d(TAG, "handerPhoneState IncomintPhone:" + incomingPhoneNumber);
            final long dateline = new Date().getTime();
            // 保存数据库
            long prId = mRdsHelper.savePhone(PhoneType.CALL_IN, mineMobile, incomingPhoneNumber, dateline);
            // 发送服务器
            send(new PhoneSendMessage(prId, PhoneType.CALL_IN, mineMobile, incomingPhoneNumber, dateline, false), true);
        }
    }

    /**
     * 发送消息
     * @param sendMessage
     * @param initCall   是否初始调用(解决处理未同步数据，会迭代调用该方法)
     * @return
     */
    private void send(AbstractSendMessage sendMessage, boolean initCall) {
        doSendMessage(sendMessage);
        // 处理未同步信息
        if(initCall) {
            try {
                this.processUnsyncData();
            } catch (Exception e) {
                Logs.e(TAG, "send call processUnsyncData: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 处理未同步数据
     */
    public void processUnsyncData() {
        final long lastProcessUnSyncDataTimestamp = mPref.getLong(Constants.Config_LastProcessUnsyncDataTimestamp, 0L),
                currentTimestamp = new Date().getTime();
        long offset = currentTimestamp - lastProcessUnSyncDataTimestamp;
        // 至少间隔24小时
        if (24 * 60 * 60 * 1000 > offset) {
            return;
        }
        // 短信
        List<SmsBean> smsItems = mRdsHelper.querySmsUnsync();
        if(null != smsItems && smsItems.size() > 0) {
            for(SmsBean smsItem : smsItems) {
                send(new SmsSendMessage(smsItem), false);
            }
        }
        // 通话记录
        List<PhoneBean> phoneItems = mRdsHelper.queryPhoneUnsync();
        if(null != phoneItems && phoneItems.size() > 0) {
            for(PhoneBean phoneItem : phoneItems) {
                send(new PhoneSendMessage(phoneItem), false);
            }
        }
        // 设置最新处理未同步数据的时间
        SharedPreferences.Editor editor = mPref.edit();
        editor.putLong(Constants.Config_LastProcessUnsyncDataTimestamp, currentTimestamp);
        editor.apply();
    }

    private boolean doSendMessage(AbstractSendMessage sendMessage) {
        Logs.d(TAG, String.format("doSendMessage MessageType：%s", sendMessage.getMessageType()));
        boolean isSuccess = false;
        final MessageType messageType = sendMessage.getMessageType();
        switch(messageType) {
            case SMS:
                SmsSendMessage smsSendMessage = (SmsSendMessage)sendMessage;
                String smsSender = smsSendMessage.getSender(), smsReceiver = smsSendMessage.getReceiver(), smsContent = smsSendMessage.getContent();
                long smsId = smsSendMessage.getId(), smsDateline = smsSendMessage.getDateline();
                // 优先发送至服务器
                isSuccess = sendSmsToServer(smsSender, smsReceiver, smsContent, smsDateline);
                if(isSuccess && smsId > 0L) {
                    mRdsHelper.updateSmsSynced(smsId);
                }
                // 发送服务器失败，则直接发送短信内容
                if (!smsSendMessage.isSentFlag() && !isSuccess) {
                    isSuccess = sendToServerChan(String.format("短信通知 发送人：%s", smsSender), String.format("时间:%s %s", AndroidUtil.longToDatetimeString(smsDateline), smsContent));
                    if (isSuccess) {
                        mRdsHelper.updateSmsSent(smsId);
                    }
                }
                break;
            case PHONE:
                PhoneSendMessage phoneSendMessage = (PhoneSendMessage)sendMessage;
                PhoneType phoneType = phoneSendMessage.getType();
                long prId = phoneSendMessage.getId(), prDateline = phoneSendMessage.getDateline();
                int prType = phoneType.ordinal();
                String prPhone = phoneSendMessage.getPhone(), prRelatedPhone = phoneSendMessage.getRelatedPhone();
                // 优先发送至服务器
                isSuccess = sendPhoneRecordToServer(prType, prPhone, prRelatedPhone, prDateline);
                if(isSuccess && prId > 0L) {
                    mRdsHelper.updatePhoneSynced(prId);
                }
                // 发送服务器失败，则直接发送短信内容
                if (!phoneSendMessage.isSentFlag() && !isSuccess) {
                    isSuccess = sendToServerChan(
                            String.format("%s通知", (PhoneType.CALL_IN == phoneType) ? "来电" : "去电"),
                            String.format("号码：%s 时间:%s", prRelatedPhone, AndroidUtil.longToDatetimeString(prDateline)));
                    if (isSuccess) {
                        mRdsHelper.updatePhoneSent(prId);
                    }
                }
                break ;
            case SIMPLE:
                SimpleSendMessage simpleSendMessage = (SimpleSendMessage)sendMessage;
                isSuccess = sendToServerChan(simpleSendMessage.getTitle(), String.format("%s 发送人:%s 时间:%s。", simpleSendMessage.getContent(), simpleSendMessage.getSender(), AndroidUtil.longToDatetimeString(simpleSendMessage.getDateline())));
                break ;
            default: break;
        }
        return false;
    }

    /**
     * 发送消息至ServerChan
     * @param title
     * @param content
     * @return
     */
    private boolean sendToServerChan(String title, String content) {
        if (TextUtils.isEmpty(mServerChanSckey)) {
            Logs.w(TAG, "sendToServerChan: SCKey is empty!");
            return false;
        }
        Logs.d(TAG, String.format("sendToServerChan Title:%s___Content:%s", title, content));
        if(TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            return true;
        }
        int count = mMaxRetry;
        final String requestUrl = String.format(ApiHostServerChan, mServerChanSckey);
        Map<String, String> params = new HashMap<String, String>();
        params.put("text", title);
        params.put("desp", content);
        while (count-- > 0) {
            try {
                String responseString = HttpUtil.post(requestUrl, params);
                JSONObject responJson = new JSONObject(responseString);
                if(0 == responJson.getInt("errno")) {
                    return true;
                }
            } catch (Exception e) {
                Logs.w(TAG, "sendToServerChan:" + e.getMessage(), e);
            }
            // 重试等待
            try {
                Thread.sleep(mRetryInterval);
            } catch (InterruptedException e) {
                Logs.e(TAG, "sendToServerChan: " + e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 将短信数据发送至自建服务
     * @return
     */
    private boolean sendSmsToServer(String sender, String receiver, String content, long dateline) {
        if(TextUtils.isEmpty(mLoginToken)) {
            Logs.w(TAG, "sendToServer: LoginToken is empty!");
            return false;
        }
        if(TextUtils.isEmpty(SERVER_HOST)) {
            boolean fetchResult = fetchServerHost();
            if(!fetchResult) {
                return false;
            }
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("login_token", mLoginToken);
        params.put("sender", sender);
        params.put("receiver", receiver);
        params.put("content", content);
        params.put("dateline", String.valueOf(dateline));

        boolean doSendResult = doSendToServer(requestUrl(API_URL_SMS), params);
        if(doSendResult){
            return true;
        }
        // 如果获取失败，重新获取serverUrl
        boolean fetchResult = fetchServerHost();
        if(fetchResult) {
            return doSendToServer(requestUrl(API_URL_SMS), params);
        }
        return false;
    }

    /**
     * 将通话数据发送至自建服务
     * @return
     */
    private boolean sendPhoneRecordToServer(int type, String phone, String relatedPhone, long dateline) {
        if(TextUtils.isEmpty(mLoginToken)) {
            Logs.w(TAG, "sendToServer: LoginToken is empty!");
            return false;
        }
        if(TextUtils.isEmpty(SERVER_HOST)) {
            boolean fetchResult = fetchServerHost();
            if(!fetchResult) {
                return false;
            }
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("login_token", mLoginToken);
        params.put("type", String.valueOf(type));
        params.put("phone", phone);
        params.put("relatedPhone", relatedPhone);
        params.put("dateline", String.valueOf(dateline));

        boolean doSendResult = doSendToServer(requestUrl(API_URL_PHONE_RECORD), params);
        if(doSendResult){
            return true;
        }
        // 如果获取失败，重新获取serverUrl
        boolean fetchResult = fetchServerHost();
        if(fetchResult) {
            return doSendToServer(requestUrl(API_URL_PHONE_RECORD), params);
        }
        return false;
    }

    /**
     * 获取api请求的url
     * @param apiTmpt
     * @return
     */
    private static String requestUrl(String apiTmpt) {
        if(TextUtils.isEmpty(apiTmpt)) {
            return null;
        }
        return String.format(apiTmpt, SERVER_HOST);
    }

    /**
     * 获取服务Host
     * @return
     */
    private boolean fetchServerHost() {
        final String requestUrl = String.format(API_URL_HOST, mApiHost);
        int count = mMaxRetry;
        while (count-- > 0) {
            try {
                String responseString = HttpUtil.get(requestUrl);
                Logs.d(TAG, responseString);
                JSONObject responseJson = new JSONObject(responseString);
                String data;
                if ((responseJson.getBoolean("success") || 0 == responseJson.getInt("code")) && !TextUtils.isEmpty(data = responseJson.getString("data"))) {
                    SERVER_HOST = data;
                    return true;
                }
            } catch (Exception e) {
                Logs.w(TAG, "fetchServerHost: " + e.getMessage(), e);
            }
            // 重试等待
            try {
                Thread.sleep(mRetryInterval);
            } catch (InterruptedException e) {
                Logs.e(TAG, "fetchServerHost: " + e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 发送数据至服务器
     * @param requestUrl
     * @param params
     * @return
     */
    private boolean doSendToServer(String requestUrl, Map<String, String> params) {
        int count = mMaxRetry;
        while (count-- > 0) {
            try {
                String responseString = HttpUtil.post(requestUrl, params);
                JSONObject responJson = new JSONObject(responseString);
                if(responJson.getBoolean("success") || 0 == responJson.getInt("code")) {
                    return true;
                }
            } catch (Exception e) {
                Logs.w(TAG, "doSendToServer: " + e.getMessage(), e);
            }
            try {
                Thread.sleep(mRetryInterval);
            } catch (InterruptedException e) {
                Logs.e(TAG, "doSendToServer: " + e.getMessage(), e);
            }
        }
        return false;
    }
}
