package com.paisheng.lib.network.interceptor;

import com.paisheng.lib.network.Smart;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * okhttp AsyncCall only catch the IOException, other exception will occur crash，this Interceptor impl can transform all exception to IOException
 * <p>
 * see : https://github.com/square/okhttp/issues/3477
 */
public class NotIOExceptionInterceptor implements Interceptor {
    private static final String TAG = "Smart.OkHttp3";

    private CatchListener mListener;

    public NotIOExceptionInterceptor() {
    }

    public NotIOExceptionInterceptor(CatchListener listener) {
        this.mListener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            return chain.proceed(chain.request());
        } catch (Throwable e) {
            if (Smart.getDebugMode()) {
                e.printStackTrace();
            }

            if (e instanceof IOException) {
                throw e;
            }

            if (mListener != null) {
                mListener.reportException();
            }
            throw new IOException(e);
        }
    }

    private interface CatchListener {
        void reportException();
    }
}