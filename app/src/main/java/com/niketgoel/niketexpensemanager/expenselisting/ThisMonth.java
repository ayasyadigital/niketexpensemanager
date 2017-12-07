package com.niketgoel.niketexpensemanager.expenselisting;

import com.niketgoel.niketexpensemanager.R;
import com.niketgoel.niketexpensemanager.helpers.DisplayDate;

public class ThisMonth extends TabLayoutListingAbstract {
		
	@Override
	protected boolean condition(DisplayDate mDisplayDate) {
		return mDisplayDate.isCurrentWeek() || mDisplayDate.isCurrentMonth();
	}
	
	@Override
	protected void setType() {
		type = R.string.sublist_thismonth;
	}

	@Override
	protected void setModifiedValues() {
		isModifiedThisYear = false;
		isModifiedThisMonth = true;
		isModifiedThisWeek = false;
		isModifiedAll = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(!isModifiedThisMonth) {
			initListView();
		}
	}
	
}
