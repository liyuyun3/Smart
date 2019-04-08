package com.paisheng.lib.network.extra;

import com.paisheng.lib.network.CallAdapter;
import com.paisheng.lib.network.RequestCall;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/11/6 10:47
 */
public class DefaultCallAdapter<T> implements CallAdapter<T> {

    @Override
    public T adapt(RequestCall requestCall) {
        return (T) requestCall;
    }
}
