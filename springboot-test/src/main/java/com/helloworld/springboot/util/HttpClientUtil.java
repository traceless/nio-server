package com.helloworld.springboot.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

/**
 * 
 * @author doctor
 */
public class HttpClientUtil {

    public static final int CONNECT_TIME_OUT = 5000;
    public static final int READ_TIME_OUT = 8000;
    public static final String CHARSET = "UTF-8";
    public static HttpClient client = null;
    /** 连接数 */
    private static final int MAX_CONN = 2000;

    static {
        // 需要通过以下代码声明对https连接支持
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build();

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            cm.setMaxTotal(MAX_CONN);
            cm.setDefaultMaxPerRoute(MAX_CONN);
            client = HttpClients.custom().setConnectionManager(cm).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public static String getByUrl(String url) {
        HttpGet get = null;
        try {
            get = createGet(url, null, 3000, 3000);
            HttpResponse res = client.execute(get);
            return EntityUtils.toString(res.getEntity(), CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
    }

    public static String get(String url, Map<String, String> headers) throws Exception {
        HttpGet get = createGet(url, headers, 3000, 3000);
        try {
            HttpResponse res = client.execute(get);
            return EntityUtils.toString(res.getEntity(), CHARSET);
        } finally {
            get.releaseConnection();
        }
    }

    private static HttpGet createGet(String url, Map<String, String> headers, Integer connectTimeOut,
            Integer readTimeOut) throws Exception {
        HttpGet get = new HttpGet(url);
        if (headers != null && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                get.addHeader(entry.getKey(), entry.getValue());
            }
        }
        RequestConfig requestConfig = buildRequestConfig(connectTimeOut, readTimeOut);
        get.setConfig(requestConfig);
        return get;
    }

    public static String postJson(String url, String parameterJson, Map<String, String> headers) throws Exception {
        return post(url, parameterJson, headers, "application/json", "utf-8", CONNECT_TIME_OUT, READ_TIME_OUT);
    }

    public static String post(String url, String body, Map<String, String> headers, String mimeType, String charset,
            Integer connTimeout,
            Integer readTimeout) throws Exception {

        HttpPost post = createPostByBody(url, body, mimeType, charset, connTimeout, readTimeout);
        String result;
        if (headers != null && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            HttpResponse res = client.execute(post);
            result = EntityUtils.toString(res.getEntity(), charset);
        } finally {
            post.releaseConnection();
        }
        return result;
    }

    private static HttpPost createPostByBody(String url, String body, String mimeType, String charset,
            Integer connTimeout,
            Integer readTimeout) {
        HttpPost post = new HttpPost(url);
        if (!StringUtils.isEmpty(body)) {
            HttpEntity entity = new StringEntity(body, ContentType.create(mimeType, charset));
            post.setEntity(entity);
        }
        RequestConfig requestConfig = buildRequestConfig(connTimeout, readTimeout);
        post.setConfig(requestConfig);
        return post;
    }

    private static RequestConfig buildRequestConfig(Integer connectTimeOut, Integer readTimeOut) {
        // 设置代理
        RequestConfig.Builder customReqConf = RequestConfig.custom();
        if (connectTimeOut != null) {
            customReqConf.setConnectTimeout(connectTimeOut);
        }
        if (readTimeOut != null) {
            customReqConf.setSocketTimeout(readTimeOut);
        }
        return customReqConf.build();
    }

    public static void main(String[] args) throws Exception {
        String res = HttpClientUtil.get("http://127.0.0.1:7080/test/20", null);
        System.out.println(res);
    }
}
