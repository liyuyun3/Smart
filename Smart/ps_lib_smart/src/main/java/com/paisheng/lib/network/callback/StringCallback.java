package com.paisheng.lib.network.callback;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:    一般字符串回调处理
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/3/31 17:40
 */
public abstract class StringCallback extends AbstractCallback<String> {

    @Override
    public String parseResponse(Call call, Response response) throws Exception {
        String result = response.body().string();
        Log.d("smart", "StringCallback:" + result);

        return result;
    }


}
