package com.jyoryo.app.android.smsmover.dto;

import java.io.Serializable;

/**
 * 数据库表:sms对应的Java Bean
 */
public class SmsBean implements Serializable {
    private static final long serialVersionUID = -1201669617246138132L;
    private long id;
    private String sender;
    private String receiver;
    private String content;
    private long dateline;
    private boolean syncFlag;
    private boolean sentFlag;

    public SmsBean(long id, String sender, String receiver, String content, long dateline, boolean syncFlag, boolean sentFlag) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.dateline = dateline;
        this.syncFlag = syncFlag;
        this.sentFlag = sentFlag;
    }

    public long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public long getDateline() {
        return dateline;
    }

    public boolean isSyncFlag() {
        return syncFlag;
    }

    public boolean isSentFlag() {
        return sentFlag;
    }
}
