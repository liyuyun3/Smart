package com.paisheng.lib.network.extra;

import com.paisheng.lib.network.CallAdapter;
import com.paisheng.lib.network.Smart;
import com.paisheng.lib.network.converter.SmartConverter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/11/6 09:26
 */
public final class _Smart {

    private SmartConverter mSmartConverter;
    private CallAdapter mCallAdapter;
    private String mBaseUrl = "";
    private Smart.Builder mBuilder;

    private final Map<Method, ServiceMethod> serviceMethodCache = new LinkedHashMap<>();

    public _Smart(Smart.Builder builder) {
        refreshBuilder(builder);
    }

    public void refreshBuilder(Smart.Builder builder) {
        this.mBuilder = builder;
        this.mSmartConverter = builder.getSmartConverter();
        this.mCallAdapter = builder.getCallAdapter();
        this.mBaseUrl = builder.getBaseUrl();

        if (serviceMethodCache.size() > 0) {
            serviceMethodCache.clear();
        }
    }

    public Smart.Builder getBuilder() {
        return this.mBuilder;
    }


    public SmartConverter getSmartConverter() {
        return mSmartConverter;
    }
    public CallAdapter getCallAdapter() {
        return mCallAdapter;
    }
    public String getBaseUrl() {
        return mBaseUrl;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        ServiceMethod serviceMethod = loadServiceMethod(method, args);
                        return  serviceMethod.adapt();
                    }
                });

    }

    private ServiceMethod loadServiceMethod(Method method, Object[] args) {
        ServiceMethod result;
        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method, args).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }




//    public static final class Builder {
//
//        private SmartConverter mSmartConverter;
//        private CallAdapter mCallAdapter;
//
//        public Builder(SmartConverter converter, CallAdapter callAdapter) {
//            this.mSmartConverter = converter;
//            this.mCallAdapter = callAdapter;
//        }
//        public Builder() {
//            this.mCallAdapter = new DefaultCallAdapter();
//        }
//
//        public _Smart build() {
//            return new _Smart(this);
//        }
//
//    }
}
