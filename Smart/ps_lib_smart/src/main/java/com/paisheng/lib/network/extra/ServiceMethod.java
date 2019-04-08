package com.paisheng.lib.network.extra;

import com.paisheng.lib.network.RequestCall;
import com.paisheng.lib.network.Smart;
import com.paisheng.lib.network.http.GET;
import com.paisheng.lib.network.http.POST;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/11/6 09:51
 */
public final class ServiceMethod<T> {

    private _Smart mSmart;
    private Method mMethod;

    private RequestCall mRequestCall;

    public ServiceMethod(Builder builder) {
        this.mSmart = builder.mSmart;
        this.mMethod = builder.mMethod;
        this.mRequestCall = builder.mRequestCall;
    }

    @SuppressWarnings("unchecked")
    public T adapt() {
        return (T) this.mRequestCall
                .converter(this.mSmart.getSmartConverter())
                .adapt(this.mSmart.getCallAdapter());
    }


    static final class Builder {
        private _Smart mSmart;
        private Method mMethod;
        private Object[] mArgs;

        private RequestCall mRequestCall;

        public Builder(_Smart smart, Method method, Object[] args) {
            this.mSmart = smart;
            this.mMethod = method;
            this.mArgs = args;
        }

        public ServiceMethod build() {

            Annotation[] methodAnnotations = mMethod.getAnnotations();
            if (methodAnnotations.length != 1) {
                throw new IllegalArgumentException("At least one annotation from POST or GET");
            }
            if (mArgs.length > 1) {
                throw new IllegalArgumentException("params support only one now, String or HashMap");
            }
//            if (mSmart.getSmartConverter() == null) {
//                throw new IllegalArgumentException("You must set SmartConverter");
//            }
            if (mSmart.getCallAdapter() == null) {
                throw new IllegalArgumentException("You must set CallAdapter");
            }

            //参数
            String params = "";
            if (mArgs.length == 1) {
                Object object = mArgs[0];
                if (object instanceof String) {
                    params = (String) object;
                } else if (object instanceof HashMap) {
                    params = hashMapToJson((HashMap) object);
                }
            }

            //http请求
            String mUrl = mSmart.getBaseUrl();
            Annotation annotation = methodAnnotations[0];
            if (annotation instanceof POST) {
                mUrl += ((POST) annotation).value();
                mRequestCall = Smart.post(mUrl).addJson(params);

            } else if (annotation instanceof GET) {
                mUrl += ((GET) annotation).value();
                mRequestCall = Smart.get(mUrl);

            } else {
                throw new IllegalArgumentException("Annotation must from POST or GET");
            }

            if (mSmart.getSmartConverter() != null) {

                //IConverter
                Type returnType = mMethod.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    Type cls = ((ParameterizedType) returnType).getActualTypeArguments()[0];
                    //Observable<HttpResult<UserInfo>>
                    if (cls instanceof ParameterizedType) {
                        cls = ((ParameterizedType) cls).getActualTypeArguments()[0];
                    }
                    //Observable<UserInfo>
                    mSmart.getSmartConverter().setType(cls);
                }
            }

            return new ServiceMethod(this);
        }
    }

    /**把数据源HashMap转换成json
     * @param map
     */
    public static String hashMapToJson(HashMap map) {
        String string = "{";
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            string += "'" + e.getKey() + "':";
            string += "'" + e.getValue() + "',";
        }
        string = string.substring(0, string.lastIndexOf(","));
        string += "}";
        return string;
    }

}
