package com.niketgoel.niketexpensemanager.expenselisting;

import com.niketgoel.niketexpensemanager.R;
import com.niketgoel.niketexpensemanager.helpers.DisplayDate;

public class All extends TabLayoutListingAbstract {
	
	@Override
	protected boolean condition(DisplayDate mDisplayDate) {
		return true;
	}
	
	@Override
	protected void setType() {
		type = R.string.sublist_all;
	}

	@Override
	protected void setModifiedValues() {
		isModifiedThisYear = false;
		isModifiedThisMonth = false;
		isModifiedThisWeek = false;
		isModifiedAll = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(!isModifiedAll) {
			initListView();
		}
	}
	
}
