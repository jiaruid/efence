package com.langf.efence.utils;

import android.os.Handler;

/**
 * 封装Handler
 * @author dujr
 *
 */
public abstract class PostTimer extends Handler {

	public abstract void execute();
	
	private Runnable innerRun = new Runnable() {
		
		@Override
		public void run() {
			execute();
		}
	};

	public void stop() {
		removeCallbacks(innerRun);
	}

	public void postDelayed(long time) {
		postDelayed(innerRun, time);
	}
	
	public void startDelayed(long time){
		stop();
		postDelayed(time);
	}
}
