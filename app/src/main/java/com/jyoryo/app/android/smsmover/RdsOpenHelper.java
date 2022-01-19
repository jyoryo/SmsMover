package com.jyoryo.app.android.smsmover;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jyoryo.app.android.smsmover.Constants.PhoneType;
import com.jyoryo.app.android.smsmover.dto.PhoneBean;
import com.jyoryo.app.android.smsmover.dto.SmsBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据库 Helper
 */
public class RdsOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sms_mover.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_SMS = "t_sms";
    private static final String CREATE_SMS = "CREATE TABLE t_sms (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, receiver TEXT, content TEXT NOT NULL, dateline INTEGER NOT NULL, syncFlag INTEGER DEFAULT 0, sentFlag INTEGER DEFAULT 0)";
    private static final String TABLE_PHONE = "t_phone";
    private static final String CREATE_PHONE = "CREATE TABLE t_phone (id INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER DEFAULT 0, phone TEXT, relatedPhone TEXT NOT NULL, dateline INTEGER, syncFlag INTEGER DEFAULT 0, sentFlag INTEGER DEFAULT 0)";
    private static RdsOpenHelper mInstance = null;

    // private Context mContext;
    public static RdsOpenHelper getInstance() {
        if(null == mInstance) {
            mInstance = new RdsOpenHelper(SmsMoverApplication.getContext());
        }
        return mInstance;
    }

    private RdsOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SMS);
        db.execSQL(CREATE_PHONE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists t_sms");
        db.execSQL("drop table if exists t_phone");
        onCreate(db);
    }

    /**
     * 保存sms
     * @param sender
     * @param receiver
     * @param content
     * @param dateline
     * @return
     */
    public long saveSms(String sender, String receiver, String content, long dateline) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sender", sender);
        values.put("receiver", receiver);
        values.put("content", content);
        values.put("dateline", dateline);
        values.put("syncFlag", 0);
        values.put("sentFlag", 0);
        return db.insert(TABLE_SMS, null, values);
    }

    /**
     * 更新短信为已同步
     * @param id
     */
    public void updateSmsSynced(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("syncFlag", 1);
        db.update(TABLE_SMS, values, "id = ?", new String [] {String.valueOf(id)});
    }

    /**
     * 更新短信为已发送
     * @param id
     */
    public void updateSmsSent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sentFlag", 1);
        db.update(TABLE_SMS, values, "id = ?", new String [] {String.valueOf(id)});
    }

    /**
     * 获取所有未同步的短信记录
     * @return
     */
    public List<SmsBean> querySmsUnsync() {
        List<SmsBean> items = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_SMS, null, "syncFlag = 0", null, null,null, "dateline ASC");
        if(cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                String sender = cursor.getString(cursor.getColumnIndex("sender"));
                String receiver = cursor.getString(cursor.getColumnIndex("receiver"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                long dateline = cursor.getLong(cursor.getColumnIndex("dateline"));
                boolean syncFlag = (1 == cursor.getInt(cursor.getColumnIndex("syncFlag")));
                boolean sentFlag = (1 == cursor.getInt(cursor.getColumnIndex("sentFlag")));

                items.add(new SmsBean(id, sender, receiver, content, dateline, syncFlag, sentFlag));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    /**
     * 删除短信已同步的历史记录
     */
    public void deleteSmsSynced() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from t_sms where syncFlag = 1");
    }

    /**
     * 保存通话记录
     * @param type
     * @param phone
     * @param relatedPhone
     * @param dateline
     * @return
     */
    public long savePhone(PhoneType type, String phone, String relatedPhone, long dateline) {
        if(0L >= dateline) {
            dateline = new Date().getTime();
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("type", type.ordinal());
        values.put("phone", phone);
        values.put("relatedPhone", relatedPhone);
        values.put("dateline", dateline);
        values.put("syncFlag", 0);
        values.put("sentFlag", 0);
        return db.insert(TABLE_PHONE, null, values);
    }

    /**
     * 更新通话记录已同步
     * @param id
     */
    public void updatePhoneSynced(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("syncFlag", 1);
        db.update(TABLE_PHONE, values, "id = ?", new String[] {String.valueOf(id)});
    }

    /**
     * 更新通话记录为已发送
     * @param id
     */
    public void updatePhoneSent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sentFlag", 1);
        db.update(TABLE_PHONE, values, "id = ?", new String [] {String.valueOf(id)});
    }

    /**
     * 获取所有未同步的通话记录
     * @return
     */
    public List<PhoneBean> queryPhoneUnsync() {
        List<PhoneBean> items = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_PHONE, null, "syncFlag = 0", null, null,null, "dateline ASC");
        if(cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                String phone = cursor.getString(cursor.getColumnIndex("phone"));
                String relatedPhone = cursor.getString(cursor.getColumnIndex("relatedPhone"));
                long dateline = cursor.getLong(cursor.getColumnIndex("dateline"));
                boolean syncFlag = 1 == cursor.getInt(cursor.getColumnIndex("syncFlag"));
                boolean sentFlag = (1 == cursor.getInt(cursor.getColumnIndex("sentFlag")));

                items.add(new PhoneBean(id, type, phone, relatedPhone, dateline, syncFlag, sentFlag));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    /**
     * 删除通话记录已同步的历史记录
     */
    public void deletePhoneSynced() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from t_phone where syncFlag = 1");
    }
}
