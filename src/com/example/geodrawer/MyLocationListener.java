package com.example.geodrawer;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class MyLocationListener implements LocationListener {

	private Context c;
	private MainActivity mainActivity;
	
    public MyLocationListener(Context applicationContext, MainActivity mActivity) {
		this.c = applicationContext;
		this.mainActivity = mActivity;
	}


	@Override
    public void onLocationChanged(Location loc) {
		mainActivity.updateLocation(loc);
    }


    @Override
    public void onProviderDisabled(String provider) {
         
        /******** Called when User off Gps *********/
        Toast.makeText(c, "Gps désactivé ", Toast.LENGTH_LONG).show();
    }
 
    @Override
    public void onProviderEnabled(String provider) {
         
        /******** Called when User on Gps  *********/
         
        Toast.makeText(c, "Gps activé ", Toast.LENGTH_LONG).show();
    }

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}