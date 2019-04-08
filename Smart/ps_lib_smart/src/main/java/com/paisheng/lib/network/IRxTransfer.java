package com.paisheng.lib.network;

import com.paisheng.lib.network.converter.IConverter;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/10/30 14:27
 */
public interface IRxTransfer<T> {

    IConverter getIConverter();

    CallAdapter<T> getCallAdapter();



}
