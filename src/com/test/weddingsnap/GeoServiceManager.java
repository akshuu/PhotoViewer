package com.test.weddingsnap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.places.Place;
import com.googlecode.flickrjandroid.places.PlacesList;
import com.test.weddingsnap.tasks.ImageDownloadTask;
import com.test.weddingsnap.tasks.PhotoSearchTask;
import com.test.weddingsnap.tasks.PlaceInfoTask;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

/**
 * Provides utlities for querying user's geo location using GPS/Network provider.
 * 
 * NOTE: This class should be instantiated only from a thread which has a Looper associated with it.
 *
 */
public class GeoServiceManager {

	private LocationManager mLocationManager = null;
	private Geocoder mGeoCoder = null;
	private Address mCurrentAddress = null;
	private Location mNotYetGeoDecodedLocation = null;
	private Activity mActivity;
	private Context mContext;
	
	public GeoServiceManager(Context context,Activity mAct) {
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mGeoCoder = new Geocoder(context);
		this.mContext = context;
		this.mActivity = mAct;
		
		try {
			// Register the listener with the Location Manager to receive location updates
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.MIN_TIME_FOR_GEO_UPDATES, 
													Constants.MIN_DISTANCE_FOR_GEO_UPDATES, mLocationListener);
		}
		catch(IllegalArgumentException exception) {
			Log.e(Constants.LOG_TAG, "GeoServiceManager: One of the parameters to requestLocationUpdates is wrong. Caught exception " + exception.getMessage());
		}
		catch(SecurityException exception) {
			Log.e(Constants.LOG_TAG, "GeoServiceManager: No suitable permission to check for the location service! Caught exception " + exception.getMessage());
		}
		catch(RuntimeException exception) {
			Log.e(Constants.LOG_TAG, "GeoServiceManager: Calling thread doesn't have a looper. Caught exception " + exception.getMessage());			
		}
	}

	/**
	 * Geo decode the current not yet decoded location
	 */
	private void geoDecodeLocation() {
		if (mNotYetGeoDecodedLocation != null) {
			try {
				List<Address> address = mGeoCoder.getFromLocation(mNotYetGeoDecodedLocation.getLatitude(), mNotYetGeoDecodedLocation.getLongitude(), 1);
				
				if (address!= null && address.size() > 0) {
					Log.v(Constants.LOG_TAG, "GeoServiceManager: Updated the current address...");
					mCurrentAddress = address.get(0);
				}
				
				//Successfully decoded, set the place holder to null
				mNotYetGeoDecodedLocation = null;
			}
			catch (IllegalArgumentException exception) {
				Log.e(Constants.LOG_TAG, "GeoServiceManager: Latitude/Longitude provided is invalid!");
				exception.printStackTrace();
			}
			catch (IOException exception) {
				Log.e(Constants.LOG_TAG, "GeoServiceManager: Network error or some other I/O error while geo coding the location!");
				exception.printStackTrace();
			}
		}		
	}
	
	
	public Address getCurrentAddress() {
		geoDecodeLocation();
		return mCurrentAddress;
	}
	
	public void updateGridView(){
		PlacesList placeList = null;
		if(mCurrentAddress!= null){
			PlaceInfoTask placeInfoTask = null;
			try {
				Log.i(Constants.LOG_TAG, "Current Latitute = " + mCurrentAddress.getLatitude() + " , longitude = " + mCurrentAddress.getLongitude());
//				place = Helper.getInstance().getFlickrPlaceId(mCurrentAddress);
				placeInfoTask = new PlaceInfoTask();
				placeInfoTask.execute(mCurrentAddress);
				placeList = placeInfoTask.get();
			} catch (Exception e) {
				e.printStackTrace();
				return ;
			}finally{
				if(placeInfoTask != null){
					placeInfoTask.cancel(true);
					placeInfoTask = null;
				}
			}
			
			
			Log.i(Constants.LOG_TAG, "cities with public images :" + placeList);
			
			if(placeList != null){
//				Log.i(Constants.LOG_TAG, "Current placeId  = " + placeList.getPlaceId() + " , WOE = " + placeList.getWoeId());
				Log.i(Constants.LOG_TAG, "Total list of cities with public images :" + placeList.size());
				for(int i=0;i<placeList.size();i++){
					Log.i(Constants.LOG_TAG,"City = " + ((Place)placeList.get(i)).getName());
				
					PhotoSearchTask publicPhotos = new PhotoSearchTask();
					publicPhotos.execute((Place)placeList.get(i));
					PhotoList photoList = null;
					try {
						photoList = publicPhotos.get();
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						if(publicPhotos != null){
							publicPhotos.cancel(true);
							publicPhotos = null;
						}
					}
					
					if(photoList != null){
//						((MainActivity)mActivity).updateGrid(photoList);
						
						Log.i(Constants.LOG_TAG, "Total list of photos with public images :" + photoList.size());
						List<String> failedURL = new LinkedList<String>();
						Map<Photo,Bitmap> photoMap = new HashMap<Photo, Bitmap>();
						
						for(Photo photo : photoList){
							Bitmap img = Helper.readFileFromDisk(photo,mContext,true);
							if(img == null){
								String url = photo.getThumbnailUrl();
								Log.i(Constants.LOG_TAG, "Thumbnail URL = " + url);
								ImageDownloadTask imageDownloadTask = new ImageDownloadTask();
								try {
									imageDownloadTask.execute(url);
									img = imageDownloadTask.get(20, TimeUnit.SECONDS);
									String filename = url.substring(url.lastIndexOf('/')+1);
									filename = Helper.getAbsoluteFileLocation(filename, mContext,true);
									Helper.saveImageToDisk(filename,img);
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (ExecutionException e) {
									e.printStackTrace();
								} catch (TimeoutException e) {
									Log.i(Constants.LOG_TAG,"Image download didn't complete in time");
									failedURL.add(url);			// Handle failed URLs
								}finally{
									imageDownloadTask.cancel(true);
									imageDownloadTask = null;
								}
							}
							photoMap.put(photo, img);
						}
//						((MainActivity)mActivity).updateGrid(photoList,thumbNailImgs);
						((MainActivity)mActivity).updateGrid(photoList,photoMap);
					}
				}
			}
		}
	}
	

	// Define a listener that responds to location updates
    LocationListener mLocationListener = new LocationListener() {

    	// Called when a new location is found by the network location provider.
    	public void onLocationChanged(Location location) {
    		Log.i(Constants.LOG_TAG, "GeoServiceManager: Received a location update!");
    		mNotYetGeoDecodedLocation = location;
    		geoDecodeLocation();
    		updateGridView();
    	}

    	public void onStatusChanged(String provider, int status, Bundle extras) {}

    	public void onProviderEnabled(String provider) {
    		Log.i(Constants.LOG_TAG, "GeoServiceManager: Provider had enabled location updates!");
    	}

    	public void onProviderDisabled(String provider) {
    		Log.i(Constants.LOG_TAG, "GeoServiceManager: Provider had disabled location updates!");
    	}
    };
}
