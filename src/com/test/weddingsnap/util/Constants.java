/**
 * 
 */
package com.test.weddingsnap.util;

import com.googlecode.flickrjandroid.Flickr;

/**
 * @author akshatj
 *
 */
public class Constants {
	public static final int MIN_DISTANCE_FOR_GEO_UPDATES = 10000; //Specified in meters	// 10Kms
	public static final int MIN_TIME_FOR_GEO_UPDATES = 15*60*1000; //Specified in milliseconds
	public static final String LOG_TAG = "WeddingSnap"; 
	public static final int ACCURACY = Flickr.ACCURACY_STREET;
	public static final int IMAGES_PER_PAGE = 24;
	
}
