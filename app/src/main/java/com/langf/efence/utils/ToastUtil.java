package com.langf.efence.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.langf.efence.R;

public class ToastUtil {

	private static final boolean isShowToast = false;
	private static long lastClickTime;
	// 防止连续点击按钮
	private synchronized static boolean isFastClick() {
		long time = System.currentTimeMillis();
		if (time - lastClickTime < 2000) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
	
	public static void centerShowToast(Context context, String message) {
		if(context == null)
			return;
		if(!isFastClick()){
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			TextView textview = new TextView(context);
			textview.setTextSize(18);
			textview.setText(message);
			textview.setGravity(Gravity.CENTER);
			textview.setTextColor(Color.WHITE);
			textview.setBackgroundResource(R.drawable.tips_bg_tost_black);
			toast.setView(textview);
			toast.show();
		}
	}

	public static void bottomShowToast(Context context, String message) {
		if(context == null)
			return;
		if(!isFastClick()){
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			TextView textview = new TextView(context);
			textview.setTextSize(18);
			textview.setText(message);
			textview.setGravity(Gravity.CENTER);
			textview.setTextColor(Color.WHITE);
			textview.setBackgroundResource(R.drawable.tips_bg_tost_black);
			toast.setView(textview);
			toast.show();
		}
	}

}
