package com.niketgoel.niketexpensemanager.show;

import android.app.Activity;
import android.widget.TextView;

import com.niketgoel.niketexpensemanager.R;

public class ShowLocationHandler {

	private TextView showLocation;

	public ShowLocationHandler(Activity activity, String location) {
		showLocation = (TextView) activity.findViewById(R.id.show_location);
		showLocation.setText(location);
	}

}
