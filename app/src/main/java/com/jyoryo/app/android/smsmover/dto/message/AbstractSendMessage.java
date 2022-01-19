package com.jyoryo.app.android.smsmover.dto.message;

import com.jyoryo.app.android.smsmover.Constants.MessageType;

import java.io.Serializable;

/**
 * 消息基类
 */
public abstract class AbstractSendMessage implements Serializable {
    private static final long serialVersionUID = 4282641897572308889L;
    protected MessageType messageType;

    public AbstractSendMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
