package com.test.weddingsnap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.places.Place;
import com.googlecode.flickrjandroid.places.PlacesList;
import com.test.weddingsnap.tasks.PhotoSearchTask;
import com.test.weddingsnap.tasks.PlaceInfoTask;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

public class MainActivity extends Activity implements AbsListView.OnScrollListener{

	private Runnable getLocation;
	private GridView gridview;
	private Intent msgIntent;
	protected static final int MSG_LOCATION = 100;
	
	private static final String KEY_PHOTOLISTS = "PhotoLists";
	private static final String KEY_PLACELISTS = "PlaceList";
	private static final String KEY_PAGENUMBER = "PageNumber";
	
	private ProgressDialog progress ;
	private PlacesList mPlaceList;
	private int pagenumber = 1;
	private PhotoList mPhotoList = null;
	private ImageAdapter adapter;
	private ExecutorService threadPool = null;
	private Double coords[];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setOnScrollListener(this);
        ConnectivityManager comMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo mobileNetwork = comMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    	NetworkInfo wifiNetwork = comMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

      	if(!(mobileNetwork.isConnected() || wifiNetwork.isConnected())){
    		Toast.makeText(getApplicationContext(), getString(R.string.no_connection),Toast.LENGTH_SHORT).show();
    	}else{
	   
	        progress = new ProgressDialog(this);
	        progress.setTitle(getString(R.string.please_wait));
	        progress.setMessage(getString(R.string.fetching_data));
	        
	        threadPool = Executors.newFixedThreadPool(10);
	        if(savedInstanceState == null){

	        	
	        	mPhotoList = new PhotoList();
	        	msgIntent = new Intent(this, LocationService.class);
	  	        Messenger messenger = new Messenger(uiHandler);
	  	        msgIntent.putExtra("MESSENGER", messenger);
	  	        //startService(msgIntent);
	  	        
	  	        // for Emulator Testing
	  	        Double emuCoords[] = { 12.9833d,-77.5833d};
	  	        updateGridView(emuCoords);
	  	        // end emulator testing
	  	        pagenumber = 1;
	  	        progress.show();
		        progress.setCancelable(true);		// Allow it to be cancelled incase it blocks due to network

	        }else{
	        	
	        	mPhotoList = (PhotoList) savedInstanceState.getSerializable(KEY_PHOTOLISTS);
	        	pagenumber = savedInstanceState.getInt(KEY_PAGENUMBER);
	        	mPlaceList = (PlacesList) savedInstanceState.getSerializable(KEY_PLACELISTS);
	        	updateGrid(mPhotoList);
	        }
	    }
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if(item.getItemId() == R.id.menu_refresh){
    		if(coords != null){
        		progress.show();
	        	mPhotoList = new PhotoList();
	        	pagenumber = 1;
    			updateGridView(coords);
    		}else{
    			// If we dont have the coordinates, we need to start the location search again
    			msgIntent = new Intent(this, LocationService.class);
	  	        Messenger messenger = new Messenger(uiHandler);
	  	        msgIntent.putExtra("MESSENGER", messenger);
	  	        startService(msgIntent);
        		progress.show();
    		}
    		return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
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
    	if(msgIntent != null){
    		stopService(msgIntent);
    		msgIntent = null;
    	}
    	threadPool.shutdown();
    	super.onDestroy();
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(Constants.LOG_TAG,"MainActivity: Saving the sate");
		outState.putSerializable(KEY_PHOTOLISTS, mPhotoList);
		outState.putSerializable(KEY_PAGENUMBER,pagenumber);
		outState.putSerializable(KEY_PLACELISTS, mPlaceList);
		super.onSaveInstanceState(outState);
	}
	
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mPhotoList = (PhotoList) savedInstanceState.getSerializable(KEY_PHOTOLISTS);;
		pagenumber = savedInstanceState.getInt(KEY_PAGENUMBER);
		mPlaceList = (PlacesList) savedInstanceState.getSerializable(KEY_PLACELISTS);
		super.onRestoreInstanceState(savedInstanceState);
	}

	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	boolean isLoading = false;
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		if(firstVisibleItem == 0){
			return;
		}
		
		if((gridview.getLastVisiblePosition() + 1 == totalItemCount)){	// if we are the last item
																		// on the grid view.
			progress.show();
			if(!isLoading){
				pagenumber++;
				getPhotosFromPlace(mPlaceList,true);
			}
		}else{
			isLoading = false;
			return;
		}
	}
	
    
    Handler uiHandler = new Handler(){
        public void handleMessage(final Message msg) {
    		switch(msg.what){
    		case MSG_LOCATION:
    			Log.v(Constants.LOG_TAG, "got the location from sevice..." + msg.getData());
    			final Bundle bundle = msg.getData();
				if(bundle != null){
					coords = new Double[]{
							bundle.getDouble("latitude"),
					        bundle.getDouble("longitude")
						};
					updateGridView(coords);
				}
    			break;
    			
    		default:
    				break;
    		}
        }
    };
    
    /**
     * Updates the grid with image thumbnails
     * @param coords
     */
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
					mPlaceList = placeList;
					if(placeList != null){
						getPhotosFromPlace(placeList,false);
					}
					
                }
	 		}.start();
			
		}
	}

	/**
	 * Gets photos for the nearby place
	 */
	void getPhotosFromPlace(PlacesList placeList,final boolean isUpdated) {
		if(placeList != null){
			isLoading = true;
			for(int i=0;i<placeList.size();i++){
			
				final PhotoSearchTask publicPhotos = new PhotoSearchTask();
				Place place = (Place)placeList.get(i);
				String[] params = {
						place.getPlaceId(),
						place.getWoeId(),
						pagenumber + ""
				};
				publicPhotos.execute(params);
				
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
						if(photoList != null){
							mPhotoList.addAll(photoList);
							updatePhotos(photoList,isUpdated);
						}else{
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									progress.dismiss();
									Toast.makeText(getApplicationContext(), "Unknown Error. Cannot fetch images", Toast.LENGTH_LONG).show();									
								}
							});
							
						}
					};
				}.start();
			}
		}else{
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					progress.dismiss();
					Toast.makeText(getApplicationContext(), "Unknown Error. Cannot fetch images", Toast.LENGTH_LONG).show();									
				}
			});
			
		}
	}
	
	
	private void updatePhotos(final PhotoList photoList, boolean isUpdated){
		if(photoList != null){
		
		Map<String,Future<Bitmap>> futureResult = new HashMap<String,Future<Bitmap>>();
		for(final Photo photo : photoList){
			// If we have a cached value, read it
			// else download it
			
			final String url = photo.getThumbnailUrl();
			String file = url.substring(url.lastIndexOf('/')+1);
			final String filename = Helper.getAbsoluteFileLocation(file, getApplicationContext(),true);

			// Queue the thumbnail download tasks to a threadpool
			if(!(new File(filename).exists())){
				futureResult.put(filename,threadPool.submit(new Callable<Bitmap>() {
					@Override
					public Bitmap call() throws Exception {
						Bitmap bitmap = Helper.downloadFile(url);
						return bitmap;
					}
				}));
			}
		}
		
		for(String filename : futureResult.keySet()){
			Future<Bitmap> results = futureResult.get(filename);
			Bitmap img;
			try {
				img = results.get(30, TimeUnit.SECONDS);
				Helper.saveImageToDisk(filename,img);
			} catch (Exception e) {
				Log.i(Constants.LOG_TAG,"Image download didn't complete in time");
				e.printStackTrace();
			}
		}
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				progress.dismiss();
				updateGrid(mPhotoList);
				isLoading = false;
			}
		});
	}
	}
	
	// Updates the grid view with the photo list.
	public void updateGrid(PhotoList photoList) {
       	
		
		if(adapter == null){
			adapter = new ImageAdapter(this,photoList);
			gridview.setAdapter(adapter);
		}else{
			// Subsequent times, just append to the list and notify dataset change
			adapter.setPhotoList(photoList);
			adapter.notifyDataSetChanged();
		}
   	 	
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
