package com.paisheng.lib.network;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/4/28 10:53
 */
public class PostRequestCall extends RequestCall {


    /**
     * Json
     */
    private String mContent = "";

    /**
     * 普通表单
     */
    private LinkedHashMap<String, String> mSimpleParams = new LinkedHashMap<>();

    /**
     * MultiPart
     */
    private LinkedHashMap<String, List<MultiPartFile>> mFileParams;


    /**
     *<br> Description: Post请求
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:41
     *
     * @param url String
     */
    public PostRequestCall(String url) {
        super(url);
    }


    /**
     *<br> Description: 增加Json参数
     *                  *注意：与addParams互斥，addJson优先级最高
     *
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:41
     *
     * @param json String
     * @return PostRequestCall
     */
    public PostRequestCall addJson(String json) {
        this.mContent = json;
        return this;
    }

    /**
     *<br> Description: 增加表单数据K-V
     *                  *注意：与addJson互斥，addJson优先级最高
     *
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/12/21 14:47
     *
     * @param key 键
     * @param value 值
     * @return PostRequestCall
     */
    public PostRequestCall addParams(String key, String value) {
        mSimpleParams.put(key, value);
        return this;
    }

    /**
     *<br> Description: 批量增加表单数据K-V （Map数据格式）
     *                  *注意：与addJson互斥，addJson优先级最高
     *
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/12/21 14:48
     *
     * @param paramsMap Map数据格式表单数据
     * @return PostRequestCall
     */
    public PostRequestCall addParams(Map<String, String> paramsMap) {
        if (paramsMap != null && !paramsMap.isEmpty()) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                mSimpleParams.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     *<br> Description: 增加文件（Multipart提交）
     *                  *注意：与addJson互斥，addJson优先级最高
     *
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/12/21 14:48
     *
     * @param key key
     * @param file 上传文件
     * @param fileName 上传文件名
     * @return PostRequestCall
     */
    public PostRequestCall addParams(String key, File file, String fileName) {
        if (!TextUtils.isEmpty(key) && file != null) {
            if (mFileParams == null) {
                mFileParams = new LinkedHashMap<>();
            }

            List<MultiPartFile> fileList = mFileParams.get(key);
            if (fileList == null) {
                fileList = new ArrayList<>();
                mFileParams.put(key, fileList);
            }
            fileList.add(new MultiPartFile(file,
                    !TextUtils.isEmpty(fileName) ? fileName : file.getName()));
        }
        return this;
    }

    @Override
    public Request generateRequest() {
        Request.Builder mBuilder = new Request.Builder().url(getUrl());

        //默认："application/json; charset=utf-8"
        if (!TextUtils.isEmpty(mContent) ||
                (mSimpleParams.isEmpty() && (mFileParams == null || mFileParams.isEmpty()))) {
            return mBuilder
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mContent))
                    .build();
        }

        //普通表单提交
        if (mFileParams == null || mFileParams.isEmpty()) {
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            for (String key : mSimpleParams.keySet()) {
                bodyBuilder.addEncoded(key, mSimpleParams.get(key));
            }
            return mBuilder
                    .post(bodyBuilder.build())
                    .build();
        }

        //multipart
        MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (!mSimpleParams.isEmpty()) {
            for (String key : mSimpleParams.keySet()) {
                multipartBodybuilder.addFormDataPart(key, mSimpleParams.get(key));
            }
        }
        //拼接文件
        for (Map.Entry<String, List<MultiPartFile>> entry : mFileParams.entrySet()) {
            List<MultiPartFile> fileValues = entry.getValue();
            for (MultiPartFile multiPartFile : fileValues) {
                RequestBody fileBody = RequestBody.create(MultipartBody.FORM, multiPartFile.mFile);
                multipartBodybuilder.addFormDataPart(entry.getKey(), multiPartFile.mFileName, fileBody);
            }
        }
        return mBuilder
                .post(multipartBodybuilder.build())
                .build();
    }


    /** 文件类型的包装类 */
    private static final class MultiPartFile {

        /**
         * 文件
         */
        private File mFile;
        /**
         * 文件名
         */
        private String mFileName;

        /**
         *<br> Description: 构造函数
         *<br> Author:      liaoshengjian
         *<br> Date:        2017/12/21 14:55
         *
         * @param file 文件
         * @param fileName 文件名
         */
        private MultiPartFile(File file, String fileName) {
            this.mFile = file;
            this.mFileName = fileName;
        }

    }
}
