package com.langf.efence.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.langf.efence.http.AsyncTaskHolder;
import com.langf.efence.http.BitmapUploadParams;
import com.langf.efence.http.LoadMode;
import com.langf.efence.http.OnListResultListener;
import com.langf.efence.http.OnResultListener;
import com.langf.efence.service.UrlService;
import com.langf.efence.utils.InternalStorage;
import com.langf.efence.utils.JSONUtil;
import com.langf.efence.utils.Logger;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author dujr
 *
 */
public abstract class AbstractService implements OnHttpInterface {
	protected Context context;
	protected Handler mMainHander;
	protected UrlService urlSerivce;

	public AbstractService(Context context) {
		super();
		this.context = context.getApplicationContext();
		mMainHander = new Handler(Looper.getMainLooper());
		urlSerivce = UrlService.getInstance();
	}

	@Override
	public <T> void doGet(final String url, Class<T> clzz, LoadMode mode, final OnResultListener<T> listener) {
		AsyncTaskHolder.getInstance(context).sendGet(url, clzz, getProxyListener(url, listener), mode, false);
	}

	@Override
	public <T> void doGet(final String url, Class<T> clzz, LoadMode mode, boolean isForceCache, final OnResultListener<T> listener) {
		AsyncTaskHolder.getInstance(context).sendGet(url, clzz, getProxyListener(url, listener), mode, isForceCache);
	}
	
	@Override
	public <T> void doPost(final String url, final Class<T> clzz, Map<String, Object> params, final OnResultListener<T> listener) {
		if(url.startsWith(urlSerivce.getUrlPrefix())){
			if(params == null){
				params = new HashMap<String, Object>();
			}
			params.put("accessToken", urlSerivce.getAccessToken());
			AsyncTaskHolder.getInstance(context).sendPost(url, params, String.class, new OnResultListener<String>() {
				@Override
				public boolean onIntercept() {
					return false;
				}

				@Override
				public void onSuccess(String value, int loadType) {
					try {
						JSONObject jsonObj = new JSONObject(value);
						switch (jsonObj.getString("code")){
							case "200":
								String dataJs = jsonObj.getString("data");
								//操作成功
								if(listener instanceof OnListResultListener){
									((OnListResultListener) listener).onSuccess(JSONUtil.parserJsonToList(clzz, dataJs), loadType);
								} else {
									listener.onSuccess(JSONUtil.getFromJSON(dataJs, clzz), loadType);
								}
								break;
							default:
								listener.onFailure(jsonObj.getString("code"), jsonObj.getString("msg"));
								break;
						}
					} catch (Exception e){
						Logger.e("parser ezjson error!", e);
						listener.onFailure("-2", "parser ezjson error");
					}
				}

				@Override
				public void onFailure(String errorCode, String msg) {
					try {
						if(isLogonValid(errorCode, url))
							listener.onFailure(errorCode, msg);
					}catch (Exception e){
						Logger.e("data callback error!", e);
					}
				}
			});
		}
	}
	
	@Override
	public <T> void doDelete(final String url, Class<T> clzz, final OnResultListener<T> listener) {
		AsyncTaskHolder.getInstance(context).sendDelete(url, clzz, getProxyListener(url, listener));
	}
	
	@Override
	public <T> void doPostImage(Class<T> clzz, List<BitmapUploadParams> bitmapParams, OnResultListener<T> listener) {
		AsyncTaskHolder.getInstance(context).postImage(bitmapParams, clzz, getProxyListener(bitmapParams.get(0).url, listener));
	}

	private <T> OnResultListener<T> getProxyListener(final String url, final OnResultListener<T> listener) {
		if(listener == null)
			return listener;
		OnResultListener<T> proxyListener;
		if(listener instanceof OnListResultListener){
			proxyListener = new OnListResultListener<T>() {

				@Override
				public boolean onIntercept() {
					//需要登录直接跳转，不会再执行后续操作
					if(needLogin(url)){
						return true;
					}
					return listener.onIntercept();
				}

				@Override
				public void onFailure(String errorCode, String msg) {
					if(isLogonValid(errorCode, url))
						listener.onFailure(errorCode, msg);
				}

				@Override
				public void onSuccess(List<T> value, int loadType) {
					try {
						((OnListResultListener<T>) listener).onSuccess(value, loadType);
					}catch (Exception e){
						Logger.e("data list callback error!", e);
					}
				}
			};
		} else {
			proxyListener = new OnResultListener<T>() {

				@Override
				public boolean onIntercept() {
					//需要登录直接跳转，不会再执行后续操作
					if(needLogin(url)){
						return true;
					}
					return listener.onIntercept();
				}

				@Override
				public void onSuccess(T value, int loadType) {
					listener.onSuccess(value, loadType);
				}

				@Override
				public void onFailure(String errorCode, String msg) {
					try {
						if(isLogonValid(errorCode, url))
							listener.onFailure(errorCode, msg);
					}catch (Exception e){
						Logger.e("data callback error!", e);
					}
				}
			};
		}
		return proxyListener;
	}
	
	/**
	 * 判断是否需要登录
	 * @param url
	 * @return
	 */
	private boolean needLogin(String url){
		return false;
	}


	/**
	 * 
	 * @param url 
	 * @param url 方便调试
	 * @return
	 */
	private boolean isLogonValid(String statusCode, String url) {
		return true;
	}

	/**
	 * 删除本地json 文件
	 * @param cachedFileName
	 * @return
	 */
	public boolean delCachedJSON(String cachedFileName) {
		return InternalStorage.getStorage(context).delJsonFile(cachedFileName);
	}
	
	public void saveCachedJSON(String cachedFileName, String json){
		try {
			InternalStorage.getStorage(context).save(cachedFileName, json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getCachedJSON(String cachedFileName){
		try {
			return InternalStorage.getStorage(context).get(cachedFileName);
		} catch (IOException e) {
		}
		return null;
	}
}
