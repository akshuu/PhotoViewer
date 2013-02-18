package com.test.weddingsnap.tasks;

import java.util.Set;
import java.util.TreeSet;

import android.os.AsyncTask;

import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.googlecode.flickrjandroid.places.Place;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

public class PhotoSearchTask extends AsyncTask<Place, Integer, PhotoList> {
 
	@Override
	protected void onPostExecute(PhotoList result) {
		if (this.isCancelled()) {
			result = null;
			return;
		}
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
	
	@Override
	protected PhotoList doInBackground(Place... params) {
		if(params == null)
			return null;
		
		SearchParameters searchParams = new SearchParameters();
		searchParams.setAccuracy(Constants.ACCURACY);
		searchParams.setPlaceId(params[0].getPlaceId());
		searchParams.setWoeId(params[0].getWoeId());
		Set<String> extras = new TreeSet<String>();
		extras.add("url_t");		// Fetching the thumbnail URL
		searchParams.setExtras(extras);
		
		PhotoList photoLists = null;
		try {
			photoLists = Helper.getInstance().getPhotosInterface().search(searchParams, Constants.IMAGES_PER_PAGE, 0);
		} catch (FlickrException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return photoLists;
	}

}
