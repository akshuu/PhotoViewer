/**
 * 
 */
package com.test.weddingsnap.tasks;

import android.location.Address;
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
public class PlaceInfoTask extends AsyncTask<Address, Void,PlacesList> {

	@Override
	protected PlacesList doInBackground(Address... params) {
		if(params == null)
			return null;
		
		PlacesList lists = null;
    	try {
    		// Making the accuracy to City, as it might fetch more results.
			lists = Helper.getInstance().getPlacesInterface().findByLatLon(params[0].getLatitude(), params[0].getLongitude(), Constants.ACCURACY);
		} catch (FlickrException e) {
			e.printStackTrace();
			Log.d(Constants.LOG_TAG,"Unable to get place information : Error Code = " + e.getErrorCode() + ", message = " + e.getMessage());
		} catch(Exception e){
			e.printStackTrace();
		}
    	
    	/*
    	Place place = null;
    	if(lists != null && lists.size() > 0){
    		place = (Place)lists.get(0);			// Since we are dealing with City level, we get only one place

	    	if(place != null){
		    	try{
		    		Log.d(Constants.LOG_TAG,"place information : Place Id = " + place.getPlaceId() + ", WOE = " + place.getWoeId());
		    		listPublicPhotos = Helper.getInstance().getPlacesInterface().getChildrenWithPhotosPublic("7.MJR8tTVrIO1EgB","2487956");//place.getPlaceId(), place.getWoeId());
		    	}catch(FlickrException feEx){
		    		feEx.printStackTrace();
		    		Log.d(Constants.LOG_TAG,"Unable to get public photos : Error Code = " + feEx.getErrorCode() + ", message = " + feEx.getMessage());
		    	}catch(Exception e){
					e.printStackTrace();
				}
	    	}
    	}*/
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
