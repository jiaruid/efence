package com.langf.efence.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JSONUtil {

	private static Gson gson;
	
	private static Gson getGSON() {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Date.class, new DateTypeAdapter());
			gson = gsonBuilder.create();
		}
		return gson;
	}
	
	public static <T> T getFromJSON(String json, Class<T> clazz){
		if(clazz.equals("a".getClass())){
			return (T) json;
		}
		return getGSON().fromJson(json,clazz);
	}
	
	public static <T> T getFromJSON(String json, Type typeOfT){
		return getGSON().fromJson(json,typeOfT);
	}
	
	public static String getJSON(Object object){
		String str = getGSON().toJson(object);
		return str;
	}
	
	/**
	 * json数据解析     [{"":"","":""},{"":"","":""},{"":"","":""}]  单层嵌套模式
	 * 暂不支持[,,,,](数组中嵌套的不是Json对象而是普通对象)
	 * @param clazz
	 * @param json
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> parserJsonToList(Class<T> clazz,String json) throws Exception {
		if(clazz.equals("a".getClass())){
			return getFromJSON(json, new TypeToken<ArrayList<String>>() {}.getType());
		}
		JSONArray array = new JSONArray(json);
		List<T> list = new ArrayList<T>();
		int length = array.length();
		for(int i = 0 ; i < length ; i++){
			list.add(getFromJSON(array.getString(i), clazz));
		}
		return list;
	}
}
