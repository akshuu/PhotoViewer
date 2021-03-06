package com.test.weddingsnap.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.places.PlacesInterface;

public class Helper {
	private static Helper instance = null;
    private static final String API_KEY = "99fbbfc6b79d13ffb7f7a62c7a65d915";
    private static final String API_SEC = "0711b7d80e2fb3e7";

    private Helper() {}

    public static Helper getInstance() {
            if (instance == null) {
                    instance = new Helper();
            }

            return instance;
    }

    public Flickr getFlickr() {
            try {
                    Flickr f = new Flickr(API_KEY, API_SEC, new REST());
                    return f;
            } catch (ParserConfigurationException e) {
                    return null;
            }
    }
    
    public PlacesInterface getPlacesInterface(){
    	return getFlickr().getPlacesInterface();
    }
    
    public PhotosInterface getPhotosInterface(){
    	return getFlickr().getPhotosInterface();
    }
 
    /**
     * Downloads files from URL. Use GZIP header to reduce network bandwidth
     * @param url
     * @return
     * @throws Exception
     */
    public static Bitmap downloadFile(String url) throws Exception{
    	
//    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	final HttpClient httpClient = AndroidHttpClient.newInstance("Android");
    	HttpResponse response;	
		HttpGet getMethod;
		
		URI uri = null;
		try {
			uri = new URI(url);
			getMethod = new HttpGet(uri);
		
		// Add GZIP request to allow for zipped response from server
			
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			AndroidHttpClient.modifyRequestToAcceptGzipResponse(getMethod);

			response = httpClient.execute(getMethod);
			Log.d(Constants.LOG_TAG, "Http request status code is : " + String.valueOf(response.getStatusLine().getStatusCode()));
			
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.w(Constants.LOG_TAG, "Error " + response.getStatusLine().getStatusCode() //$NON-NLS-1$//$NON-NLS-2$
						+ " while retrieving bitmap from " + url); //$NON-NLS-1$
				return null;
			}
		
			// Parse the input and construct the bitmap
			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					return BitmapFactory.decodeStream(new BufferedInputStream(
							inputStream));
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		}
		catch(IllegalArgumentException exception) {
			Log.e(Constants.LOG_TAG, "The url " + uri + " is invalid! Can't proceed with the GET request");
			throw exception;
		}finally{
			((AndroidHttpClient)httpClient).close();																		
		}
		return null;
    }
    
    /**
     * Saves the provided bitmap to disk
     * @param strDestFile
     * @param bitmap
     * @return
     */
    public static boolean saveImageToDisk(String strDestFile,Bitmap bitmap) {
    	if (bitmap == null) {
			return false;
		}
    	File destFile = new File(strDestFile);
		FileOutputStream fos = null;
		try {
			if (destFile.exists()) {
				destFile.delete();
			}
			fos = new FileOutputStream(destFile);
			boolean status = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			Log.d(Constants.LOG_TAG,"image saved to disk ? " + status);
		} catch (FileNotFoundException fnfe) {
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe) {
				}
			}
		}
		return true;
	}
    
    /**
	 * @param filename
	 * @return
	 */
	public static String getAbsoluteFileLocation(String filename,Context context,boolean isThumbNail) {
		
		if(isThumbNail){		// For thumbnails, save them to a different location
			File dir = context.getExternalFilesDir("");
			
			if(dir == null){
				Log.d(Constants.LOG_TAG,"No external Storage. Saving image to:  " + context.getFilesDir().getAbsolutePath());
				dir = context.getFilesDir();
			}
			File f  = new File(dir + "/thumbnails");
			f.mkdirs();
			filename = dir + "/thumbnails/" + filename;
		}else{
			File picturePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			if(!picturePath.exists())
				if(!picturePath.mkdirs()){
					return null;
				}
			filename = picturePath + "/"+ filename;
		}
		Log.d(Constants.LOG_TAG,"image path:  " + filename);
		return filename;
	}

	/*
	 * Read the cached thumbnails/images from the disk
	 */
	public static Bitmap readFileFromDisk(String url, Context mContext,boolean isThumbNail){
			String filename = url.substring(url.lastIndexOf('/')+1);
			filename = getAbsoluteFileLocation(filename, mContext,isThumbNail);
			FileInputStream fis = null;
			Bitmap img = null;
			{
				Log.d(Constants.LOG_TAG,"Reading file:  " + filename);
				try {
					fis = new FileInputStream(filename);
					img = BitmapFactory.decodeStream(fis);
					return img;
				} catch (FileNotFoundException e) {
				}finally{
					if(fis!=null){
						try {
							fis.close();
						} catch (IOException e) {
						}
						fis = null;
					}
				}
				
			}
			return null;
	}

	public static void showError(final Activity mActivity, final String error) {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mActivity.getApplicationContext(), error,Toast.LENGTH_SHORT).show();
				return;
			}
		});		
	}

}
