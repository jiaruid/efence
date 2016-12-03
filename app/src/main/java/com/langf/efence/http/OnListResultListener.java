package com.langf.efence.http;

import java.util.List;


/**
 * 网络数据处理
 * 
 * @author Administrator
 * 
 */
public abstract class OnListResultListener<T> implements OnResultListener<T>{

	public final void onSuccess(T value, int loadType) {};
	
	public abstract void onSuccess(List<T> value, int loadType);

}
