package com.jyoryo.app.android.smsmover.dto.message;

import com.jyoryo.app.android.smsmover.Constants.MessageType;

public class SimpleSendMessage extends AbstractSendMessage {
    private static final long serialVersionUID = -2224505081416101198L;
    private String sender;
    private String title;
    private String content;
    private long dateline;

    public SimpleSendMessage(String sender, String title, String content, long dateline) {
        super(MessageType.SIMPLE);
        this.sender = sender;
        this.title = title;
        this.content = content;
        this.dateline = dateline;
    }

    public String getSender() {
        return sender;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getDateline() {
        return dateline;
    }
}
