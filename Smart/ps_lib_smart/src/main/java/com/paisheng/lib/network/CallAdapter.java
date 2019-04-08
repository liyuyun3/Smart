package com.paisheng.lib.network;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/7/24 16:31
 */
public interface CallAdapter<T> {

    T adapt(RequestCall requestCall);

}
