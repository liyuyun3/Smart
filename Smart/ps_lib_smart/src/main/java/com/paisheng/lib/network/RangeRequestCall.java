package com.paisheng.lib.network;

import android.util.Log;

import com.paisheng.lib.network.callback.AbstractCallback;
import com.paisheng.lib.network.callback.RangeRequestCallback;

import java.io.File;

import okhttp3.Request;

/**
 * <br> ClassName:   RangeRequestCall
 * <br> Description: Range请求
 * <br>
 * <br> Author:      yexiaochuan
 * <br> Date:        2018/3/14 15:18
 */
public class RangeRequestCall extends RequestCall {
    private final String mFileName;
    private long mRangeSPos;
    private String mDownLoadPath;
    private String mFilePath;
    private boolean mIsRest;

    public RangeRequestCall(String url, String downLoadPath, String fileName) {
        super(url);
        mDownLoadPath = downLoadPath;
        mFileName = fileName;
        mFilePath = downLoadPath + "/" + mFileName;
    }

    /**
     *<br> Description: 重置起始位置为0
     *<br> Author:      yexiaochuan
     *<br> Date:        2018/3/20 10:11
     */
    public void resetRangeSPos() {
        mIsRest = true;
        mRangeSPos = 0;
    }

    /**
     *<br> Description: 重置请求成功
     *<br> Author:      yexiaochuan
     *<br> Date:        2018/3/20 10:44
     */
    public void resetOK() {
        mIsRest = false;
    }

    @Override
    public Request generateRequest() {
        if (!mIsRest) {
            mRangeSPos = new File(mFilePath).length();  //向服务器定位下载位置的起始点
//                    Log.i("update","rangeStart : " + mRangeSPos);
        }
        return (new Request.Builder()).url(this.getUrl()).addHeader("RANGE", "bytes=" + mRangeSPos + "-").build();
    }

    @Override
    public RequestCall execute(AbstractCallback abstractCallback) {
        if (abstractCallback == null) {
            return this;
        }

        if (abstractCallback instanceof RangeRequestCallback) {
            ((RangeRequestCallback)abstractCallback).onStart(mDownLoadPath, mFileName);
        }
        return super.execute(abstractCallback);
    }
}
