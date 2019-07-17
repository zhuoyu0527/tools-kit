package com.anarres.toolskit.httpclient.async;

import com.anarres.toolskit.httpclient.ConfigBuilderProxy;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class AsyncBuilder extends ConfigBuilderProxy<AsyncBuilder, AsyncHttpClient> {

    /**
     * 该参数是work线程的数量。从new DefaultConnectingIOReactor(ioReactorConfig);
     * 追踪到父类AbstractMultiworkerIOReactor的构造方法可以看出来。
     * 代码为
     * this.workerCount = this.config.getIoThreadCount();
     * this.dispatchers = new BaseIOReactor[workerCount];
     * this.workers = new Worker[workerCount];
     * this.threads = new Thread[workerCount];
     */
    private int workThread = 1;

    private int totalConnectPoolMax = 100;

    private int maxConnectEachHost = 10;

    private Map<HttpRoute, Integer> hostMaxConnect = new HashMap<>();

    private HttpAsyncClientBuilder clientBuilder;

    public AsyncBuilder(HttpAsyncClientBuilder builder) {
        this.clientBuilder = builder;
    }
    public AsyncBuilder() {
        this.clientBuilder = HttpAsyncClients.custom();
    }

    /**
     * 最高连接数
     * apche的并发来源于多连接并发，而不是多线程。连接数越高同一时间发起的请求越多。
     * 异步httpclient的连接池 效用等同于 同步httpclient的线程池。
     * @param totalConnectPoolMax
     * @return
     */
    public AsyncBuilder totalConnectPoolMax(int totalConnectPoolMax) {
        this.totalConnectPoolMax = totalConnectPoolMax;
        return this;
    }

    /**
     * 默认每个域名的最高连接数，换句话说，每个域名最高能同时发起多少个请求。
     * @param maxConnectEachHost
     * @return
     */
    public AsyncBuilder maxConnectEachHost(int maxConnectEachHost) {
        this.maxConnectEachHost = maxConnectEachHost;
        return this;
    }

    public AsyncBuilder hostMaxConnect(String host, int max) {
        hostMaxConnect.put(new HttpRoute(new HttpHost(host)), max);
        return this;
    }
    public AsyncBuilder hostMaxConnect(String host, int port, int max) {
        hostMaxConnect.put(new HttpRoute(new HttpHost(host, port)), max);
        return this;
    }
    public AsyncBuilder hostMaxConnect(HttpHost host, int max) {
        hostMaxConnect.put(new HttpRoute(host), max);
        return this;
    }
    public AsyncBuilder hostMaxConnect(HttpRoute route, int max) {
        hostMaxConnect.put(route, max);
        return this;
    }

    public AsyncBuilder workThread(int workThread) {
        this.workThread = workThread;
        return this;
    }

    public AsyncBuilder workThreadFull() {
        this.workThread = Runtime.getRuntime().availableProcessors();
        return this;
    }

    @Override
    public AsyncHttpClient build(KeyManager[] keymanagers,
                                 TrustManager[] trustmanagers,
                                 SecureRandom secureRandom) throws KeyManagementException, NoSuchAlgorithmException {

        if(null == trustmanagers) {
            RequestConfig config = configBuilder.build();
            CloseableHttpAsyncClient client = clientBuilder.setDefaultRequestConfig(config).build();

            return new AsyncHttpClient(defaultCharset, client, config);
        }
        // SSL context
        final SSLContext sslcontext = SSLContext.getInstance(
                this.protocol != null ? this.protocol : "TLS");
        sslcontext.init(keymanagers, trustmanagers, secureRandom);

        HostnameVerifier verifier;
        if( ignoreSSLCert ) {
            verifier = new NoopHostnameVerifier();
        } else {
            verifier = SSLIOSessionStrategy.getDefaultHostnameVerifier();
        }

        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", new SSLIOSessionStrategy(sslcontext, new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" }, null, verifier))
                .build();

        //配置io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(workThread).build();

        //设置连接池大小
        ConnectingIOReactor ioReactor;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            logger.error("创建 ioReactor 失败", e);
            return null;
        }
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor, sessionStrategyRegistry);

        connManager.setDefaultMaxPerRoute(maxConnectEachHost);//每个域名一次能同时发起多少个请求。
        connManager.setMaxTotal(totalConnectPoolMax);//最多一次能同时发起多少个请求（每个请求就是一个连接）
        hostMaxConnect.forEach((httpRoute, max) -> connManager.setMaxPerRoute(httpRoute, max));//每个域名最多能发起多少个连接。

        CloseableHttpAsyncClient httpclient = clientBuilder
                .setConnectionManager(connManager)
                .build();

        RequestConfig config = configBuilder.build();
        return new AsyncHttpClient(defaultCharset, httpclient, config);
    }
}
