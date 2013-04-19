package com.test.weddingsnap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
	private Photo photo = null;
	private Collection<Exif> exifInfo = null;
	private Bitmap img ; 
	private ImageDownloadTask imageDownloadTask;
	private PhotoInfoTask photoInfoTask ;
	private ProgressDialog progress ;

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
        
        progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.please_wait));
        progress.setMessage(getString(R.string.fetching_data));
        
        progress.show();
        progress.setCancelable(true);		// Allow it to be cancelled incase it blocks due to network


        imgView = (ImageView)findViewById(R.id.imageView);
        ((RelativeLayout)imgView.getParent()).setBackgroundColor(Color.BLACK);
        Bundle photoBundle = getIntent().getExtras();
        photo = (Photo) photoBundle.get("photo");
       
        if(savedInstanceState == null){
	        if(img == null){
		        imageDownloadTask = new ImageDownloadTask();
		        Log.i(Constants.LOG_TAG, "Downloading the image : " + photo.getMediumUrl());
		        imageDownloadTask.execute(photo.getMediumUrl());
		        new Thread(){
					public void run() {
				        try {
				        	// if image not fetched in 20 sec, timeout
							img = imageDownloadTask.get(20, TimeUnit.SECONDS);
						}catch(Exception ex){
							img = null;
						}
				        Log.i(Constants.LOG_TAG, "Updating the imageview");
				        showImage();
					}
	
		        }.start();
		    }else{
		        Log.i(Constants.LOG_TAG, "Updating the imageview from cache");
		        showImage();
		    }
        }
        else{
        	Log.i(Constants.LOG_TAG, "Updating the imageview from cache...maybe on rotation");
        	img = savedInstanceState.getParcelable("bitmap");
        	showImage();
	    }
    }

    /**
	 * 
	 */
	void showImage() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				progress.dismiss();
				if(img != null)
					imgView.setImageBitmap(img);
				else
					Toast.makeText(getApplicationContext(), getString(R.string.no_image),Toast.LENGTH_SHORT).show();
			}
		});
	};
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	img = savedInstanceState.getParcelable("bitmap");
    	super.onRestoreInstanceState(savedInstanceState);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	outState.putParcelable("bitmap", img);
    	super.onSaveInstanceState(outState);
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
			Log.i(Constants.LOG_TAG,"Downloading file");
			if(photo == null)
				return;
			String url = photo.getMediumUrl();
			String filename = url.substring(url.lastIndexOf('/')+1);
			//Store the image in app folder
			filename = Helper.getAbsoluteFileLocation(filename,this,false);
			boolean status = Helper.saveImageToDisk(filename,img);
			if(status){
				Toast.makeText(this, getString(R.string.image_saved), Toast.LENGTH_SHORT).show();
				runMediaScanIntent(filename);
			}
			else
				Toast.makeText(this, getString(R.string.image_not_saved), Toast.LENGTH_LONG).show();
		}else if(v.getId() == R.id.btnInfo){
			Log.i(Constants.LOG_TAG,"Getting info about the photo");
			if(photo == null)
				return;

			if(exifInfo == null){		// Cache the values
				photoInfoTask = new PhotoInfoTask();
				photoInfoTask.execute(photo.getId(),photo.getSecret());
				new Thread(){
					public void run() {

					try {
						exifInfo = photoInfoTask.get();
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					if(exifInfo == null){

						 runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), getString(R.string.no_image_info),Toast.LENGTH_SHORT).show();
								}
							});
						return;
					}
					startImageDetailsActivity();
					
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
