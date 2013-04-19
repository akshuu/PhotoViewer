package com.test.weddingsnap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.test.weddingsnap.util.Constants;

public class LocationService extends Service {
	
    public LocationService() {
    }

    private LocationManager mLocationManager = null;
	private static final int MSG_LOCATION = 100;

    private class LocationListener implements android.location.LocationListener{
        Location mLastLocation;
        
		public LocationListener(String provider)
        {
            Log.v(Constants.LOG_TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location)
        {
            Log.v(Constants.LOG_TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            updateUserLocation(location);
        }
        
        
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.v(Constants.LOG_TAG, "onProviderDisabled: " + provider);            
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.v(Constants.LOG_TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.v(Constants.LOG_TAG, "onStatusChanged: " + provider);
        }
    } 
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
	private Intent mIntent;
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
    public void updateUserLocation(Location mAddress) {

    		Log.i(Constants.LOG_TAG, "got the location...");
    		
    		Log.i(Constants.LOG_TAG, "Latitude ..." + mAddress.getLatitude() + ", long= " + mAddress.getLongitude());
    		if(mIntent != null){
	    		Bundle extras = mIntent.getExtras();
	    	    if (extras != null) {
	    	      Messenger messenger = (Messenger) extras.get("MESSENGER");
	    	      Message msg = Message.obtain();
	    	      msg.what = MSG_LOCATION;
	    	      Bundle bundle = new Bundle();
	    	      bundle.putDouble("latitude", mAddress.getLatitude());
	    	      bundle.putDouble("longitude", mAddress.getLongitude());
	    	      msg.setData(bundle);
	    	      try {
	    	        messenger.send(msg);
	    	      } catch (android.os.RemoteException e1) {
	    	        Log.w(getClass().getName(), "Exception sending message", e1);
	    	      }
	    	    }
    		}
	}
    
	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
		Log.v(Constants.LOG_TAG, "onStartCommand: ");
        mIntent = intent;
        super.onStartCommand(intent, flags, startId);       
        return START_STICKY;
    }
    @Override
    public void onCreate()
    {
    	Log.v(Constants.LOG_TAG, "onCreate: ");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, Constants.MIN_TIME_FOR_GEO_UPDATES, Constants.MIN_DISTANCE_FOR_GEO_UPDATES,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(Constants.LOG_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(Constants.LOG_TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, Constants.MIN_TIME_FOR_GEO_UPDATES, Constants.MIN_DISTANCE_FOR_GEO_UPDATES,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(Constants.LOG_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(Constants.LOG_TAG, "gps provider does not exist " + ex.getMessage());
        }
    }
    @Override
    public void onDestroy()
    {
        Log.v(Constants.LOG_TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(Constants.LOG_TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    } 
}
