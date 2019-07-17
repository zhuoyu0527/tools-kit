package com.anarres.toolskit.httpclient.sync;


import com.anarres.toolskit.httpclient.HttpTools;
import com.anarres.toolskit.httpclient.Response;
import com.anarres.toolskit.httpclient.exception.CallHttpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

//import org.apache.http.annotation.ThreadSafe;

/**
 * 一个简易,线程安全的httpclient。 只要职责是提供http客户端配置。如 http 和 ssl 构建，并提供一些简单的通用方法。具体实现一来apache httpclient。
 *
 * 该类是线程安全的，允许多线程访问单例。
 * 示例：
 * <code>
 *      SimpleHttpClient client = SimpleHttpClient.http().build();
 *      Map<String, String> headers = new HashMap();
 *      headers.put(key1, val1);
 *      headers.put(key2, val2);
 *      Map<String, String> params = new HashMap();
 *      pamras.put(key1, val1);
 *      pamras.put(key2, val2);
 *
 *      String response = client.doGet(url, params, headers, "UTF-8", null);
 *
 * </code>
 * 以上代码可能过于繁琐。参数含义不够清晰，而且重载方法太多，不容易选择。可以考虑使用HttpRequest
 * <code>
 *      HttpClient client = HttpClient.http().build();
 *      String response = HttpRequest.newBuilder().url(url)
 *          .header(headkey1, headval1)
 *          .header(headkey2, headval2)
 *          .paramter(pk1, pv1)
 *          .paramter(pk2, pv2)
 *          .method("GET")
 *          .charsetUTF()
 *          .build()
 *          .execute(client);
 *
 * </code>
 *
 * Created by ath on 2016/8/18.
 */
//@ThreadSafe
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class HttpClient {

    public HttpClient(String defaultCharset, CloseableHttpClient httpClient, RequestConfig defaultConfig) {
        this.charset = defaultCharset;
        this.httpClient = httpClient;
        this.defaultConfig = defaultConfig;

    }

    private final String charset;
    private final CloseableHttpClient httpClient;
    private final RequestConfig defaultConfig;

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    public String doGet(String url) throws CallHttpException, IOException {
        return doGet(url, null);
    }

    public String doPost(String url) throws CallHttpException, IOException {
        return doPost(url, null);
    }

    public String doGet(String url, Map<String, String> params) throws CallHttpException, IOException {
        return doGet(url, params, charset);
    }

    public String doPost(String url, Map<String, String> params) throws CallHttpException, IOException {
        return doPost(url, params, charset);
    }

    public String doPost(String url, Map<String, String> params, String charset)  throws CallHttpException, IOException {
        return doPost(url, params, null, charset, null).asString();
    }

    public String doGet(String url, Map<String, String> params, String charset) throws CallHttpException, IOException {
        return doGet(url, params, null,charset, null, -1, -1).asString() ;
    }



    /**
     * 执行http请求
     * @param request httrequest对象
     * @param requestData request数据，例如xml，json等等
     * @param heads 头信息
     * @param charset 编码
     * @param proxy 代理
     * @return
     * @throws IOException
     * @throws CallHttpException
     */
    public Response exec(HttpRequestBase request, String requestData, Map<String, String> heads, String charset, HttpHost proxy, int connTimeout, int readTimeout) throws IOException, CallHttpException {
        return exec(request, new StringEntity(requestData, charset), heads, charset, proxy, connTimeout, readTimeout);
    }

    /**
     * 执行请求
     * @param request 请求对象
     * @param params 参数键值
     * @param heads 头信息
     * @param charset 编码
     * @param proxy 代理
     * @return
     * @throws IOException
     * @throws CallHttpException
     */
    public Response exec(HttpRequestBase request, Map<String, String> params, Map<String, String> heads, String charset, HttpHost proxy, int connTimeout, int readTimeout) throws IOException, CallHttpException {

        UrlEncodedFormEntity requestEntity = null;
        if (params != null && !params.isEmpty()) {
            ArrayList<NameValuePair> pairs = new ArrayList(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                value = (value == null ? "": value);
                pairs.add(new BasicNameValuePair(entry.getKey(), value));

            }
            requestEntity = new UrlEncodedFormEntity(pairs, charset);

        }
        return exec(request, requestEntity, heads, charset, proxy,connTimeout, readTimeout);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址 不能为空
     * @param params  请求的参数 允许为空
     * @param charset 编码格式  允许为空，若为空取httpclient配置中的默认字符编码
     * @param proxy 代理地址，允许为空，若为空，则不适用代理
     * @return 页面内容
     */
    public Response doGet(String url, Map<String, String> params, Map<String, String> heads, String charset, HttpHost proxy, int connTimeout, int readTimeout) throws CallHttpException, IOException {

        if (StringUtils.isBlank(url)) {
            return null;
        }

        HttpGet httpGet = new HttpGet(url);


        return exec(httpGet, params, heads, charset, proxy, connTimeout, readTimeout);

    }


    /**
     * HTTP post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址 不能为空
     * @param params  请求的参数 允许为空
     * @param charset 编码格式  允许为空，若为空取httpclient配置中的默认字符编码
     * @param proxy 代理地址，允许为空，若为空，则不适用代理
     * @return 页面内容
     */
    public Response doPost(String url, Map<String, String> params, Map<String, String> heads, String charset, HttpHost proxy)  throws CallHttpException, IOException {

        if (StringUtils.isBlank(url)) {
            return null;
        }

        return exec(new HttpPost(url), params, heads, charset, proxy, -1, -1);
    }



    /**
     * HTTP Post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param requestData  请求体字符串
     * @param charset 编码格式
     * @return 页面内容
     */
    public Response doPost(String url, String requestData, Map<String, String> heads, String charset, HttpHost proxy)  throws CallHttpException, IOException {

        if (StringUtils.isBlank(url)) {
            return null;
        }
        return exec(new HttpPost(url) , requestData, heads, charset, proxy, -1, -1);
    }


    /**
     * HTTP put 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param requestData  请求体字符串
     * @param charset 编码格式
     * @return 页面内容
     */
    public Response doPut(String url, String requestData, Map<String,String> heads, String charset, HttpHost proxy)  throws CallHttpException, IOException {

        if (StringUtils.isBlank(url)) {
            return null;
        }
        return exec(new HttpPut(url), requestData, heads, charset, proxy, -1, -1);
    }

    /**
     * HTTP put 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param parmas  请求体
     * @param charset 编码格式
     * @return 页面内容
     */
    public Response doPut(String url, Map<String, String> parmas, Map<String,String> heads, String charset, HttpHost proxy)  throws CallHttpException, IOException {

        if (StringUtils.isBlank(url)) {
            return null;
        }
        return exec(new HttpPut(url), parmas, heads, charset, proxy, -1, -1);
    }


    /**
     * 执行请求
     * @param request 请求对象
     * @param requestEntity 请求数据体
     * @param heads 头信息
     * @param charset 编码
     * @param proxy 代理
     * @return
     * @throws IOException
     * @throws CallHttpException
     */
    public Response exec(HttpRequestBase request, HttpEntity requestEntity, Map<String, String> heads, String charset, HttpHost proxy, int connTimeout, int readTimeout) throws IOException, CallHttpException {
//        logger.info("URI:" + request.getURI());
        RequestConfig.Builder cfgBuilder = RequestConfig.copy(defaultConfig);
        if(null != proxy) {
            cfgBuilder.setProxy(proxy);
        }

        if(-1 != connTimeout){
            cfgBuilder.setConnectTimeout(connTimeout);
        }

        if(-1 != readTimeout){
            cfgBuilder.setSocketTimeout(readTimeout);
        }
        request.setConfig(cfgBuilder.build());

        if(heads != null) {
            for(String key : heads.keySet()) {
                request.addHeader(key, heads.get(key));
            }
        }
        if(null == charset || charset.isEmpty()) {
            charset = this.charset;
        }

        if (null != requestEntity) {
            //如果是将参数写入entity的
            if(request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest)request).setEntity(requestEntity);
            } else {
                request.setURI(URI.create(request.getURI().toString() + "?" + EntityUtils.toString(requestEntity)));
            }
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);

            return new Response(request, response, charset);
        } catch (IOException e) {
            logger.error("http 请求失败", e);
            HttpTools.closeQuietly(response);
            throw e;
        }
    }
}
