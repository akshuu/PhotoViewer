/**
 * 
 */
package com.test.weddingsnap.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.test.weddingsnap.ImageViewActivity;
import com.test.weddingsnap.R;
import com.test.weddingsnap.WeddingApp;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

/**
 * @author akshatj
 *
 */
public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

	private LruCache<String, Bitmap> mMemoryCache;
	private ProgressDialog progress ;
	private Activity mActivity;
	
	public ImageDownloadTask(Activity activity) {
		mActivity = activity;
		mMemoryCache = ((WeddingApp)activity.getApplication()).getMemoryCache();
	}
	
	@Override
	protected void onPreExecute() {
		progress = new ProgressDialog(mActivity);
        progress.setTitle(mActivity.getString(R.string.please_wait));
        progress.setMessage(mActivity.getString(R.string.fetching_image));
        
        progress.show();
        progress.setCancelable(true);		// Allow it to be cancelled incase it blocks due to network
		super.onPreExecute();
	}
	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = null;
		if(params != null){
			String id = params[0];
			String url = params[1];
			try {
				Log.i(Constants.LOG_TAG,"Downloading file from " + url);
				bitmap = Helper.downloadFile(url);
				mMemoryCache.put(id, bitmap);			// Add it to the cache
			} catch (Exception e) {
				e.printStackTrace();
				bitmap = null;
			}
		}
		return bitmap;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		progress.dismiss();
		if (this.isCancelled()) {
			result = null;
			return;
		}
		
		((ImageViewActivity)mActivity).showImage(result);
	}

}
