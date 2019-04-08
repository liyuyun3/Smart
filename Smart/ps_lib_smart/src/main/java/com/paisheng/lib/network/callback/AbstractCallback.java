package com.paisheng.lib.network.callback;

import com.paisheng.lib.network.RequestCall;
import com.paisheng.lib.network.converter.IConverter;
import com.paisheng.lib.network.exception.ApiException;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:    对OkHttp Callback 的扩展
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/3/25 09:52
 */
public abstract class AbstractCallback<T> implements IConverter<T>{

    /**
     *<br> Description: 发起请求前（主线程）
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:17
     */
    public void onStart() {

    }

    /**
     *<br> Description: 请求失败（主线程）
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:18
     *
     * @param requestCall RequestCall
     * @param e ApiException
     */
    public abstract void onFailure(RequestCall requestCall, ApiException e);

    /**
     *<br> Description: 请求成功（主线程）
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:18
     *
     * @param response T
     */
    public abstract void onSuccess(T response);

    /**
     *<br> Description: （下载）进度（主线程）
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:18
     *
     * @param progress float
     * @param total long
     */
    public void onProgress(float progress, long total) {

    }

    /**
     *<br> Description: 请求完成（成功、失败都会调用，主线程）
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:19
     *
     * @param requestCall RequestCall
     * @param response T
     */
    public void onComplete(RequestCall requestCall, T response) {

    }

    /**
     *<br> Description: 是否需要重发，默认为false（不需要失败重发）
     *<br> Author:      liaoshengjian
     *<br> Date:        2018/3/23 16:18
     */
    public boolean onNeedRetry(RequestCall requestCall) {
        return false;
    }
}
