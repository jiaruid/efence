package com.langf.efence.http;

public interface OnResultListener<T> {

	int LOAD_NET = 0;
	int LOAD_CACHE = 1;


	/**
	 * 是否拦截此次请求
	 * @return true 拦截不会再请求数据
	 */
	boolean onIntercept();
	/**
	 * 请求到正确结果会调用
	 * @param value
	 */
	void onSuccess(T value, int loadType);
	
	/**
	 * 状态码
	 * @param errorCode
	 * @param msg
	 */
	void onFailure(String errorCode, String msg);
	
}
