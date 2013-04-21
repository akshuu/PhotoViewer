/**
 * 
 */
package com.test.weddingsnap;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.test.weddingsnap.util.Constants;
import com.test.weddingsnap.util.Helper;

/**
 * @author akshatj
 *
 */
public class ImageAdapter extends BaseAdapter {
	 private Context mContext;
	 private PhotoList photos = null;
	 
	 public ImageAdapter(Context c) {
		 mContext = c;
	 }
	 
	 public ImageAdapter(Context c,PhotoList photoList) {
		 mContext = c;
		 this.photos = photoList;
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
		return tPhoto;
		
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	public void setPhotoList(PhotoList photoList){
		this.photos = photoList;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        
    	// Use cached images
        Bitmap img = null;
		Photo photo = photos.get(position);
		img = Helper.readFileFromDisk(photo.getThumbnailUrl(),mContext,true);
    	imageView.setImageBitmap(img);
        return imageView;
	}
	
}
