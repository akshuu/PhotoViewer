/**
 * 
 */
package com.test.weddingsnap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.test.weddingsnap.util.Constants;

/**
 * @author akshatj
 *
 */
public class ImageAdapter extends BaseAdapter {
	 private Context mContext;
	 private List<Bitmap> imgs = null;
	 private PhotoList photos = null;
	 private Map<Photo,Bitmap> photoMap;
	 
	 public ImageAdapter(Context c) {
		 mContext = c;
	 }
	 
	 public ImageAdapter(Context c,PhotoList photoList,List<Bitmap> imgs) {
		 mContext = c;
		 this.photos = photoList;
		 this.imgs = imgs;
	 }
	 
	 public ImageAdapter(Context c,PhotoList photoList,Map<Photo,Bitmap> photo) {
		 mContext = c;
		 this.photos = photoList;
		 this.photoMap = photo;
	 }
	 
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return photos.size();
	}

	public Photo getPhoto(int position){
		return photos.get(position);
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		Photo tPhoto = photos.get(position);
		return photoMap.get(tPhoto);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	Log.i(Constants.LOG_TAG, "New imageview created");
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setPadding(4,4,4,4);
        } else {
        	Log.i(Constants.LOG_TAG, "reusing old imageview");
            imageView = (ImageView) convertView;
        }
        
        for(Photo photo:photos){
        	// Use cached images
        	//Bitmap img = Helper.readFileFromDisk(photo);
        	//if(img == null)
        	Bitmap img = (Bitmap)getItem(position);
        	
        	imageView.setImageBitmap(img);
        }
        return imageView;
	}
	
}
