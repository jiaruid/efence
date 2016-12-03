package com.langf.efence.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;

import java.util.List;
import java.util.Map;

public abstract class SharedPreferences {
	private android.content.SharedPreferences mKV;
	private Editor mEditor;

	/**
	 * 构造方法。
	 * 
	 * @param context
	 */
	public SharedPreferences(Context context) {
		String kvName = getSharedName()==null?"startimes":getSharedName();
		int mode = getSharedMode()==0? Context.MODE_PRIVATE:getSharedMode();
		mKV = context.getSharedPreferences(kvName, mode);
		mEditor = mKV.edit();
	}

	/**
	 *
	 * @return 键值表名称。
	 */
	public abstract String getSharedName();

	/**
	 *  打开的模式。值为Context.MODE_APPEND, Context.MODE_PRIVATE,
	 *            Context.WORLD_READABLE, Context.WORLD_WRITEABLE.
	 * @return
	 */
	public abstract int getSharedMode();

	/**
	 * 获取保存着的boolean对象。
	 * 
	 * @param key
	 *            键名
	 * @param defValue
	 *            当不存在时返回的默认值。
	 * @return 返回获取到的值，当不存在时返回默认值。
	 */
	public boolean getBoolean(String key, boolean defValue) {
		return mKV.getBoolean(key, defValue);
	}

	/**
	 * 获取保存着的int对象。
	 * 
	 * @param key
	 *            键名
	 * @param defValue
	 *            当不存在时返回的默认值。
	 * @return 返回获取到的值，当不存在时返回默认值。
	 */
	public int getInt(String key, int defValue) {
		return mKV.getInt(key, defValue);
	}

	/**
	 * 获取保存着的long对象。
	 * 
	 * @param key
	 *            键名
	 * @param defValue
	 *            当不存在时返回的默认值。
	 * @return 返回获取到的值，当不存在时返回默认值。
	 */
	public long getLong(String key, long defValue) {
		return mKV.getLong(key, defValue);
	}

	/**
	 * 获取保存着的float对象。
	 * 
	 * @param key
	 *            键名
	 * @param defValue
	 *            当不存在时返回的默认值。
	 * @return 返回获取到的值，当不存在时返回默认值。
	 */
	public float getFloat(String key, float defValue) {
		return mKV.getFloat(key, defValue);
	}

	/**
	 * 获取保存着的String对象。
	 * 
	 * @param key
	 *            键名
	 * @param defValue
	 *            当不存在时返回的默认值。
	 * @return 返回获取到的值，当不存在时返回默认值。
	 */
	public String getString(String key, String defValue) {
		return mKV.getString(key, defValue);
	}
	
	public <T> List<T> getList(String key, Class<T> clzz) {
		String json = mKV.getString(key, null);
		if(json != null){
			try {
				return JSONUtil.parserJsonToList(clzz, json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取所有键值对。
	 * 
	 * @return 获取到的所胡键值对。
	 */
	public Map<String, ?> getAll() {
		return mKV.getAll();
	}

	/**
	 * 设置一个键值对，它将在{@linkplain #commit()}被调用时保存。<br/>
	 * 注意：当保存的value不是boolean, byte(会被转换成int保存),int, long, float,
	 * String等类型时将调用它的toString()方法进行值的保存。
	 * 
	 * @param key
	 *            键名称。
	 * @param value
	 *            值。
	 * @return 引用的KV对象。
	 */
	public SharedPreferences put(String key, Object value) {
		if (value == null){
			mEditor.putString(key, null);
		}else if (value instanceof Boolean) {
			mEditor.putBoolean(key, (Boolean) value);
		} else if (value instanceof Integer || value instanceof Byte) {
			mEditor.putInt(key, (Integer) value);
		} else if (value instanceof Long) {
			mEditor.putLong(key, (Long) value);
		} else if (value instanceof Float) {
			mEditor.putFloat(key, (Float) value);
		} else if (value instanceof String) {
			mEditor.putString(key, (String) value);
		} else if (value instanceof List) {
			mEditor.putString(key, JSONUtil.getJSON(value));
		} else {
			mEditor.putString(key, value.toString());
		}
		apply();
		return this;
	}
	
	/**
	 * 移除键值对。
	 * 
	 * @param key
	 *            要移除的键名称。
	 * @return 引用的KV对象。
	 */
	public SharedPreferences remove(String key) {
		mEditor.remove(key).commit();
		return this;
	}

	/**
	 * 清除所有键值对。
	 * 
	 * @return 引用的KV对象。
	 */
	public SharedPreferences clear() {
		mEditor.clear().commit();
		return this;
	}

	/**
	 * 是否包含某个键。
	 * 
	 * @param key
	 *            查询的键名称。
	 * @return 当且仅当包含该键时返回true, 否则返回false.
	 */
	public boolean contains(String key) {
		return mKV.contains(key);
	}

	/**
	 * 返回是否提交成功。
	 * 
	 * @return 当且仅当提交成功时返回true, 否则返回false.
	 */
	public boolean commit() {
		return mEditor.commit();
	}

	/**
	 * 异步提交，没有返回值
	 */
	public void apply(){
		mEditor.apply();
	}
}
