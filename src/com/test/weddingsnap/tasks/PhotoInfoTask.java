/**
 * 
 */
package com.test.weddingsnap.tasks;

import java.util.Collection;

import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.flickrjandroid.photos.Exif;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

/**
 * @author akshatj
 *
 */
public class PhotoInfoTask extends AsyncTask<String, Void, Collection<Exif>> {

	@Override
	protected Collection<Exif> doInBackground(String... params) {
		if(params == null || params.length < 1)
			return null;
		
		String photo_id = params[0];		// Photo Id
		String secret = params[1];
		
		Collection<Exif> exif = null;
		
		try{
			exif = Helper.getInstance().getPhotosInterface().getExif(photo_id, secret);
			Log.i(Constants.LOG_TAG,"Got ExIF info for the photo :" + photo_id );
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return exif;
	}
	
	@Override
	protected void onPostExecute(Collection<Exif> result) {
		if (this.isCancelled()) {
			result = null;
			return;
		}
	}

}
