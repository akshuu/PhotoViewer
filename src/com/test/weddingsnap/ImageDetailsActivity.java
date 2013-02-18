package com.test.weddingsnap;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.googlecode.flickrjandroid.photos.Exif;
import com.test.weddingsnap.util.Constants;

public class ImageDetailsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);
        
        Bundle exifBundle = getIntent().getBundleExtra("exifInfo");
        ArrayList<Exif> listExif = (ArrayList<Exif>) exifBundle.get("exifInfo");
        
        updateUI(listExif);
    }

    private void updateUI(ArrayList<Exif> listExif) {

    	for(Exif exif:listExif){
			Log.i(Constants.LOG_TAG,"Label = " + exif.getLabel());
			Log.i(Constants.LOG_TAG,"Raw = " + exif.getRaw());
			Log.i(Constants.LOG_TAG,"Tag = " + exif.getTag());
			Log.i(Constants.LOG_TAG,"Clean = " + exif.getClean());
			Log.i(Constants.LOG_TAG,"Tag Space = " + exif.getTagspace());
			
			if("Flash".equals(exif.getTag())){
				TextView txtFlashTxt = (TextView) findViewById(R.id.txtFlashTxt);
				txtFlashTxt.setText(exif.getRaw());
			}else if("FNumber".equals(exif.getTag())){
				TextView txtApertureTxt = (TextView) findViewById(R.id.txtApertureTxt);
				txtApertureTxt.setText(exif.getRaw());
			}else if("FocalLength".equals(exif.getTag())){
				TextView txtApertureTxt = (TextView) findViewById(R.id.txtFocalTxt);
				txtApertureTxt.setText(exif.getRaw());
			}else if("ExposureTime".equals(exif.getTag())){
				TextView txtValueTxt = (TextView) findViewById(R.id.txtExposureTxt);
				txtValueTxt.setText(exif.getRaw());
			}else if("CreateDate".equals(exif.getTag())){
				TextView txtValueTxt = (TextView) findViewById(R.id.txtTakenTxt);
				txtValueTxt.setText(exif.getRaw());
			}else if("Model".equals(exif.getTag())){
				TextView txtValueTxt = (TextView) findViewById(R.id.txtCameraTxt);
				txtValueTxt.setText(exif.getRaw());
			}else if("iso".equalsIgnoreCase(exif.getTag())){
				TextView txtValueTxt = (TextView) findViewById(R.id.txtIsoTxt);
				txtValueTxt.setText(exif.getRaw());
			}
			
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_image_details, menu);
        return true;
    }
}
