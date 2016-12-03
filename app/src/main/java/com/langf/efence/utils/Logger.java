package com.langf.efence.utils;

import android.util.Log;

/**
 * dujr
 * 日志打印工具类
 */
public class Logger {
	private final static boolean logFlag = true;

	private final static int logLevel = Log.VERBOSE;

	/**
	 * Get The Current Function Name
	 * 
	 * @return
	 */
	private static String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(Logger.class.getName())) {
				continue;
			}
			return "[ " + Thread.currentThread().getName() + ": "
					+ st.getFileName() + ":" + st.getLineNumber() + " "
					+ st.getMethodName() + " ]";
		}
		return null;
	}

	private static String getTag() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		String tag = "StarTimes";
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(Logger.class.getName())) {
				continue;
			}
			tag = st.getClassName().substring(st.getClassName().lastIndexOf(".")+1);
			break;
		}
		return tag;
	}

	/**
	 * The Log Level:i
	 * 
	 * @param str
	 */
	public static void i(String str) {
		if (logFlag && logLevel <= Log.INFO) {
			Log.i(getTag(), getFunctionName() + " - " + str);
		}
	}

	/**
	 * The Log Level:d
	 * 
	 * @param str
	 */
	public static void d(String str) {
		if (logFlag && logLevel <= Log.DEBUG) {
			Log.d(getTag(), getFunctionName() + " - " + str);
		}
	}

	/**
	 * The Log Level:V
	 * 
	 * @param str
	 */
	public static void v(String str) {
		if (logFlag && logLevel <= Log.VERBOSE) {
			Log.v(getTag(), getFunctionName() + " - " + str);
		}
	}

	/**
	 * The Log Level:w
	 * 
	 * @param str
	 */
	public static void w(String str) {
		if (logFlag && logLevel <= Log.WARN) {
			Log.w(getTag(), getFunctionName() + " - " + str);
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param str
	 */
	public static void e(String str) {
		if (logFlag && logLevel <= Log.ERROR) {
				Log.e(getTag(), getFunctionName() + " - " + str);
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param log
	 * @param tr
	 */
	public static void e(String log, Throwable tr) {
		if (logFlag && logLevel <= Log.ERROR) {
			Log.e(getTag(), getFunctionName() + " - " + log, tr);
		}
	}
}
