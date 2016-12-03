package com.langf.efence.http;





public class ResponseVO<T> {
	/**
	 * 请求地址
	 */
	public String url;
	
	/**
	 * 返回数据
	 */
	public T value;
	/**
	 * 请求数据回调
	 */
	public OnResultListener<T> listener;
	
	/**
	 * 请求网络状态码，loadway 网络情况下有效
	 */
	public int statusCode;
	
}
