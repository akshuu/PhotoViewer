package com.test.weddingsnap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.googlecode.flickrjandroid.photos.Exif;
import com.googlecode.flickrjandroid.photos.Photo;
import com.test.weddingsnap.tasks.ImageDownloadTask;
import com.test.weddingsnap.tasks.PhotoInfoTask;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

public class ImageViewActivity extends Activity implements OnClickListener {

	private Button btnDownload, btnInfo;
	private ImageView imgView = null;
	private Photo mPhoto = null;
	private Collection<Exif> exifInfo = null;
	private Bitmap mImage ; 
	private ImageDownloadTask imageDownloadTask;
	private PhotoInfoTask photoInfoTask ;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        btnDownload = (Button)findViewById(R.id.btnDownload);
        btnInfo  = (Button)findViewById(R.id.btnInfo);
        btnDownload.setBackgroundResource(R.drawable.download);
        btnInfo.setBackgroundResource(R.drawable.info);
        btnDownload.setOnClickListener(this);
        btnInfo.setOnClickListener(this);
        
        // Get the Cache from application class
        WeddingApp app = (WeddingApp)getApplication();
        LruCache<String, Bitmap> mMemoryCache;
        mMemoryCache = app.getMemoryCache();
        
        imgView = (ImageView)findViewById(R.id.imageView);
        ((RelativeLayout)imgView.getParent()).setBackgroundColor(Color.BLACK);
        Bundle photoBundle = getIntent().getExtras();
        mPhoto = (Photo) photoBundle.get("photo");
        
        // Read the image from cache.
        mImage = mMemoryCache.get(mPhoto.getId());
        if(savedInstanceState == null){
	        if(mImage == null){
		        imageDownloadTask = new ImageDownloadTask(this);
		        Log.d(Constants.LOG_TAG, "Downloading the image : " + mPhoto.getMediumUrl());
		        String params[] = {mPhoto.getId(), mPhoto.getMediumUrl()};
		        imageDownloadTask.execute(params);
		       
		    }else{
		        Log.d(Constants.LOG_TAG, "Updating the imageview from cache ...  " + mImage);
		        showImage(mImage);
		    }
        }
        else{
        	Log.d(Constants.LOG_TAG, "Updating the imageview from cache...maybe on rotation " + mImage);
        	showImage(mImage);
	    }
    }

    /**
	 * Render the image on the image view
	 */
	public void showImage(Bitmap bitmap) {
		if(bitmap != null){
			mImage = bitmap;
			imgView.setImageBitmap(bitmap);
		}
		else
			Toast.makeText(getApplicationContext(), getString(R.string.no_image),Toast.LENGTH_SHORT).show();
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onStop() {
    	if(imageDownloadTask != null)
    		imageDownloadTask.cancel(true);
    	if(photoInfoTask != null)
    		photoInfoTask.cancel(true);
    	
    	super.onStop();
    }
    
	@Override
	public void onClick(final View v) {
		if(v.getId() == R.id.btnDownload){
			// Download the image
			Log.i(Constants.LOG_TAG,"Downloading file");
			if(mPhoto == null)
				return;
			String url = mPhoto.getMediumUrl();
			String filename = url.substring(url.lastIndexOf('/')+1);
			//Store the image in app folder
			filename = Helper.getAbsoluteFileLocation(filename,this,false);
			boolean status = Helper.saveImageToDisk(filename,mImage);
			if(status){
				Toast.makeText(this, getString(R.string.image_saved), Toast.LENGTH_SHORT).show();
				runMediaScanIntent(filename);
			}
			else
				Toast.makeText(this, getString(R.string.image_not_saved), Toast.LENGTH_LONG).show();
		}else if(v.getId() == R.id.btnInfo){
			// Fetch the info about the image
			Log.i(Constants.LOG_TAG,"Getting info about the photo");
			if(mPhoto == null)
				return;

			if(exifInfo == null){		// Cache the values
				photoInfoTask = new PhotoInfoTask();
				photoInfoTask.execute(mPhoto.getId(),mPhoto.getSecret());
				new Thread(){
					public void run() {

					try {
						exifInfo = photoInfoTask.get();
						if(exifInfo != null)
							startImageDetailsActivity();
						else{
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), getString(R.string.no_image_info),Toast.LENGTH_SHORT).show();
									return;
								}
							});		
						}
					} catch (Exception e) {
						e.printStackTrace();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), getString(R.string.no_image_info),Toast.LENGTH_SHORT).show();
								return;
							}
						});	
						return;
					}
				};
			}.start();
			}else{
				startImageDetailsActivity();
			}

		}
		
	}

	/**
	 * Run a media scan so that picture is available in the galery
	 * @param filename
	 */
	private void runMediaScanIntent(String filename) {

		File file = new File(filename);
		try{
			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			
		    Uri contentUri = Uri.fromFile(file);
		    mediaScanIntent.setData(contentUri);
		    sendBroadcast(mediaScanIntent);
		}catch (Exception e) {
			Log.w(Constants.LOG_TAG, "doMediaScan: Exception during media scan" + file);
		}

	}

	/**
	 * @param exifInfo 
	 * 
	 */
	void startImageDetailsActivity() {
		ArrayList<Exif> listExif = new ArrayList<Exif>(exifInfo);
		Intent imageInfoIntent = new Intent(getApplicationContext(),ImageDetailsActivity.class);
		
		Bundle bundle = new Bundle();
		bundle.putSerializable("exifInfo", listExif);
		imageInfoIntent.putExtra("exifInfo", bundle);
		startActivity(imageInfoIntent);
	}

	
}
