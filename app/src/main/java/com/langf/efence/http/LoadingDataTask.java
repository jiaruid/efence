package com.langf.efence.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.videogo.exception.BaseException;

public abstract class LoadingDataTask {

	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			if(msg.what==0){
				try{
					onPostExecute();
				}catch (Exception e) {
				}
			}
		};
	};
	public abstract void onPreExecute();

	public abstract void doInBackground() throws BaseException;

	public abstract void onPostExecute();

	public abstract void onError(Exception e);

	public void execute() {
		onPreExecute();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					doInBackground();
					handler.sendEmptyMessage(0);
				} catch (final Exception e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							onError(e);
						}
					});
				}
			}
		};
		ThreadPoolManager.getInstance().addTask(runnable);
	}
}
