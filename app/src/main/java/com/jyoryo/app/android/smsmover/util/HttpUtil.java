package com.jyoryo.app.android.smsmover.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Http请求封装工具
 */
public abstract class HttpUtil {
    private static final String TAG = "HttpUtil";

    /**
     * 构建HttpURLConnection
     * @param requestUrl
     * @param requestMethod
     * @return
     * @throws IOException
     */
    private static HttpURLConnection buildConnection(String requestUrl, String requestMethod) throws IOException {
        Logs.d(TAG, "buildConnection: RequestUrl:" + requestUrl + "___Method:" + requestMethod);
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.setConnectTimeout(5 * 1000);
        connection.setReadTimeout(10 * 1000);
        return connection;
    }

    /**
     * 同步Get 请求
     * @param requestUrl
     */
    public static String get(String requestUrl) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = buildConnection(requestUrl, "GET");
            return inputToString(connection.getInputStream());
        } finally {
            if(null != connection) {
                connection.disconnect();
            }
        }
    }

    public static String inputToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sbd = new StringBuilder();
        String line = null;
        while (null != (line = reader.readLine())) {
            sbd.append(line);
        }
        return sbd.toString();
    }

    /**
     * 同步POST请求
     * @param requestUrl
     * @param params
     * @return
     * @throws Exception
     */
    public static String post(String requestUrl, Map<String, String> params) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = buildConnection(requestUrl, "POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            String postData = postData(params);
            if(!TextUtils.isEmpty(postData)) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(postData);
                writer.close();
            }
            return inputToString(connection.getInputStream());
        } finally {
            if(null != connection) {
                connection.disconnect();
            }
        }
    }

    public static String postData(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder sbd = new StringBuilder();
        boolean first = true;
        String key = null, value = null;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if(TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                continue ;
            }
            if(first) {
                first = false;
            } else {
                sbd.append('&');
            }
            sbd.append(URLEncoder.encode(key, "UTF-8"));
            sbd.append('=');
            sbd.append(URLEncoder.encode(value, "UTF-8"));
        }
        return sbd.toString();
    }
}
