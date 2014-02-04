package com.example.geodrawer;


import android.content.Context;
import android.util.AttributeSet;

public class MyMapView extends org.osmdroid.views.MapView {
	
	public MyMapView (Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
	    int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
	    this.setMeasuredDimension(
	            parentWidth / 2, parentHeight);
	}
}
