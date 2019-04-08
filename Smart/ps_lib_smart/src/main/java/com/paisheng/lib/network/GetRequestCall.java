package com.paisheng.lib.network;

import okhttp3.Request;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/9/18 16:39
 */
public class GetRequestCall extends RequestCall {

    /**
     *<br> Description: Get请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/9/18 16:41
     *
     * @param url String
     */
    public GetRequestCall(String url) {
        super(url);
    }

    @Override
    public Request generateRequest() {
        return new Request.Builder()
                .url(getUrl())
                .build();
    }
}
