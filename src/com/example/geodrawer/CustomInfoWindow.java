package com.example.geodrawer;

import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.views.MapView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class CustomInfoWindow extends DefaultInfoWindow {
	private MainActivity mActivity;

	public CustomInfoWindow(MainActivity mainActivity, MapView mapView) {
		super(R.layout.bonuspack_bubble, mapView);
		this.mActivity = mainActivity;
	}

	@Override
	public void onClose() {
		this.mActivity.patternClicked();
		super.onClose();
	}
}