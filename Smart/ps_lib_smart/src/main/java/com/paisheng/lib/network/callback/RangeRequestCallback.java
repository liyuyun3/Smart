package com.paisheng.lib.network.callback;

import android.text.TextUtils;
import android.util.Base64;

import com.paisheng.lib.network.RangeRequestCall;
import com.paisheng.lib.network.RequestCall;
import com.paisheng.lib.network.Smart;
import com.paisheng.lib.network.exception.ApiException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * <br> ClassName:   RangeRequestCallback
 * <br> Description: Range请求的callback
 * <br>
 * <br> Author:      yexiaochuan
 * <br> Date:        2018/3/14 15:29
 */
public abstract class RangeRequestCallback extends AbstractCallback<File> {
    private String mSaveFilePath;
    private final int MAX_RETRY = 5;
    private int times = 0;

    public void onStart(String mDownLoadPath, String fileName) {
        mSaveFilePath = mDownLoadPath + "/" + fileName;
    }

    /**
     *<br> Description: 设定416重试机制
     *<br> Author:      yexiaochuan
     *<br> Date:        2018/3/14 18:50
     */
    @Override
    public final void onFailure(RequestCall requestCall, ApiException e) {
        if (!(requestCall instanceof RangeRequestCall)) {
            onRequestFailure(requestCall,e);
            return;
        }

        if (times >= MAX_RETRY) {
            times = 0;
            onRequestFailure(requestCall,e);
            return;
        }

//        Log.i("update","fail : retry times (" + times + ") " + e.toString() + "code : " + e.getResultObject());
        times++;
        //rang起始位置越界 重新设置为0 重试机制重新开始
        if (e.getResultObject() instanceof Integer && ((Integer)e.getResultObject()) == 416) {
            times = 0;
            ((RangeRequestCall) requestCall).resetRangeSPos();
        }
        requestCall.retry();
    }

    public abstract void onRequestFailure(RequestCall requestCall, ApiException e);

    @Override
    public File parseResponse(Call call, Response response) throws Exception {
//        Log.i("update","response header : " + response.headers().toString());
        if (call instanceof RangeRequestCall) {
            ((RangeRequestCall) call).resetOK();
        }
        return readAndSave2File(response.body(),response.headers());
    }

    private File readAndSave2File(ResponseBody body, Headers headers) throws IOException, ApiException {
        byte[] buf = new byte[2048];
        long totalSize = 0;
        long rangSPos = 0;
        RandomAccessFile mDownLoadFile = null;
        try {
            // 获得下载保存文件
            mDownLoadFile = new RandomAccessFile(mSaveFilePath, "rwd");
            // 获得本地下载的文件大小
            long fileLength = mDownLoadFile.length();

            //获取文件总大小和下载起始位置 content-range:bytes $start-$end/$total
            if (!TextUtils.isEmpty(headers.get("content-range"))) {
                String[] value = headers.get("content-range").split("/");
                if (value.length == 2) {
                    String[] posValue = value[0].replace("bytes ","").split("-");
                    if (posValue.length > 0) {
                        rangSPos =  Long.valueOf(posValue[0]);
                    }
                    totalSize = Long.valueOf(value[1]);
                }
            } else {
                //没有content-range 证明不是使用range请求 contentLength就是文件总大小
                totalSize = body.contentLength();
                rangSPos = 0;
            }

            //如果重新获取到的文件总大小等于已下载文件总大小 下载已完成
            if (totalSize != 0 && totalSize == fileLength) {
                final long finalTotalSize = totalSize;
                Smart.runOnUIThread(new Runnable() {
                    public void run() {
                        onProgress(1, finalTotalSize);
                    }
                });
                return verifyMD5(mDownLoadFile,mSaveFilePath,headers.get("content-md5"));
            }

            //文件超过总大小 下载起始位置与文件末尾不一致
            if (totalSize < fileLength || rangSPos != fileLength) {
                deleteFileAndThrowError(mDownLoadFile,mSaveFilePath);
            }

            long sum = rangSPos;
            float times = 0;

            // 写文件的位置需要与Range Header的起始位置一致
            mDownLoadFile.seek(sum);

            int length = 0;
            while ((length = body.byteStream().read(buf)) != -1) {
                sum += length;
                mDownLoadFile.write(buf, 0, length);
                final float progress = sum * 1.0f / totalSize;
                // 更新进度，回调100次
                if ((int) (100 * progress) - (int) (100 * times) >= 1) {
                    times = progress;
                    // 更新状态
                    final long finalTotalSize1 = totalSize;
                    Smart.runOnUIThread(new Runnable() {
                        public void run() {
                            onProgress(progress, finalTotalSize1);
                        }
                    });
                }
            }
        } finally {
            // 回收资源
            close(body.byteStream());
            close(mDownLoadFile);
        }
        return verifyMD5(mDownLoadFile,mSaveFilePath,headers.get("content-md5"));
    }

    /**
     *<br> Description: 检验文件MD5
     *<br> Author:      yexiaochuan
     *<br> Date:        2018/3/14 18:55
     */
    private File verifyMD5(RandomAccessFile file, String path, String md5) throws ApiException {
        File apk = new File(path);
        //没有MD5不做检验
        if (TextUtils.isEmpty(md5)) {
            return apk;
        }

        if (getContentMD5(apk).equals(md5)) {
            return apk;
        }

        deleteFileAndThrowError(file,path);
        return null;
    }

    private String getContentMD5(File file){
        if (file == null || !file.isFile() || !file.exists()) {
            return "";
        }
        FileInputStream in = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            byte[] buffer=new byte[8192];
            int read=0;
            while( (read = in.read(buffer)) > 0)
                md.update(buffer, 0, read);
            byte[] md5 = md.digest();
            return Base64.encodeToString(md5,Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(null!=in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     *<br> Description: 文件检验失败或者文件大小越界 删除
     *<br> Author:      yexiaochuan
     *<br> Date:        2018/3/14 18:48
     */
    private void deleteFileAndThrowError(RandomAccessFile file, String path) throws ApiException {
        close(file);
        new File(path).delete();
        throw new ApiException(ApiException.CODE_OTHER_EXCEPTION,"file is wrong",416);
    }

    private void close(Closeable closeable) {
        try {
            if(closeable != null){
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
