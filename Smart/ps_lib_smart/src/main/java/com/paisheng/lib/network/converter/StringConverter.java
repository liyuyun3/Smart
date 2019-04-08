package com.paisheng.lib.network.converter;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/9/15 16:18
 */
public class StringConverter implements IConverter<String> {

    @Override
    public String parseResponse(Call call, Response response) throws Exception {
        String result = response.body().string();
        Log.d("smart", "StringConverter:" + result);

        return result;
    }

}
