package com.paisheng.lib.network.callback;

import com.paisheng.lib.network.Smart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author: liaoshengjian
 * @Filename:
 * @Description:    文件下载回调处理
 * @Copyright: Copyright (c) 2016 Tuandai Inc. All rights reserved.
 * @date: 2017/3/25 16:11
 */
public abstract class FileCallback extends AbstractCallback<File> {

    private String mSaveFileDir;

    private String mSaveFileName;

    /**
     *<br> Description: 构造函数
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:25
     *
     * @param saveFileDir 保存文件目录
     * @param saveFileName 保存文件名
     */
    public FileCallback(String saveFileDir, String saveFileName) {
        this.mSaveFileDir = saveFileDir;
        this.mSaveFileName = saveFileName;
    }

    @Override
    public File parseResponse(Call call, Response response) throws Exception {
        return saveFile(response);
    }

    /**
     *<br> Description: 保存文件
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/6/13 15:26
     *
     * @param response Response
     * @return File
     * @throws IOException e
     */
    private File saveFile(Response response) throws IOException {

        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;

        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();

            long sum = 0;
            File dir = new File(mSaveFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, mSaveFileName);
            fos = new FileOutputStream(file);

            float lastProgress = 0;
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;

                //通知刷新进度
                //过滤频繁刷新抖动，导致的卡顿问题
                final float newProgress = finalSum * 1.0f / total;
                if ((int) (100 * newProgress) - (int) (100 * lastProgress) > 1) {
                    Smart.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            onProgress(newProgress, total);
                        }
                    });
                    lastProgress = newProgress;
                }
            }
            fos.flush();

            return file;


        } finally {
            try {
                response.body().close();
                if (is != null) {
                    is.close();
                }
            } catch (IOException exp) {
                exp.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException exp) {
                exp.printStackTrace();
            }
        }
    }




}
