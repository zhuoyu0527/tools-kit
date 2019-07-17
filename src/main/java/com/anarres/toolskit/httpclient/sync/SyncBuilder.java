package com.anarres.toolskit.httpclient.sync;

import com.anarres.toolskit.httpclient.ConfigBuilderProxy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.apache.http.nio.conn.ssl.SSLIOSessionStrategy.getDefaultHostnameVerifier;

public class SyncBuilder extends ConfigBuilderProxy<SyncBuilder, HttpClient> {

    private HttpClientBuilder clientBuilder;

    public SyncBuilder(HttpClientBuilder builder) {
        clientBuilder = builder;
    }
    public SyncBuilder() {
        clientBuilder = HttpClientBuilder.create();
    }

    @Override
    public HttpClient build(KeyManager[] keymanagers,
                            TrustManager[] trustmanagers,
                            SecureRandom secureRandom) throws KeyManagementException, NoSuchAlgorithmException {

        if(null == trustmanagers) {
            RequestConfig config = configBuilder.build();
            CloseableHttpClient client = clientBuilder.setDefaultRequestConfig(config).build();
            return new HttpClient(defaultCharset, client, config);
        }
        // 创建 SSL 链接
        final SSLContext sslcontext = SSLContext.getInstance(
                this.protocol != null ? this.protocol : "TLS");

        HostnameVerifier verifier ;
        if(ignoreSSLCert) {
            verifier = new NoopHostnameVerifier();
        } else {
            verifier = getDefaultHostnameVerifier();
        }

        sslcontext.init(keymanagers, trustmanagers, secureRandom);
        RequestConfig config = configBuilder.build();
        CloseableHttpClient client = clientBuilder
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslcontext, verifier))
                .setDefaultRequestConfig(config)
                .build();

        return new HttpClient(defaultCharset, client, config);
    }
}
