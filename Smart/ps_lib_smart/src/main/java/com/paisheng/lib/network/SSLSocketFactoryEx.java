package com.paisheng.lib.network;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * <br> ClassName:   SSLSocketFactoryEx
 * <br> Description: SSLSocketFactoryEx
 * <br>
 * <br> Author:      hehaodong
 * <br> Date:        2016-09-13 10:43
 */
public class SSLSocketFactoryEx extends SSLSocketFactory {

    SSLContext mSSLContext = SSLContext.getInstance("TLS");

    /**
     *<br> Description: SSLSocketFactoryEx
     *<br> Author:      hehaodong
     *<br> Date:        2016-09-13 10:43
     *
     * @param truststore KeyStore
     * @throws NoSuchAlgorithmException ne
     * @throws KeyManagementException ke
     * @throws KeyStoreException keye
     * @throws UnrecoverableKeyException ue
     */
    public SSLSocketFactoryEx(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }
        };

        mSSLContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port,
                               boolean autoClose) throws IOException, UnknownHostException {
        return mSSLContext.getSocketFactory().createSocket(socket, host, port,
                autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return mSSLContext.getSocketFactory().createSocket();
    }
}
