package com.jyoryo.app.android.smsmover;

public interface Constants {
    /**
     * 配置文件名称
     */
    String FILE_NAME_SETTING = "setting";

    /**
     * 本地短信保存文件名
     */
    String FILE_NAME_SMS = "sms.json";

    /**
     * 本机号码
     */
    String Config_MineMobile = "mineMobile";

    /**
     * 服务Host
     */
    String Config_ApiHost = "apiHost";

    /**
     * 登录认证Token
     */
    String Config_LoginToken = "loginToken";

    /**
     * Server酱认证Key
     */
    String Config_ServerChanSckey = "serverChanSckey";

    String Config_BatteryNotifyFlag = "batteryNotifyFlag";

    /**
     * 重试次数
     */
    String Config_MaxRetry = "maxRetry";

    /**
     * 重试间隔时间
     */
    String Config_RetryInterval = "retryInterval";

    /**
     * 本地存储数量
     */
    String Config_MaxCount = "maxCount";

    /**
     * 最新监测电池时间
     */
    String Config_LastBatteryCheckTimestamp = "lastBatteryCheckTimestamp";
    /**
     * 最新发送电池通知时间
     */
    String Config_LastBatteryNotifyTimestamp = "lastBatteryNotifyTimestamp";

    /**
     * 最新处理未同步数据的时间
     */
    String Config_LastProcessUnsyncDataTimestamp = "lastProcessUnsyncDataTimestamp";

    /**
     * 默认的重试次数
     */
    int Default_MaxRetry = 2;
    /**
     * 默认的重试间隔时长
     */
    long Default_RetryInterval = 500L;
    /**
     * 默认的存储短信数
     */
    int Default_MaxCount = 10000;
    /**
     * 默认配置是否电池通知
     */
    boolean Default_BatteryNotifyFlag = true;

    String KEY_BROADCASTRECEIVERRESULT = "broadcastReceiverResultCode";
    String KEY_SENDMESSAGE = "sendMessage";

    /**
     * 消息类型
     */
    enum MessageType {
        /**
         * 简单消息
         */
        SIMPLE,

        /**
         * 短信
         */
        SMS,

        /**
         * 电话记录
         */
        PHONE
        ;
    }

    /**
     * 电话类型
     */
    enum PhoneType {
        /**
         * 来电
         */
        CALL_IN,

        /**
         * 呼出
         */
        CALL_OUT
    }
}
