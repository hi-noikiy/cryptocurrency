package com.chen.cryptocurrency.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenxiaotong
 * @date 2018/1/25
 */
public class HttpUtil implements CoinHttpClient {
    private static final int TIME_OUT = 3000;
    private static HttpUtil instance = new HttpUtil();
    private static HttpClient client;
    private static long startTime = System.currentTimeMillis();
    private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private static ConnectionKeepAliveStrategy keepAliveStart = new DefaultConnectionKeepAliveStrategy() {
        @Override
        public long getKeepAliveDuration(
                HttpResponse response,
                HttpContext context) {
            long keepAlive = super.getKeepAliveDuration(response, context);

            if (keepAlive == -1) {
                keepAlive = 5000;
            }
            return keepAlive;
        }
    };

    private HttpUtil() {
        client = HttpClients.custom().setConnectionManager(cm).setKeepAliveStrategy(keepAliveStart).build();
    }

    private static void idleConnectionMonitor() {

        if (System.currentTimeMillis() - startTime > TIME_OUT) {
            startTime = System.currentTimeMillis();
            cm.closeExpiredConnections();
            cm.closeIdleConnections(30, TimeUnit.SECONDS);
        }
    }

    private static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(TIME_OUT)
            .setConnectTimeout(TIME_OUT)
            .setConnectionRequestTimeout(TIME_OUT)
            .build();


    public static HttpUtil getInstance() {
        return instance;
    }

    public HttpClient getHttpClient() {
        return client;
    }

    private HttpPost httpPostMethod(String url) {
        return new HttpPost(url);
    }

    private HttpRequestBase httpGetMethod(String url) {
        return new HttpGet(url);
    }

    @Override
    public String requestHttpGet(String domain, String url, String param) {
        idleConnectionMonitor();
        url = domain + url + param;
        return requestHttpGet(url);
    }

    public String requestHttpGet(String url) {
        HttpRequestBase method = this.httpGetMethod(url);
        method.setConfig(requestConfig);
        HttpResponse response = null;
        try {
            response = client.execute(method);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getResult(response);
    }

    @Override
    public String requestHttpPost(String domain, String path, String param, Map<String, String> params) {

        idleConnectionMonitor();
        String url = domain + path;
        if (!url.endsWith("?")) {
            url += "?";
        }
        url += param;
        HttpPost method = this.httpPostMethod(url);
        List<NameValuePair> valuePairs = this.convertMap2PostParams(params);
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(valuePairs, Consts.UTF_8);
        method.setEntity(urlEncodedFormEntity);
        method.setConfig(requestConfig);
        HttpResponse response = null;
        try {
            response = client.execute(method);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getResult(response);
    }

    private String getResult(HttpResponse response) {
        if (response == null) {
            return "";
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return "";
        }
        InputStream is = null;

        String responseData = "";
        try {
            is = entity.getContent();
            responseData = IOUtils.toString(is, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseData;
    }

    private List<NameValuePair> convertMap2PostParams(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }
        int keySize = keys.size();
        List<NameValuePair> data = new LinkedList<>();
        for (String key : keys) {
            String value = params.get(key);
            data.add(new BasicNameValuePair(key, value));
        }
        return data;
    }
}
