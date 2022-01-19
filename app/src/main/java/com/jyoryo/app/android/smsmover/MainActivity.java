package com.jyoryo.app.android.smsmover;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jyoryo.app.android.smsmover.dto.message.SimpleSendMessage;
import com.jyoryo.app.android.smsmover.service.SenderService;

import java.util.Date;

public class MainActivity extends Activity {

    private TextView textAppVersion;
    private EditText textMineMobile;
    private EditText textApiHost;
    private EditText textLoginToken;
    private EditText textServerChanSckey;
    private EditText textMaxRetry;
    private EditText textRetryInterval;
    private EditText textMaxCount;
    private CheckBox checkBoxBatteryNotifyFlag;

    private String mMineMobile;  // 本机号码
    private String mApiHost; // 服务host
    private String mLoginToken;  // 认证Token
    private String mServerChanSckey; // SCKEY
    private int mMaxRetry = Constants.Default_MaxRetry;   // 重试次数
    private long mRetryInterval = Constants.Default_RetryInterval;  // 重试间隔
    private int mMaxCount = Constants.Default_MaxCount;  // 短信数量
    private boolean mBatteryNotifyFlag = Constants.Default_BatteryNotifyFlag;  // 是否电池通知

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 加载数据
        loadSetting();
        // 将数据设置至组件中
        textAppVersion = (TextView) this.findViewById(R.id.appVersion);
        textAppVersion.setText("V" + BuildConfig.VERSION_NAME);
        textMineMobile = (EditText) this.findViewById(R.id.mineMobile);
        textMineMobile.setText(mMineMobile);
        textApiHost = (EditText)this.findViewById(R.id.apiHost);
        textApiHost.setText(mApiHost);
        textLoginToken = (EditText) this.findViewById(R.id.loginToken);
        textLoginToken.setText(mLoginToken);
        textServerChanSckey = (EditText) this.findViewById(R.id.serverChanSckey);
        textServerChanSckey.setText(mServerChanSckey);
        textMaxRetry = (EditText)this.findViewById(R.id.maxRetry);
        textMaxRetry.setText(String.valueOf(mMaxRetry));
        textRetryInterval = (EditText)this.findViewById(R.id.retryInterval);
        textRetryInterval.setText(String.valueOf(mRetryInterval));
        textMaxCount = (EditText)this.findViewById(R.id.maxCount);
        textMaxCount.setText(String.valueOf(mMaxCount));
        checkBoxBatteryNotifyFlag = (CheckBox) this.findViewById(R.id.batteryNotifyFlag);
        checkBoxBatteryNotifyFlag.setChecked(mBatteryNotifyFlag);
        Button btnTest = (Button)this.findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.testMessage, Toast.LENGTH_SHORT).show();
                Intent serviceIntent = new Intent(MainActivity.this, SenderService.class);
                final String deviceModel = Build.MODEL;
                final String title = String.format("%s %s", deviceModel, getString(R.string.testTitle));
                serviceIntent.putExtra(Constants.KEY_SENDMESSAGE, new SimpleSendMessage(getString(R.string.senderSystem), title, getString(R.string.testContent), new Date().getTime()));
                startService(serviceIntent);
            }
        });
        Button btnSave = (Button)this.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMineMobile = textMineMobile.getText().toString();
                mApiHost = textApiHost.getText().toString();
                mLoginToken = textLoginToken.getText().toString();
                mServerChanSckey = textServerChanSckey.getText().toString();
                mMaxRetry = Integer.parseInt(textMaxRetry.getText().toString());
                mRetryInterval = Long.parseLong(textRetryInterval.getText().toString());
                mMaxCount = Integer.parseInt(textMaxCount.getText().toString());
                mBatteryNotifyFlag = checkBoxBatteryNotifyFlag.isChecked();

                saveSetting();
                Toast.makeText(MainActivity.this, R.string.savedMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 检测权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.PROCESS_OUTGOING_CALLS
                },
            1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                boolean auth = true;
                if(grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i ++) {
                        if(PackageManager.PERMISSION_GRANTED != grantResults[i]) {
                            auth = false;
                            break ;
                        }
                    }
                }
                if(!auth) {
                    Toast.makeText(this, R.string.requiredPermission, Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
            default:break;
        }
    }

    /**
     * 加载设置内容
     */
    public void loadSetting() {
        SharedPreferences pref = this.getSharedPreferences(Constants.FILE_NAME_SETTING, Context.MODE_PRIVATE);
        mMineMobile = pref.getString(Constants.Config_MineMobile, mMineMobile);
        mApiHost = pref.getString(Constants.Config_ApiHost, BuildConfig.ApiHost);
        mLoginToken = pref.getString(Constants.Config_LoginToken, BuildConfig.LoginToken);    // mLoginToken
        mServerChanSckey = pref.getString(Constants.Config_ServerChanSckey, BuildConfig.ServerChanSckey);  // mServerChanSckey
        mMaxRetry = pref.getInt(Constants.Config_MaxRetry, Constants.Default_MaxRetry);
        mRetryInterval = pref.getLong(Constants.Config_RetryInterval, Constants.Default_RetryInterval);
        mMaxCount = pref.getInt(Constants.Config_MaxCount, Constants.Default_MaxCount);
        mBatteryNotifyFlag = pref.getBoolean(Constants.Config_BatteryNotifyFlag, Constants.Default_BatteryNotifyFlag);
    }

    /**
     * 保存设置内容
     */
    public void saveSetting() {
        SharedPreferences.Editor editor = this.getSharedPreferences(Constants.FILE_NAME_SETTING, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.Config_MineMobile, mMineMobile);
        editor.putString(Constants.Config_ApiHost, mApiHost);
        editor.putString(Constants.Config_LoginToken, mLoginToken);
        editor.putString(Constants.Config_ServerChanSckey, mServerChanSckey);
        editor.putInt(Constants.Config_MaxRetry, mMaxRetry);
        editor.putLong(Constants.Config_RetryInterval, mRetryInterval);
        editor.putInt(Constants.Config_MaxCount, mMaxCount);
        editor.putBoolean(Constants.Config_BatteryNotifyFlag, mBatteryNotifyFlag);
        editor.apply();
        SenderService.loadConfig();
    }
}
