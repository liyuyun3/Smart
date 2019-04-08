package com.paisheng.lib.network;

import android.os.Handler;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:    扩展HttpUtils，统一网络请求
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/3/24 16:04
 */
@Deprecated
public class NewHttpUtils {

    /**
     *<br> Description: 设置自定义OkHttpClient.Builder
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/8/5 15:19
     *
     * @param builder OkHttpClient.Builder
     */
    public static void init(OkHttpClient.Builder builder) {
        Smart.initOkHttp(builder);
    }

    /**
     *<br> Description: 返回一个OkHttpClient
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:06
     *
     * @return client OkHttpClient
     */
    public static OkHttpClient getClient() {
        return Smart.getClient();
    }



    /**
     *<br> Description: post （Json）请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:08
     *
     * @param url String
     * @return PostRequestCall
     */
    public static PostRequestCall post(String url) {
        return new PostRequestCall(url);
    }

    /**
     *<br> Description: get 请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:08
     *
     * @param url String
     * @return RequestCall
     */
    public static GetRequestCall get(String url) {
        return new GetRequestCall(url);
    }


    public static Handler getDelivery() {
        return Smart.getDelivery();
    }


    /**
     *<br> Description: 指定到UI线程执行
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:08
     *
     * @param runnable Runnable
     */
    public static void runOnUIThread(Runnable runnable) {
        Smart.runOnUIThread(runnable);
    }

    /**
     *<br> Description: 判断是否主线程
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/15 17:18
     */
    public static boolean isMainThread() {
        return Smart.isMainThread();
    }

    public static void setDebugMode(boolean mode) {
        Smart.setDebugMode(mode);
    }

    public static boolean getDebugMode() {
        return Smart.getDebugMode();
    }



    /**
     *<br> Description: createSSLSocketFactory
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:10
     *
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory createSSLSocketFactory() {
        return Smart.createSSLSocketFactory();
    }

    /**
     *<br> Description: TrustAllManager
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:10
     */
    private static class TrustAllManager implements X509TrustManager {

        /**
         * checkClientTrusted
         */
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
        }

        /**
         * checkClientTrusted
         */
        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
        }

        /**
         * checkClientTrusted
         */
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }

}
