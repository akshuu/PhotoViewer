/**
 * 
 */
package com.test.weddingsnap.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

/**
 * @author akshatj
 *
 */
public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = null;
		if(params != null){
			for(String url: params){
				try {
					Log.i(Constants.LOG_TAG,"Downloading file from " + url);
					bitmap = Helper.downloadFile(url);
				} catch (Exception e) {
					e.printStackTrace();
					bitmap = null;
				}
			}
		}
		return bitmap;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		// XXX : Cache it for optimizations
		if (this.isCancelled()) {
			result = null;
			return;
		}
	}

}
