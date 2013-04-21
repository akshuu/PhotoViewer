/**
 * 
 */
package com.test.weddingsnap.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.places.PlacesList;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

/**
 * @author akshatj
 *
 */
public class PlaceInfoTask extends AsyncTask<Double, Void,PlacesList> {

	@Override
	protected PlacesList doInBackground(Double... params) {
		if(params == null)
			return null;
		
		PlacesList lists = null;
    	try {
    		// Making the accuracy to City, as it might fetch more results.
			lists = Helper.getInstance().getPlacesInterface().findByLatLon(params[0], params[1], Constants.ACCURACY);
		} catch (FlickrException e) {
			e.printStackTrace();
			Log.d(Constants.LOG_TAG,"Unable to get place information : Error Code = " + e.getErrorCode() + ", message = " + e.getMessage());
		} catch(Exception e){
			e.printStackTrace();
		}
    	
    	return lists;
	}
	
	@Override
	protected void onPostExecute(PlacesList result) {
		if (this.isCancelled()) {
			result = null;
			return;
		}
	}
	
}
