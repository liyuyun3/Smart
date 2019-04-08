package com.paisheng.lib.network;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/5/12 11:15
 */
public interface IRequestManager<T> {

    void addCalls(T call);

    void removeAllCalls();

    void removeCall(T call);

}
