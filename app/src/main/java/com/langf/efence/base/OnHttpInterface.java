package com.langf.efence.base;

import com.langf.efence.http.BitmapUploadParams;
import com.langf.efence.http.LoadMode;
import com.langf.efence.http.OnResultListener;

import java.util.List;
import java.util.Map;

public interface OnHttpInterface {

	/**
	 * get 请求
	 * @param clzz 请求对象class
	 * @param mode 请求方式 cache or net
	 * @param listener 请求后的回调
	 */
	<T> void doGet(String url, Class<T> clzz, LoadMode mode, OnResultListener<T> listener);

	/**
	 * get 请求
	 * @param clzz 请求对象class
	 * @param mode 请求方式 cache or net
	 * @param listener 请求后的回调
	 * @param isForceCache 是否要强制缓存
	 */
	<T> void doGet(String url, Class<T> clzz, LoadMode mode, boolean isForceCache, OnResultListener<T> listener);
	
	/**
	 * post 请求
	 * @param url
	 * @param clzz
	 * @param params
	 * @param listener
	 */
	<T> void doPost(String url, Class<T> clzz, Map<String, Object> params, OnResultListener<T> listener);
	
	/**
	 * delete 请求
	 * @param url
	 * @param clzz
	 * @param listener
	 */
	<T> void doDelete(String url, Class<T> clzz, OnResultListener<T> listener);

	/**
	 * 上传图片
	 * @param clzz
	 * @param bitmapParams
	 * @param listener
	 */
	<T> void doPostImage(Class<T> clzz, List<BitmapUploadParams> bitmapParams, OnResultListener<T> listener);
}
