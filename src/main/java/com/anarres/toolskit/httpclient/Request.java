package com.anarres.toolskit.httpclient;

import com.anarres.toolskit.httpclient.async.AsyncHttpClient;
import com.anarres.toolskit.httpclient.exception.CallHttpException;
import com.anarres.toolskit.httpclient.sync.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

;
//import org.apache.http.annotation.GuardedBy;
//import org.apache.http.annotation.ThreadSafe;

/**
 * 一个简单的HttpRequest。 作用是描述请求内容
 *  多数时候Fluent 通过使用{@link org.apache.http.client.fluent.Request}的实现已经完全够用，例如 String html = org.apache.http.client.fluent.Request.Post("http://www.test.com").execute().returnContent().asString();
 *  Request 的主要作用是和 HttpClient 配合使用，HttpClient提供默认的配置。某些情况下，例如某个平台下的所有接口都必须使用SSL。因此每个
 *  Request的构建都比较麻烦，使用HttpClient可以简化这种配置，并提供给Request。
 *  使用Request的异步方式时，不用担心request和response不一致的情况。换句话说，同一时间发送N个request，每个A request返回的Promise不会获取到 B request的结果。
 *  即便访问N个request访问的是同一个网站，也不混淆。通过每个request开启一个连接，这样就能保证request和response的一致)。
 * Created by ath on 2016/10/28.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class Request implements Supplier<HttpRequestBase> {

    private final String url;
    private final HttpMethod method;
    private final Map<String, String> heads;
    private final Map<String, String> params;
    private final HttpEntity entity;

    private final int connTimeout;
    private final int readTimeout;

    //@GuardedBy("this")
    private HttpRequestBase base;

    private final HttpHost proxy;

    private final String charset;


    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getHeads() {
        return heads;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public HttpHost getProxy() {
        return proxy;
    }

    public String getCharset() {
        return charset;
    }

    public String getURL() {
        return url;
    }

    public String getMethodString() {
        return method.toString();
    }

    public int getConnTimeout() {
        return this.connTimeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public Request(String url, HttpMethod method, Map<String, String> heads, Map<String, String> params, HttpEntity entity, HttpHost proxy, String charset, int connTimeout, int readTimeout) {
        this.method = method;
        this.heads = heads;
        this.params = params;
        this.proxy = proxy;
        this.charset = charset;
        this.entity = entity;
        this.url = url;
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
        initBase();
    }

    public Response execute(HttpClient client) throws CallHttpException, IOException {
        if(null == entity) {
            return client.exec(base, params, heads, charset, proxy, connTimeout, readTimeout);
        } else {
            return client.exec(base, entity, heads, charset, proxy, connTimeout, readTimeout);
        }
    }

    public CompletableFuture<Response> execute(AsyncHttpClient client) {
        if(null == entity) {
            return client.exec(base, params, null, heads, charset, proxy, connTimeout, readTimeout);
        } else {
            return client.exec(base, entity, null, heads, charset, proxy, connTimeout, readTimeout);
        }
    }

    public CompletableFuture<String> executeAsString(AsyncHttpClient client) {
        if(null == entity) {
            return client.exec(base, params, null, heads, charset, proxy, connTimeout, readTimeout).thenApply(Response::asString);
        } else {
            return client.exec(base, entity, null, heads, charset, proxy, connTimeout, readTimeout).thenApply(Response::asString);
        }
    }

    public String executeAsString(HttpClient client) throws CallHttpException, IOException {
        return execute(client).asString();
    }

    public Response execute() throws CallHttpException, IOException {

        if(null == entity) {
            return Https.syncClient.exec(base, params, heads, charset, proxy, connTimeout, readTimeout);
        } else {
            return Https.syncClient.exec(base, entity, heads, charset, proxy, connTimeout, readTimeout);
        }
    }

    /*public static boolean isRunning(){
        return Https.asyncClient.isRunning();
    }

    public static void shutdown(){
        Https.asyncClient.shutdown();
    }*/

    /*public static void start(){
        Https.Rebuild();
    }*/

    public String executeAsString() throws CallHttpException, IOException {
        return execute().asString();
    }

   /* *//**
     * 如果当前异步HttpClient工作线程挂掉了,尝试重建HttpClient
     * 在同步方法里面进行二次判断,避免HttpClient重复创建,造成内存泄漏
     *//*
    public synchronized void restart(){
        if(!this.isRunning()){
            Request.start();
        }
    }
    public void ensureRunning(){
        if(!this.isRunning()){
            this.restart();
        }
    }*/


    public CompletableFuture<Response> asyncExecute() {
        //this.ensureRunning();
        if(null == entity) {
            return Https.asyncClient.exec(base, params, null, heads, charset, proxy, connTimeout, readTimeout);
        } else {
            return Https.asyncClient.exec(base, entity, null, heads, charset, proxy, connTimeout, readTimeout);
        }
    }

    public CompletableFuture<String> asyncExecuteAsString() {
        return asyncExecute().thenApply(Response::asString);
    }



    public HttpRequestBase getHttpRequestBase() {
        return base;
    }

    private synchronized void initBase() {

        if(HttpMethod.DELETE == method) {
            this.base = new HttpDelete(url);
        }
        if(HttpMethod.GET == method) {
            this.base = new HttpGet(url);
        }
        if(HttpMethod.HEAD == method) {
            this.base = new HttpHead(url);
        }
        if(HttpMethod.OPTIONS == method) {
            this.base = new HttpOptions(url);
        }
        if(HttpMethod.PATCH == method){
            this.base = new HttpPatch(url);
        }
        if(HttpMethod.POST == method) {
            this.base = new HttpPost(url);
        }
        if(HttpMethod.PUT == method) {
            this.base = new HttpPut(url);
        }
        if(HttpMethod.TRACE == method) {
            this.base = new HttpTrace(url);
        }

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder Post(String url) {
        return newBuilder().method(HttpMethod.POST).url(url);
    }
    public static Builder Get(String url) {
        return newBuilder().method(HttpMethod.GET).url(url);
    }

    public static Builder Head(String url) {
        return newBuilder().method(HttpMethod.HEAD).url(url);
    }
    public static Builder Delete(String url) {
        return newBuilder().method(HttpMethod.DELETE).url(url);
    }
    public static Builder Patch(String url) {
        return newBuilder().method(HttpMethod.PATCH).url(url);
    }
    public static Builder Put(String url) {
        return newBuilder().method(HttpMethod.PUT).url(url);
    }
    public static Builder Trace(String url) {
        return newBuilder().method(HttpMethod.TRACE).url(url);
    }

    @Override
    public HttpRequestBase get() {
        return base;
    }

    public static class Builder {
        private HttpMethod method = HttpMethod.GET;
        private Map<String, String> headers;
        private Map<String, String> params;
        private HttpEntity entity;

        private String body;

        private String url;

        private int connTimeout;
        private int readTimeout;

        private HttpHost proxy = null;

        private String charset;

        public Builder() {
            headers = new HashMap<>();
            params = new LinkedHashMap<>();
            charset = "UTF-8";
            connTimeout = -1;
            readTimeout = -1;
        }

        public Builder method(String method) {
            this.method = HttpMethod.valueOf(method.toUpperCase());
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder header(String key, String val){
            headers.put(key, val);
            return this;
        }

        public Builder parameter(String key, String val){
            params.put(key, val);
            this.entity = null;
            return this;
        }

        public Builder parameter(Map<String, String> map) {
            params.putAll(map);
            this.entity = null;
            return this;
        }

        public Builder connTimeout(int timeout) {
            this.connTimeout = timeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder proxy(String host, int port) {
            this.proxy = new HttpHost(host, port);
            return this;
        }

        public Builder proxy(HttpHost httpHost) {
            this.proxy = httpHost;
            return this;
        }

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder chartUTF8() {
            this.charset = "UTF-8";
            return this;
        }

        public Builder chartGBK() {
            this.charset = "GBK";
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            this.params.clear();
            return this;
        }

        public Builder entity(HttpEntity entity) {
            this.entity = entity;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }



        public Request build() {
            if(null != body) {
                this.entity = new StringEntity(body, charset);
                params.clear();
            }
            return new Request(url, method, headers, params, entity, proxy, charset, connTimeout, readTimeout);
//            Map<String, String> h = new HashMap<>();
//            Map<String, String> p = new HashMap<>();
//            h.putAll(headers);
//            p.putAll(params);
//
//
//            return new Request(url, HttpMethod.valueOf(method.name()), h, p, )
        }



    }



}
