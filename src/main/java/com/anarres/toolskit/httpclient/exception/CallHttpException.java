package com.anarres.toolskit.httpclient.exception;

public class CallHttpException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    private int httpCode;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private String method;
    public CallHttpException(String msg, int httpCode, String method) {
        super(msg);
        this.httpCode = httpCode;
        this.method = method;
    }

}
