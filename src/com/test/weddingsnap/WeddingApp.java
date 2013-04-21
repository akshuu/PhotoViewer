package com.test.weddingsnap;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.test.weddingsnap.util.Constants;

public class WeddingApp extends Application {
	private LruCache<String, Bitmap> mMemoryCache;
	
	public WeddingApp() {
		final int memClass = (int) (Runtime.getRuntime().maxMemory() / 1024); //((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        final int cacheSize = memClass / 8;
        Log.i(Constants.LOG_TAG, "Creating WeddingApp instance...");
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
	}
	
	public LruCache<String, Bitmap> getMemoryCache(){
		return mMemoryCache;
	}
}
