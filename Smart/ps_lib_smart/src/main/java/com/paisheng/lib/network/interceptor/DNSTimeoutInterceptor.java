package com.paisheng.lib.network.interceptor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Based on http://stackoverflow.com/questions/693997/how-to-set-httpresponse-timeout-for-android-in-java/31643186#31643186
 * as per https://github.com/square/okhttp/issues/95
 */
public class DNSTimeoutInterceptor implements Interceptor {

    private long mTimeoutMillis;

    /**
     *<br> Description: 构造函数，设置DNS超时时间
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/11/3 10:24
     *
     * @param timeoutMillis 毫秒
     */
    public DNSTimeoutInterceptor(long timeoutMillis) {
        mTimeoutMillis = timeoutMillis;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        //Log.SplitTimer timer = (request.tag() instanceof RequestTag ? ((RequestTag) request.tag()).getTimer() : null);

        // underlying call should timeout after 2 tries of 5s:  https://android.googlesource.com/platform/bionic/+/android-5.1.1_r38/libc/dns/include/resolv_private.h#137
        // could use our own Dns implementation that falls back to public DNS servers:  https://garage.easytaxi.com/tag/dns-android-okhttp/
        if (!DNSResolver.isDNSReachable(request.url().host(), mTimeoutMillis)) {
            throw new UnknownHostException("DNS timeout");
        }

        return chain.proceed(request);
    }

    /**
     *<br> Description: DNSResolver DNS解析
     *<br> Author:      liaoshengjian
     *<br> Date:        2017/11/3 10:25
     */
    private static class DNSResolver implements Runnable {
        private String mDomain;
        private InetAddress mAddress;

        /**
         *<br> Description: DNS解析是否成功
         *<br> Author:      liaoshengjian
         *<br> Date:        2017/11/3 10:26
         *
         * @param domain 域名
         * @param timeoutMillis 超时时间（毫秒）
         * @return boolean 解析结果
         */
        public static boolean isDNSReachable(String domain, long timeoutMillis) {
            try {
                DNSResolver dnsRes = new DNSResolver(domain);

                Thread t = new Thread(dnsRes, "DNSResolver");
                t.start();
                t.join(timeoutMillis);
                return dnsRes.get() != null;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         *<br> Description: 构造函数
         *<br> Author:      liaoshengjian
         *<br> Date:        2017/11/3 10:27
         *
         * @param domain 域名
         */
        public DNSResolver(String domain) {
            this.mDomain = domain;
        }

        /**
         *<br> Description: 启动线程
         *<br> Author:      liaoshengjian
         *<br> Date:        2017/11/3 10:28
         */
        public void run() {
            try {
                InetAddress addr = InetAddress.getByName(mDomain);
                set(addr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        /**
         *<br> Description: 设置解析的ip信息
         *<br> Author:      liaoshengjian
         *<br> Date:        2017/11/3 10:28
         *
         * @param inetAddr IP信息对象
         */
        public synchronized void set(InetAddress inetAddr) {
            this.mAddress = inetAddr;
        }

        /**
         *<br> Description: 获取IP信息对象
         *<br> Author:      liaoshengjian
         *<br> Date:        2017/11/3 10:28
         *
         * @return InetAddress IP信息对象
         */
        public synchronized InetAddress get() {
            return mAddress;
        }
    }

}