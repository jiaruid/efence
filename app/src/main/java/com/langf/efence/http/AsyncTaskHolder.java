package com.langf.efence.http;

import android.content.Context;

import com.langf.efence.utils.InternalStorage;
import com.langf.efence.utils.JSONUtil;
import com.langf.efence.utils.Logger;
import com.langf.efence.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author dujr
 *
 */
@SuppressWarnings("unchecked")
public class AsyncTaskHolder {

	private static AsyncTaskHolder instance;
	private Context context;
	private ExecutorService service;

	private AsyncTaskHolder(){
		int num = Runtime.getRuntime().availableProcessors();
		this.service = Executors.newFixedThreadPool(num * 2 + 1);
	};

	/**
	 * @param context 需要传入applicationContext,否则容易造成内存泄漏
	 * @return
     */
	public static AsyncTaskHolder getInstance(Context context){
		if(instance == null)
			synchronized (AsyncTaskHolder.class) {
				if(instance == null)
					instance = new AsyncTaskHolder();
			}
		instance.context = context;
		return instance;
	}

	/**
	 * 发送get请求， 不带提示框
	 *
	 * @param url 地址
	 * @param listener 回调监听器
	 * @param clzz 请求数据模型类对象
	 * @param mode 数据请求方式
	 */
	public synchronized <T> void sendGet(String url, Class<T> clzz, OnResultListener<T> listener, LoadMode mode, boolean isForceCache) {
		if (listener != null && listener.onIntercept()) {
			return;
		}
		if (!LoadMode.NET.equals(mode)) {
			loadLocal(url, clzz, listener, mode, isForceCache);
		} else {
			loadNet(url, clzz, listener, mode, isForceCache);
		}
	}

	private <T> void loadNet(String url, Class<T> clzz, OnResultListener<T> listener, LoadMode mode, boolean isForceCache){
		if(!checkNetStatus(listener, context)){
			return;
		}
		RequestVO<T> params = new RequestVO<T>();
		params.url = url;
		params.clazz = clzz;
		params.listener = listener;
		params.mode = mode;
		params.isForceCache = isForceCache;
		if(!isForceCache&&LoadMode.CACHE_NET.equals(mode)){
			params.isForceCache = true;
		}
		OkHttpClient.getInstance(context).sendGet(params);
	}

	private <T> void loadLocal(final String url, final Class<T> clazz, final OnResultListener<T> listener, final LoadMode mode, final boolean isForceCache) {
		new LoadingDataTask() {
			List<T> value;
			@Override
			public void onPreExecute() {
			}
			@Override
			public void doInBackground() {
				try {
					String json = InternalStorage.getStorage(context.getApplicationContext()).get(url);
					value = new ArrayList<T>();
					if (listener instanceof OnListResultListener) {
						value = JSONUtil.parserJsonToList(clazz, json);
					}  else if (listener instanceof OnResultListener) {
						value.add(JSONUtil.getFromJSON(json, clazz));
					}
				} catch (Exception e) {
					Logger.e("parser data from local error!", e);
				}
			}
			@Override
			public void onPostExecute() {
				if(value != null && value.size()>0){
					if (listener instanceof OnListResultListener) {
						((OnListResultListener<T>) listener).onSuccess(value, OnResultListener.LOAD_CACHE);
					}  else if (listener instanceof OnResultListener) {
						((OnResultListener<T>) listener).onSuccess(value.get(0), OnResultListener.LOAD_CACHE);
					}
				}
				if(LoadMode.CACHE.equals(mode)){
					return;
				}else if(LoadMode.IFCACHE_NOTNET.equals(mode)){
					if(value == null || value.size()==0){
						loadNet(url, clazz, listener, mode, isForceCache);
					}
				}else if(LoadMode.CACHE_NET.equals(mode)){
					loadNet(url, clazz, listener, mode, isForceCache);
				}
			}

			@Override
			public void onError(Exception e) {

			}
		}.execute();
	}

	/**
	 * 发送Post请求到服务器
	 *
	 * @param url
	 * @param params key=value
	 * @param listener
	 * @param clzz 请求数据模型类对象
	 */
	public synchronized <T> void sendPost(String url, Map<String, Object> params, Class<T> clzz, OnResultListener<T> listener) {
		if(listener!=null && listener.onIntercept()){
			return;
		}
		if(!checkNetStatus(listener, context)){
			return;
		}
		RequestVO<T> param = new RequestVO<T>();
		param.url = url;
		param.clazz = clzz;
		param.params = params;
		param.listener = listener;
		OkHttpClient.getInstance(context).sendPost(param);
	}

	/**
	 * 发送delete请求到服务器
	 *
	 * @param url
	 * @param listener
	 * @param clzz 请求数据模型类对象
	 */
	public synchronized <T> void sendDelete(String url, Class<T> clzz, OnResultListener<T> listener) {
		if(listener!=null && listener.onIntercept()){
			return;
		}
		if(!checkNetStatus(listener, context)){
			return;
		}
		RequestVO<T> param = new RequestVO<T>();
		param.url = url;
		param.clazz = clzz;
		param.listener = listener;
		OkHttpClient.getInstance(context).sendDelete(param);
	}

	/**
	 * 发送图片
	 * @param clzz
	 * @param listener
	 */
	public synchronized <T> void postImage(List<BitmapUploadParams> bitmapParams, Class<T> clzz, OnResultListener<T> listener) {
		if(listener!=null && listener.onIntercept()){
			return;
		}
		if(!checkNetStatus(listener, context)){
			return;
		}
		RequestVO<T> param = new RequestVO<T>();
		param.clazz = clzz;
		if(bitmapParams!=null&&bitmapParams.size()>0){
			Map<String, Object> ps = new HashMap<String, Object>();
			ps.put("bitmaps", bitmapParams);
			param.params = ps;
		}else{
			throw new IllegalArgumentException("bitmaps must be set.");
		}
		param.listener = listener;
		OkHttpClient.getInstance(context).sendImage(param);
	}

	private <T> boolean checkNetStatus(OnResultListener<T> listener, Context context) {
		boolean netStatus = NetworkUtil.isNetworkAvailable(context);
		if(!netStatus){
			if(listener!=null){
				listener.onFailure("-2", "network error!");
			}
		}
		return netStatus;
	}

	/**
	 * 清除所有的网络异步请求任务
	 */
	public void clearAsyncTask() {
		OkHttpClient.getInstance(context).clearCallQueue();
	}
}
