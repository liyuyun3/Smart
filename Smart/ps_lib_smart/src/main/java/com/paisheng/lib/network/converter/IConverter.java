package com.paisheng.lib.network.converter;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/9/14 16:30
 */
public interface IConverter<T> {

    /**
     *<br> Description: 数据解析（子线程）
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:17
     *
     * @param call Call
     * @param response Response
     * @return T
     * @throws Exception e
     */
    T parseResponse(Call call, Response response) throws Exception;


}
