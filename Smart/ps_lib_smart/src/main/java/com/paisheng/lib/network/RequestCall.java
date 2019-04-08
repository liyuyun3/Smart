package com.paisheng.lib.network;

import android.text.TextUtils;

import com.paisheng.lib.network.callback.AbstractCallback;
import com.paisheng.lib.network.converter.IConverter;
import com.paisheng.lib.network.exception.ApiException;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/4/28 10:53
 */
public abstract class RequestCall {

    private String mUrl;

    private String mTaskId;
    private Call mCall;

    private long mReadTimeOut;
    private long mWriteTimeOut;
    private long mConnectTimeout;

    private volatile boolean mCanceled;

    /**
     * 当前请求的AbstractCallback
     */
    private AbstractCallback mAbstractCallback;

    /**
     * 当前请求的IConverter
     */
    private IConverter mIConverter;

    /**
     * 请求管理
     */
    private Reference<IRequestManager> mIRequestManager;

    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:44
     *
     * @param url String
     */
    public RequestCall(String url) {
        this.mUrl = url;
        this.mTaskId = url;
    }

    /**
     *<br> Description: 设置重新加载请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:45
     *
     * @param iSetReloadAction ISetReloadAction
     * @return RequestCall
     */
    public RequestCall setReload(ISetReloadAction iSetReloadAction) {
        if (iSetReloadAction != null) {
            iSetReloadAction.setReloadAction(this);
        }
        return this;
    }

    /**
     *<br> Description: 设置请求管理器
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/23 14:11
     *
     * @param iRequestManager IRequestManager
     * @return RequestCall
     */
    public RequestCall setRequestManager(IRequestManager iRequestManager) {
        if (iRequestManager != null) {
            this.mIRequestManager = new WeakReference<IRequestManager>(iRequestManager);
        }
        return this;
    }

    /**
     *<br> Description: 设置Url
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/8/14 13:48
     *
     * @param url String
     * @return RequestCall
     */
    public RequestCall setUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            this.mUrl = url;
            this.mTaskId = url;
        }
        return this;
    }

    /**
     *<br> Description: 设置读取超时
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:45
     *
     * @param readTimeOut long
     * @return RequestCall
     */
    public RequestCall setReadTimeout(long readTimeOut) {
        this.mReadTimeOut = readTimeOut;
        return this;
    }

    /**
     *<br> Description: 设置写入超时
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:45
     *
     * @param writeTimeOut long
     * @return RequestCall
     */
    public RequestCall setWriteTimeOut(long writeTimeOut) {
        this.mWriteTimeOut = writeTimeOut;
        return this;
    }

    /**
     *<br> Description: 设置socket连接超时
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:45
     *
     * @param connectTimeout long
     * @return RequestCall
     */
    public RequestCall setConnectTimeout(long connectTimeout) {
        this.mConnectTimeout = connectTimeout;
        return this;
    }

    /**
     *<br> Description: 包装OkHttpClient
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:45
     *
     * @return OkHttpClient
     */
    private OkHttpClient generateClient() {
        if (mReadTimeOut <= 0 && mWriteTimeOut <= 0 && mConnectTimeout <= 0) {
            return Smart.getClient();
        } else {
            OkHttpClient.Builder newClientBuilder = Smart.getClient().newBuilder();
            if (mReadTimeOut > 0) {
                newClientBuilder.readTimeout(mReadTimeOut, TimeUnit.MILLISECONDS);
            }
            if (mWriteTimeOut > 0) {
                newClientBuilder.writeTimeout(mWriteTimeOut, TimeUnit.MILLISECONDS);
            }
            if (mConnectTimeout > 0) {
                newClientBuilder.connectTimeout(mConnectTimeout, TimeUnit.MILLISECONDS);
            }
            return newClientBuilder.build();
        }
    }

    /**
     *<br> Description: 获取任务Id
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:45
     *
     * @return String
     */
    public String getTaskId() {
        return TextUtils.isEmpty(mTaskId) ? "" : mTaskId;
    }

    /**
     *<br> Description: 获取Url
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/8/14 13:50
     *
     * @return String
     */
    public String getUrl() {
        return TextUtils.isEmpty(mUrl) ? "" : mUrl;
    }

    /**
     *<br> Description: 获取OkHttp的Call
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:45
     *
     * @return Call
     */
    public Call getCall() {
        return  mCall;
    }

    /**
     *<br> Description: 重新请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:50
     *
     * @return RequestCall
     */
    public RequestCall retry() {
        if (this.mCall != null) {
            this.mCall.cancel();
            this.mCall = null;
        }
        if (mAbstractCallback != null) {
            return execute(mAbstractCallback);
        }
        return null;
    }

    /**
     *<br> Description: 将RequeCall加入请求管理器
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/23 14:17
     *
     * @param requestCall RequestCall
     */
    private void addToRequestManager(RequestCall requestCall) {
        if (this.mIRequestManager != null && this.mIRequestManager.get() != null && requestCall != null) {
            this.mIRequestManager.get().addCalls(requestCall);
        }
    }

    /**
     *<br> Description: 将RequeCall从请求管理器移除
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/23 14:17
     *
     * @param requestCall RequestCall
     */
    private void removeFromReqManager(RequestCall requestCall) {
        if (this.mIRequestManager != null && this.mIRequestManager.get() != null && requestCall != null) {
            this.mIRequestManager.get().removeCall(requestCall);
        }
    }

    /**
     *<br> Description: 兼容RxJava
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/14 16:36
     *
     * @param adapter CallAdapter
     */
    @SuppressWarnings("unchecked")
    public <R> R adapt(CallAdapter<R> adapter) {
        return adapter.adapt(this);
    }

    /**
     *<br> Description: 兼容RxJava，设置转换器IConverter
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/14 16:36
     *
     * @param iConverter IConverter
     * @return RequestCall
     */
    @SuppressWarnings("unchecked")
    public RequestCall converter(IConverter iConverter) {
        this.mIConverter = iConverter;
        return this;
    }

    /**
     *<br> Description: 获取转换器IConverter
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/14 16:42
     */
    public IConverter getConverter() {
        return this.mIConverter;
    }

    public <R> R toRx(IRxTransfer<R> iRxTransfer) {
        this.mIConverter = iRxTransfer.getIConverter();
        return iRxTransfer.getCallAdapter().adapt(this);
    }

    /**
     *<br> Description: 取消请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/19 20:40
     */
    public void cancel() {
        mCanceled = true;
        if (this.mCall != null) {
            this.mCall.cancel();
        }
    }

    /**
     *<br> Description: 是否取消了请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/19 20:40
     */
    public boolean isCanceled() {
        if (mCanceled) {
            return true;
        }
        synchronized (this) {
            return this.mCall != null && this.mCall.isCanceled();
        }
    }

    /**
     *<br> Description: 包装okhttp3.Request，区分post\get
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/19 20:40
     */
    public abstract Request generateRequest();


    /**
     *<br> Description: 执行请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:50
     *
     * @param abstractCallback AbstractCallback
     * @return RequestCall
     */
    public RequestCall execute(AbstractCallback abstractCallback) {

        mAbstractCallback = abstractCallback;
        mAbstractCallback.onStart();

        if (Smart.isMainThread() && !Smart.isNetWorkAvailable()) {
            //TODO mTaskId 处理重新刷新
            sendOnFailure(mAbstractCallback, null, new ApiException(ApiException.CODE_NO_NETWORK, "无网络连接"));
            addToRequestManager(this);
            return this;
        }

        Call mCall = generateClient().newCall(generateRequest());
        if (mCanceled) {
            removeFromReqManager(this);
            return this;
        }
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                removeFromReqManager(RequestCall.this);

                Smart.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mAbstractCallback.onNeedRetry(RequestCall.this)) {
                            if (e instanceof SocketTimeoutException) {
                                sendOnFailure(mAbstractCallback, call,
                                        new ApiException(ApiException.CODE_OKHTTP_SOCKET_TIMEOUT, e, mUrl));
                            } else if (e instanceof SocketException) {
                                sendOnFailure(mAbstractCallback, call,
                                        new ApiException(ApiException.CODE_OKHTTP_SOCKET_EXP, e, mUrl));
                            } else if (e instanceof SSLException) {
                                sendOnFailure(mAbstractCallback, call,
                                        new ApiException(ApiException.CODE_OKHTTP_SSL_EXP, e, mUrl));
                            } else {
                                sendOnFailure(mAbstractCallback, call,
                                        new ApiException(ApiException.CODE_OKHTTP_FAILURE, e, mUrl));
                            }
                        }
                    }
                });

            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                try {
                    removeFromReqManager(RequestCall.this);

                    if (response.isSuccessful()) {
                        final Object o = mAbstractCallback.parseResponse(call, response);
                        Smart.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                sendOnSuccess(mAbstractCallback, o);
                            }
                        });
                    } else {
                        Smart.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!mAbstractCallback.onNeedRetry(RequestCall.this)) {
                                    ApiException apiException = new ApiException(ApiException.CODE_OKHTTP_NO_SUCCESS, response.message(), response.code());
                                    apiException.setUrl(mUrl);
                                    sendOnFailure(mAbstractCallback, call, apiException);
                                }
                            }
                        });

                    }

                } catch (final Exception exp) {
                    Smart.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (exp instanceof ApiException) {
                                sendOnFailure(mAbstractCallback, call, (ApiException) exp);
                            } else {
                                sendOnFailure(mAbstractCallback, call,
                                        new ApiException(ApiException.CODE_OTHER_EXCEPTION, exp));
                            }
                        }
                    });
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });

        this.mCall = mCall;
        addToRequestManager(this);
        return this;
    }

    /**
     *<br> Description: 发送请求成功结果
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:51
     *
     * @param abstractCallback AbstractCallback
     * @param object Object
     */
    private void sendOnSuccess(AbstractCallback abstractCallback, Object object) {
        abstractCallback.onSuccess(object);
        abstractCallback.onComplete(this, object);
    }

    /**
     *<br> Description: 发送请求失败结果
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:51
     *
     * @param abstractCallback AbstractCallback
     * @param call Call
     * @param apiException ApiException
     */
    private void sendOnFailure(AbstractCallback abstractCallback, Call call, ApiException apiException) {
        abstractCallback.onFailure(this, apiException);
        abstractCallback.onComplete(this, null);
    }

}
