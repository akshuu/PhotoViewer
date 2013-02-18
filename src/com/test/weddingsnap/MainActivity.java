package com.test.weddingsnap;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class MainActivity extends Activity {

	private Handler uiHandler;
	private Runnable getLocation;
	private GeoServiceManager mGeoServiceManager;
	private GridView gridview;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        gridview = (GridView) findViewById(R.id.gridview);
        
        // Start the location thread.
        uiHandler = new Handler(Looper.getMainLooper());
        getLocation  = new Runnable() {					
			@Override
			public void run() {
				mGeoServiceManager = new GeoServiceManager(getApplicationContext(),MainActivity.this);	
			}
		};
		uiHandler.post(getLocation);
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
    protected void onStop() {
    	super.onStop();
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
