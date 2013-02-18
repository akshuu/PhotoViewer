package com.test.weddingsnap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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

	Button btnDownload, btnInfo;
	ImageView imgView = null;
	Photo photo = null;
	Collection<Exif> exifInfo = null;
	Bitmap img ; 
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
        
        imgView = (ImageView)findViewById(R.id.imageView);
        ((RelativeLayout)imgView.getParent()).setBackgroundColor(Color.BLACK);
        Bundle photoBundle = getIntent().getExtras();
        photo = (Photo) photoBundle.get("photo");
       
        if(img == null){
	        ImageDownloadTask task = new ImageDownloadTask();
	        Log.i(Constants.LOG_TAG, "Downloading the image : " + photo.getMediumUrl());
	        task.execute(photo.getMediumUrl());
	        
	        try {
				img = task.get(20, TimeUnit.SECONDS);
			}catch(Exception ex){
			}
	    }
        
        Log.i(Constants.LOG_TAG, "Updating the imageview");
        imgView.setImageBitmap(img);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_image_view, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btnDownload){
			Log.i(Constants.LOG_TAG,"Downloading file");
			if(photo == null)
				return;
			String url = photo.getMediumUrl();
			String filename = url.substring(url.lastIndexOf('/')+1);
			//Store the image in app folder
			File dir = getExternalFilesDir("");
			if(dir == null){
				Log.i(Constants.LOG_TAG,"No external Storage. Saving image to:  " + getFilesDir().getAbsolutePath());
				dir = getFilesDir();
			}
			Log.i(Constants.LOG_TAG,"Saving image to:  " + dir);
			filename = dir + filename;
			boolean status = Helper.saveImageToDisk(filename,img);
			if(status)
				Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "Image was not saved.Please try again", Toast.LENGTH_LONG).show();
		}else if(v.getId() == R.id.btnInfo){
			Log.i(Constants.LOG_TAG,"Getting info about the photo");
			if(photo == null)
				return;

			if(exifInfo == null){		// Cache the values
				PhotoInfoTask photoInfo = new PhotoInfoTask();
				photoInfo.execute(photo.getId(),photo.getSecret());
				try {
					exifInfo = photoInfo.get();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
				ArrayList<Exif> listExif = new ArrayList<Exif>(exifInfo);
				Intent imageInfoIntent = new Intent(this,ImageDetailsActivity.class);
				
				Bundle bundle = new Bundle();
				bundle.putSerializable("exifInfo", listExif);
				imageInfoIntent.putExtra("exifInfo", bundle);
                startActivity(imageInfoIntent);
		}
		
	}
}
