package com.paisheng.lib.network.interceptor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;

import static okhttp3.internal.platform.Platform.INFO;

/**
 * <br> ClassName:   CustomLoggingInterceptor
 * <br> Description: HttpLoggingInterceptor 改进版
 *                      1、修复MultiPartBody 8M文件打印导致的OOM
 *                      2、兼容Unicode中文乱码
 *                      3、OkHttp 3.9
 * <br>
 * <br> Author:      liaoshengjian
 * <br> Date:        2017/11/3 10:33
 */
public class CustomLoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public enum Level {
        /** No logs. */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END GET
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    public interface Logger {
        void log(String message);

        /** A {@link Logger} defaults output appropriate for the current platform. */
        Logger DEFAULT = new Logger() {
            @Override public void log(String message) {
                Platform.get().log(INFO, message, null);
            }
        };
    }

    public CustomLoggingInterceptor() {
        this(Logger.DEFAULT);
    }

    public CustomLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    private final Logger logger;

    private volatile Level level = Level.NONE;

    /** Change the level at which this interceptor logs. */
    public CustomLoggingInterceptor setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        Level level = this.level;

        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == Level.BODY;
        boolean logHeaders = logBody || level == Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        logger.log(requestStartMessage);

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    logger.log("Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    logger.log(name + ": " + headers.value(i));
                }
            }

            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method());
            } else if (bodyEncoded(request.headers())) {
                logger.log("--> END " + request.method() + " (encoded body omitted)");
            } else {
                logger.log("");

                //兼容MultipartBody，大文件打印
                if (requestBody instanceof MultipartBody) {
                    for (int i = 0; i < ((MultipartBody) requestBody).parts().size(); i++) {
                        try {
                            MultipartBody.Part requestPart = ((MultipartBody) requestBody).part(i);
                            Field partHeaders = MultipartBody.Part.class.getDeclaredField("headers");
                            partHeaders.setAccessible(true);
                            logger.log(partHeaders.get(requestPart) + "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);

                    Charset charset = UTF8;
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }
                    logger.log(buffer.readString(charset));
                }
                logger.log("--> END " + request.method()
                        + " (" + requestBody.contentLength() + "-byte body)");
            }
        }

        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        logger.log("<-- " + response.code() + ' ' + response.message() + ' '
                + response.request().url() + " (" + tookMs + "ms" + (!logHeaders ? ", "
                + bodySize + " body" : "") + ')');

        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                logger.log(headers.name(i) + ": " + headers.value(i));
            }

            if (!logBody ||!HttpHeaders.hasBody(response)) {
                logger.log("<-- END HTTP");
            } else if (bodyEncoded(response.headers())) {
                logger.log("<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8);
                    } catch (UnsupportedCharsetException e) {
                        logger.log("");
                        logger.log("Couldn't decode the response body; charset is likely malformed.");
                        logger.log("<-- END HTTP");

                        return response;
                    }
                }

                //206表示这是一个文件content,不打印文件
                if (contentLength != 0 && response.code() != 206) {
                    logger.log("");
                    /*logger.log(buffer.clone().readString(charset));*/
                    logger.log(unicodeToUTF_8(buffer.clone().readString(charset)));
                }

                logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
            }
        }

        return response;
    }

    /**
     * Unicode 转 UTF-8
     */
    private String unicodeToUTF_8(String src) {
        if (null == src) {
            return null;
        }
        System.out.println("src: " + src);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < src.length();) {
            char c = src.charAt(i);
            if (i + 6 < src.length() && c == '\\' && src.charAt(i + 1) == 'u') {
                String hex = src.substring(i + 2, i + 6);
                try {
                    out.append((char) Integer.parseInt(hex, 16));
                } catch (NumberFormatException nfe) {
                    nfe.fillInStackTrace();
                }
                i = i + 6;
            } else {
                out.append(src.charAt(i));
                ++i;
            }
        }
        return out.toString();

    }


    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }
}
