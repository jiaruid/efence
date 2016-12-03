package com.langf.efence.http;

import java.util.Map;


public class RequestVO<T> {
	/**
	 * 请求地址
	 */
	public String url;
	/**
	 * 请求参数 post专用
	 */
	public Map<String, Object> params;
	/**
	 * 请求数据回调
	 */
	public OnResultListener<T> listener;
	/**
	 * clazz
	 */
	public Class<T> clazz;
	
	public LoadMode mode;

	public boolean isForceCache = false;
}
