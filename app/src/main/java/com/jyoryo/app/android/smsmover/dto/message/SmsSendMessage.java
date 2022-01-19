package com.jyoryo.app.android.smsmover.dto.message;

import com.jyoryo.app.android.smsmover.Constants.MessageType;
import com.jyoryo.app.android.smsmover.dto.SmsBean;

/**
 * 短信转发消息
 */
public class SmsSendMessage extends AbstractSendMessage {
    private static final long serialVersionUID = 1005468035601804797L;
    private long id;
    private String sender;
    private String receiver;
    private String content;
    private long dateline;
    private boolean sentFlag;

    public SmsSendMessage(long id, String sender, String receiver, String content, long dateline, boolean sentFlag) {
        super(MessageType.SMS);
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.dateline = dateline;
        this.sentFlag = sentFlag;
    }
    public SmsSendMessage(SmsBean smsBean){
        this(smsBean.getId(), smsBean.getSender(), smsBean.getReceiver(), smsBean.getContent(), smsBean.getDateline(), smsBean.isSentFlag());
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

    public boolean isSentFlag() {
        return sentFlag;
    }
}
