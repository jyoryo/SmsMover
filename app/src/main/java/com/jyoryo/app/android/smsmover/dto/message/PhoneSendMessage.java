package com.jyoryo.app.android.smsmover.dto.message;

import com.jyoryo.app.android.smsmover.Constants;
import com.jyoryo.app.android.smsmover.Constants.PhoneType;
import com.jyoryo.app.android.smsmover.dto.PhoneBean;

/**
 * 通话记录转发消息
 */
public class PhoneSendMessage extends AbstractSendMessage {
    private static final long serialVersionUID = 6517869726081970021L;
    private long id;
    private PhoneType type;
    private String phone;
    private String relatedPhone;
    private long dateline;
    private boolean sentFlag;

    public PhoneSendMessage(long id, PhoneType type, String phone, String relatedPhone, long dateline, boolean sentFlag) {
        super(Constants.MessageType.PHONE);
        this.id = id;
        this.type = type;
        this.phone = phone;
        this.relatedPhone = relatedPhone;
        this.dateline = dateline;
        this.sentFlag = sentFlag;
    }
    public PhoneSendMessage(PhoneBean phoneBean) {
        this(phoneBean.getId(), phoneBean.getPhoneType(), phoneBean.getPhone(), phoneBean.getRelatedPhone(), phoneBean.getDateline(), phoneBean.isSentFlag());
    }

    public long getId() {
        return id;
    }

    public PhoneType getType() {
        return type;
    }

    public String getPhone() {
        return phone;
    }

    public String getRelatedPhone() {
        return relatedPhone;
    }

    public long getDateline() {
        return dateline;
    }

    public boolean isSentFlag() {
        return sentFlag;
    }
}
