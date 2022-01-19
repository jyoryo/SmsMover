package com.jyoryo.app.android.smsmover.dto;

import com.jyoryo.app.android.smsmover.Constants.PhoneType;

import java.io.Serializable;

/**
 * 数据库表:phone_record对应的Java Bean
 */
public class PhoneBean implements Serializable {
    private static final long serialVersionUID = 1246020637615698405L;
    private long id;
    private int type;
    private String phone;
    private String relatedPhone;
    private long dateline;
    private boolean syncFlag;
    private boolean sentFlag;

    public PhoneBean(long id, int type, String phone, String relatedPhone, long dateline, boolean syncFlag, boolean sentFlag) {
        this.id = id;
        this.type = type;
        this.phone = phone;
        this.relatedPhone = relatedPhone;
        this.dateline = dateline;
        this.syncFlag = syncFlag;
        this.sentFlag = sentFlag;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }
    public PhoneType getPhoneType() {
        for(PhoneType phoneType : PhoneType.values()) {
            if(type == phoneType.ordinal()) {
                return phoneType;
            }
        }
        return null;
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

    public boolean isSyncFlag() {
        return syncFlag;
    }

    public boolean isSentFlag() {
        return sentFlag;
    }
}
