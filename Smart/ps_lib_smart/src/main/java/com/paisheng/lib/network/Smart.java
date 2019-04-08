package com.paisheng.lib.network;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.paisheng.lib.network.converter.SmartConverter;
import com.paisheng.lib.network.extra.DefaultCallAdapter;
import com.paisheng.lib.network.extra._Smart;
import com.paisheng.lib.network.interceptor.NotIOExceptionInterceptor;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * <br> ClassName:   Smart
 * <br> Description: Smart 网络请求
 * <br>
 * <br> Author:      liaoshengjian
 * <br> Date:        2017/11/3 14:36
 */
public final class Smart {

    //application
    private static Context mContext;

    private static boolean isDebug = false;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private volatile static OkHttpClient client;
    private static OkHttpClient.Builder customBuilder;

    public static void init(Application application) {
        mContext = application;
    }

    /**
     *<br> Description: 设置自定义OkHttpClient.Builder
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/8/5 15:19
     *
     * @param builder OkHttpClient.Builder
     */
    public static void initOkHttp(OkHttpClient.Builder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("builder must not be null.");
        }
        synchronized (Smart.class) {
            if (customBuilder != null) {
                throw new IllegalStateException("customBuilder already exists.");
            }
            customBuilder = builder;
        }
    }

    /**
     *<br> Description: 返回一个OkHttpClient
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:06
     *
     * @return client OkHttpClient
     */
    public static OkHttpClient getClient() {
        if (client == null) {
            synchronized (Smart.class) {
                if (client == null) {
                    if (customBuilder != null) {
                        customBuilder.addInterceptor(new NotIOExceptionInterceptor());
                        client = customBuilder.build();
                    } else {
                        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
                        mBuilder.addInterceptor(new NotIOExceptionInterceptor());
                        mBuilder.sslSocketFactory(createSSLSocketFactory());
                        mBuilder.retryOnConnectionFailure(false);
                        client = mBuilder.build();
                    }

                }
            }
        }
        return client;
    }

    /**
     *<br> Description: 单独返回一个新的OkHttpClient
     *<br> Author:      liaoshengjian
     *<br> Date:        2018/8/10 12:05
     */
    public static OkHttpClient getNewClient(OkHttpClient.Builder builder) {
        if (builder != null) {
            builder.addInterceptor(new NotIOExceptionInterceptor());
            return builder.build();
        }

        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.addInterceptor(new NotIOExceptionInterceptor());
        mBuilder.retryOnConnectionFailure(false);
        return mBuilder.build();
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

    /**
     *<br> Description: Range请求
     *<br> Author:      yexiaochuan
     *<br> Date:        2018/3/14 19:00
     *
     * @param url      请求的Get地址
     * @param downloadPath 下载路径
     * @param fileName     文件名，必须与Url一一对应，否则会出现多个请求地址下载到同一文件
     * @return
     */
    public static RangeRequestCall range(String url, String downloadPath, String fileName) {
        return new RangeRequestCall(url, downloadPath, fileName);
    }

    public static Handler getDelivery() {
        return MAIN_HANDLER;
    }


    /**
     *<br> Description: 指定到UI线程执行
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:08
     *
     * @param runnable Runnable
     */
    public static void runOnUIThread(Runnable runnable) {
        if (runnable != null) {
            MAIN_HANDLER.post(runnable);
        }
    }

    /**
     *<br> Description: 判断是否主线程
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/15 17:18
     *
     * @return boolean 是否主线程
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     *<br> Description: 设置为调试模式
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/11/3 14:38
     *
     * @param mode boolean 模式
     */
    public static void setDebugMode(boolean mode) {
        isDebug = mode;
    }

    /**
     *<br> Description: 获取模式状态
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/11/3 14:38
     *
     * @return boolean 是否为调试模式
     */
    public static boolean getDebugMode() {
        return isDebug;
    }

    /**
     * <br> Description: 是否有网络
     * <br> Author:      liaoshengjian
     * <br> Date:        2017/6/14 16:49
     *
     * @return boolean
     */
    public static boolean isNetWorkAvailable() {
        if (mContext == null) {
            throw new IllegalArgumentException("Method Smart.init() must be called first !!!");
        }
        ConnectivityManager mgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo info = mgr.getActiveNetworkInfo();
            if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }


    /**
     *<br> Description: createSSLSocketFactory
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:10
     *
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory createSSLSocketFactory() {

        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sSLSocketFactory;
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


    private _Smart mSmart;
    public Smart(Builder builder) {
        mSmart = new _Smart(builder);
    }

    public void refreshBaseUrl(String url) {
        if (mSmart == null || mSmart.getBuilder() == null) {
            throw new IllegalArgumentException("new Builder().build() has never called");
        }
        if (!mSmart.getBaseUrl().equals(url)) {
            mSmart.refreshBuilder(mSmart.getBuilder().baseUrl(url));
        }
    }



    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        return mSmart.create(service);
    }

    public static final class Builder {

        private SmartConverter mSmartConverter;
        private CallAdapter mCallAdapter;
        private String mBaseUrl = "'";

        public SmartConverter getSmartConverter() {
            return mSmartConverter;
        }

        public CallAdapter getCallAdapter() {
            return mCallAdapter;
        }

        public String getBaseUrl() {
            return mBaseUrl;
        }

        public Builder(SmartConverter converter, CallAdapter callAdapter) {
            this.mSmartConverter = converter;
            this.mCallAdapter = callAdapter;
        }
        public Builder() {
            this.mCallAdapter = new DefaultCallAdapter();
        }

        public Builder baseUrl(String baseUrl) {
            this.mBaseUrl = baseUrl;
            return this;
        }


        public Smart build() {
            return new Smart(this);
        }
    }




}
