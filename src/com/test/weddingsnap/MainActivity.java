package com.test.weddingsnap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.places.Place;
import com.googlecode.flickrjandroid.places.PlacesList;
import com.test.weddingsnap.tasks.ImageDownloadTask;
import com.test.weddingsnap.tasks.PhotoSearchTask;
import com.test.weddingsnap.tasks.PlaceInfoTask;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

public class MainActivity extends Activity {

	private Runnable getLocation;
	private GridView gridview;
	private Intent msgIntent;
	protected static final int MSG_LOCATION = 100;
	private ProgressDialog progress ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        gridview = (GridView) findViewById(R.id.gridview);
  
        ConnectivityManager comMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo mobileNetwork = comMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    	NetworkInfo wifiNetwork = comMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

      	if(!(mobileNetwork.isConnected() || wifiNetwork.isConnected())){
    		Toast.makeText(getApplicationContext(), getString(R.string.no_connection),Toast.LENGTH_SHORT).show();
    	}else{
	   
	        msgIntent = new Intent(this, LocationService.class);
	        Messenger messenger = new Messenger(uiHandler);
	        msgIntent.putExtra("MESSENGER", messenger);
	        startService(msgIntent);
	
	        if(savedInstanceState == null){
	           
	//        	pageTokens = new ArrayList<String>(SEARCH_PAGES);
	//        	pageNumber = 0;
	//        	searchResults = new HashMap<Integer, Results>();		// Cache results
	        	
	        }else{
	//        	pageTokens = savedInstanceState.getStringArrayList(KEY_PAGE_TOKEN);
	//        	pageNumber = savedInstanceState.getInt(KEY_PAGE_NUMBER);
	//        	searchResults = (HashMap<Integer, Results>) savedInstanceState.getSerializable(KEY_SEARCH_RESULT);
	        }
	        
	        progress = new ProgressDialog(this);
	        progress.setTitle(getString(R.string.please_wait));
	        progress.setMessage(getString(R.string.fetching_data));
	        
	        progress.show();
	        progress.setCancelable(true);		// Allow it to be cancelled incase it blocks due to network

	    }
        
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	if(getLocation!= null)
    		uiHandler.removeCallbacks(getLocation); 
    	super.onPause();
    }

    @Override
    protected void onDestroy() {
    	if(msgIntent != null)
    		stopService(msgIntent);
    	super.onDestroy();
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

    
    Handler uiHandler = new Handler(){
        public void handleMessage(final Message msg) {
    		switch(msg.what){
    		case MSG_LOCATION:
    			Log.v(Constants.LOG_TAG, "got the location from sevice..." + msg.getData());
    			final Bundle bundle = msg.getData();
    				
    			runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(bundle != null){
							Double coords[] = new Double[]{bundle.getDouble("latitude"),
							                             bundle.getDouble("longitude")
							};
							updateGridView(coords);
						}
						
					}
				});
    			break;
    			
    		default:
    				break;
    		}
        }
    };
    
	public void updateGridView(final Double coords[]){
		
		if(coords!= null){
			final PlaceInfoTask placeInfoTask = new PlaceInfoTask();
			Log.i(Constants.LOG_TAG, "Current Latitute = " + coords[0] + " , longitude = " + coords[1]);
			placeInfoTask.execute(coords);


	   		new Thread() { 
                public void run() { 
    		// Fetch the data
                	PlacesList placeList = null;
					try {
						
						placeList = placeInfoTask.get();
					} catch (Exception e) {
						e.printStackTrace();
						return ;
					}finally{
						if(placeInfoTask != null){
							placeInfoTask.cancel(true);
						}
					}
					if(placeList != null)
						getPhotosFromPlace(placeList);
					
					progress.dismiss();
                }
	 		}.start();
			
		}
	}

	
	/**
	 * 
	 */
	void getPhotosFromPlace(PlacesList placeList) {
		if(placeList != null){
//				Log.i(Constants.LOG_TAG, "Current placeId  = " + placeList.getPlaceId() + " , WOE = " + placeList.getWoeId());
			Log.i(Constants.LOG_TAG, "Total list of cities with public images :" + placeList.size());
			for(int i=0;i<placeList.size();i++){
				Log.d(Constants.LOG_TAG,"City = " + ((Place)placeList.get(i)).getName());
			
				final PhotoSearchTask publicPhotos = new PhotoSearchTask();
				publicPhotos.execute((Place)placeList.get(i));
				
				new Thread(){
					public void run() {

						PhotoList photoList = null;
						try {
							photoList = publicPhotos.get();
						} catch (Exception e) {
							e.printStackTrace();
						}finally{
							if(publicPhotos != null){
								publicPhotos.cancel(true);
							}
						}
						updatePhotos(photoList);
					};
				}.start();
				
				
			}
		}
	}
	
	
	private void updatePhotos(final PhotoList photoList){
		if(photoList != null){
		
		Log.i(Constants.LOG_TAG, "Total list of photos with public images :" + photoList.size());
		
		final Map<Photo,Bitmap> photoMap = new HashMap<Photo, Bitmap>();
		
		for(final Photo photo : photoList){
			Bitmap img = Helper.readFileFromDisk(photo,this,true);
			if(img == null){
				final String url = photo.getThumbnailUrl();
				Log.i(Constants.LOG_TAG, "Thumbnail URL = " + url);
				final ImageDownloadTask imageDownloadTask = new ImageDownloadTask();
				imageDownloadTask.execute(url);
					new Thread(){
						public void run() {
							List<String> failedURL = new LinkedList<String>();
							try {
					
								Bitmap img2 = imageDownloadTask.get(20, TimeUnit.SECONDS);
								String filename = url.substring(url.lastIndexOf('/')+1);
								filename = Helper.getAbsoluteFileLocation(filename, getApplicationContext(),true);
								Helper.saveImageToDisk(filename,img2);
								photoMap.put(photo, img2);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							} catch (TimeoutException e) {
								Log.i(Constants.LOG_TAG,"Image download didn't complete in time");
								failedURL.add(url);			// Handle failed URLs
							}finally{
								imageDownloadTask.cancel(true);
							}
							
							
						}
					}.start();
			}else{
				photoMap.put(photo,img);
			}
			
		}
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				updateGrid(photoList,photoMap);
				
			}
		});
	}
	}
	
	public void updateGrid(PhotoList photoList,Map<Photo,Bitmap> photoMap) {
		 final ImageAdapter adapter = new ImageAdapter(this,photoList,photoMap);
       	 gridview.setAdapter(adapter);
       	 gridview.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	 Photo photo = (Photo) adapter.getPhoto(position);
                 Intent imageViewIntent = new Intent(MainActivity.this,ImageViewActivity.class);
                 imageViewIntent.putExtra("photo", photo);
                 startActivity(imageViewIntent);
             }
         });		
	}
}
