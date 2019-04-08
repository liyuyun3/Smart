package com.paisheng.lib.network.exception;

import android.text.TextUtils;

import com.paisheng.lib.network.Response;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:    统一异常类
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/4/6 11:27
 */
public class ApiException extends Exception {

    /***无网络连接***/
    public static final int CODE_NO_NETWORK = 4000;

    /***OkHttp 回调 onFailure***/
    public static final int CODE_OKHTTP_FAILURE = 4001;
    /***OkHttp response.isSuccessful() == false***/
    public static final int CODE_OKHTTP_NO_SUCCESS = 4002;

    /***response.code == 299***/
    public static final int CODE_REQ_TOOFREQUEST = 4003;
    /***!response.isSuccessful()***/
    public static final int CODE_REQ_FAILURE = 4004;

    /***OkHttp 回调 onFailure——SocketTimeoutException***/
    public static final int CODE_OKHTTP_SOCKET_TIMEOUT = 4005;
    /***OkHttp 回调 onFailure——SocketException***/
    public static final int CODE_OKHTTP_SOCKET_EXP = 4006;
    /***OkHttp 回调 onFailure——SSLException***/
    public static final int CODE_OKHTTP_SSL_EXP = 4007;


    //TODO 这个考虑放在哪里比较好
    /***"数据异常: 500"; 请求JSON数据异常***/
    public static final int CODE_REQ_JSON_ERROR = 5000;
    /***"数据解析异常：501"; JSON解析错误***/
    public static final int CODE_RES_JSON_PARSE_ERROR = 5001;
    /***"数据解析异常：502"; 解密错误***/
    public static final int CODE_RES_DECODE_ERROR = 5002;
    /***"数据解析异常：503"; 加密错误***/
    public static final int CODE_REQ_ENCRY_ERROR = 5003;
    /***"数据解析异常：504"; JSON解析错误***/
    public static final int CODE_RES_JSON_PARSE_ERROR_2 = 5004;
    /***"数据异常：506"; 其它错误***/
    public static final int CODE_RES_OTHER_ERROR = 5006;
    /***"数据异常：507"; 服务器数据错误***/
    public static final int CODE_RES_DATA_EMPTY = 5007;
    /***"数据异常：508"; 服务器数据错误***/
    public static final int CODE_RES_NO_RETURN_CODE = 5008;

    /***系统维护中***/
    public static final int CODE_REQ_SERVER_DOWN = 5009;
    /***重新登录***/
    public static final int CODE_REQ_RELOGIN = 5010;
    /***学籍信息认证错误***/
    public static final int CODE_REQ_SCHOOL_AUTH_ERROR = 5011;
    /***请求时间异常***/
    public static final int CODE_REQ_TIME_ERROR = 5012;
    /***其他请求错误***/
    public static final int CODE_REQ_OTHER_ERROR = 5013;

    /***运营配置数据与本地相同，不做回掉处理***/
    public static final int CODE_REQ_UNDO_ERROR = 5050;

    /***其余异常***/
    public static final int CODE_OTHER_EXCEPTION = 7000;

    /**
     * 错误码
     */
    private int mCode;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 请求URL
     */
    private String mUrl;

    /**
     * 异常处理Object
     */
    private Object mResultObject;


    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:34
     *
     * @param code int
     * @param message String
     * @param throwable Throwable
     */
    public ApiException(int code, String message, Throwable throwable) {
        super(message, throwable);
        this.mCode = code;
    }

    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:34
     *
     * @param code int
     * @param message String
     */
    public ApiException(int code, String message) {
        super(message);
        this.mCode = code;
    }

    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:34
     *
     * @param code int
     * @param throwable Throwable
     */
    public ApiException(int code, Throwable throwable) {
        super(throwable);
        this.mCode = code;
    }

    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:34
     *
     * @param code int
     * @param throwable Throwable
     * @param url String
     */
    public ApiException(int code, Throwable throwable, String url) {
        super(throwable);
        this.mCode = code;
        this.mUrl = url;
    }

    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:34
     *
     * @param code int
     * @param message String
     * @param resultObject Object
     */
    public ApiException(int code, String message, Object resultObject) {
        super(message);
        this.mCode = code;
        this.mResultObject = resultObject;
    }

    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/18 18:33
     */
    public ApiException(Response<?> response) {
        super(getMessage(response));
        this.mCode = response.code();
        this.message = response.message();
    }


    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    @Override
    public String getMessage() {
        if (TextUtils.isEmpty(message)) {
            if (TextUtils.isEmpty(super.getMessage())) {
                return "Unknown Error!";
            } else {
                return super.getMessage();
            }
        } else {
            return message;
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public Object getResultObject() {
        return mResultObject;
    }

    public void setResultObject(Object resultObject) {
        this.mResultObject = resultObject;
    }

    /**
     *<br> Description: 获取Response信息
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/18 18:34
     *
     * @param response Response
     * @return String
     */
    private static String getMessage(Response<?> response) {
        if (response == null) {
            return "Response is null!!! ";
        }
        return "HTTP " + response.code() + " " + response.message();
    }
}
