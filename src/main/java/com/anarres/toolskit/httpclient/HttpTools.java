package com.anarres.toolskit.httpclient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpTools {

	private static final Logger logger = LoggerFactory.getLogger(HttpTools.class);
	
    public static String getQueryURI (URI uri, Map<String, String> params) {
    	String path = uri.getPath();
    	String query = uri.getQuery();
    	if(StringUtils.isEmpty(path)) {
    		path = "/";
    	}
    	if(StringUtils.isEmpty(query)) {
    		query = getQueryString(params);
    	} else {
    		query += "&" + getQueryString(params);
    	}
    	
    	return path + "?" + query;
    }
    
	/**
	 * 将map转成字符串
	 * @param params
	 * @return
	 */
	public static String getQueryString(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		String result = "";
		Iterator<Entry<String, String>> iter = params.entrySet().iterator();
		Entry<String, String> entry = iter.next();

		result += entry.getKey() + "=" + entry.getValue();// 添加第一个参数
		if (params.size() == 1) {
			return result;
		}

		while (iter.hasNext()) {
			entry = iter.next();
			result += "&" + entry.getKey() + "=" + entry.getValue();// 添加第一个参数
		}
		return result;

	}

	public static String entity2str(HttpEntity httpEntity, int frameLen, String charset) throws IOException {
		InputStream input = httpEntity.getContent();
		byte[] buff = new byte[frameLen];
		int len;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			while ((len = input.read(buff)) != -1) {

				out.write(buff, 0, len);
//				if (len < frameLen) {
//					break;
//				}
			}
		} catch (EOFException e) {
			logger.warn("Apache HttpEntity读取时意外的打到了流结尾。", e);
		}

		return new String(out.toByteArray(), charset);

	}

	public static String entity2strAndClose(HttpEntity httpEntity, int frameLen, String charset) throws IOException {
		String response = entity2str(httpEntity, frameLen, charset);
		httpEntity.getContent().close();
		return response;
	}

	public static void closeQuietly(CloseableHttpResponse response) {
		try {
			if(null != response) {
				response.close();
			}
		} catch (Exception e) {
			logger.error("无法关闭response", e);
		}
	}
}
