package com.jyoryo.app.android.smsmover.dto;

import android.telephony.SmsMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Sms Dto
 */
public class SmsDto implements Serializable {
    private static final long serialVersionUID = 2654054461886114295L;
    private long timestamp;
    private String address;
    private String content;

    public SmsDto(long timestamp, String address, String content) {
        this.timestamp = timestamp;
        this.address = address;
        this.content = content;
    }
    public SmsDto(JSONObject json) throws JSONException {
        this(json.getLong("timestamp"), json.getString("address"), json.getString("content"));
    }
    public SmsDto(SmsMessage sms) {
        this(sms.getTimestampMillis(), sms.getOriginatingAddress(), sms.getMessageBody());
    }


    public long getTimestamp() {
        return timestamp;
    }
    public String getAddress() {
        return address;
    }
    public String getContent() {
        return content;
    }
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("address", address);
            json.put("content", content);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
