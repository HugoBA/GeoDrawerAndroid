package com.example.geodrawer;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class AcceptPatternListener implements DialogInterface.OnClickListener {
	
	public Context c;

	public AcceptPatternListener(Context context)
	{
		this.c = context;
	}
	
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		Toast.makeText(this.c, Integer.toString(which),
				Toast.LENGTH_LONG).show();
	}

}
