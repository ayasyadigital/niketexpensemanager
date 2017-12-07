package com.niketgoel.niketexpensemanager.expenselisting;

import com.niketgoel.niketexpensemanager.R;
import com.niketgoel.niketexpensemanager.helpers.DisplayDate;

public class ThisYear extends TabLayoutListingAbstract {
	
	@Override
	protected boolean condition(DisplayDate mDisplayDate) {
		return mDisplayDate.isCurrentWeek() || mDisplayDate.isCurrentMonth() || mDisplayDate.isNotCurrentMonthAndCurrentYear();
	}
	
	@Override
	protected void setType() {
		type = R.string.sublist_thisyear;
	}

	@Override
	protected void setModifiedValues() {
		isModifiedThisYear = true;
		isModifiedThisMonth = false;
		isModifiedThisWeek = false;
		isModifiedAll = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!isModifiedThisYear) {
			initListView();
		}
	}
	
}
