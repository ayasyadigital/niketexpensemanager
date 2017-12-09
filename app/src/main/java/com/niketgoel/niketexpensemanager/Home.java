package com.niketgoel.niketexpensemanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.niketgoel.niketexpensemanager.entry.CameraEntry;
import com.niketgoel.niketexpensemanager.entry.FavoriteEntry;
import com.niketgoel.niketexpensemanager.entry.Text;
import com.niketgoel.niketexpensemanager.entry.Voice;
import com.niketgoel.niketexpensemanager.expenselisting.ExpenseListing;
import com.niketgoel.niketexpensemanager.helpers.ConvertCursorToListString;
import com.niketgoel.niketexpensemanager.helpers.DatabaseAdapter;
import com.niketgoel.niketexpensemanager.helpers.GraphHelper;
import com.niketgoel.niketexpensemanager.helpers.LocationHelper;
import com.niketgoel.niketexpensemanager.helpers.UnfinishedEntryCount;
import com.niketgoel.niketexpensemanager.models.Entry;
import com.niketgoel.niketexpensemanager.sync.SyncHelper;

import java.util.Calendar;

public class Home extends BaseActivity implements OnClickListener {

	private Bundle bundle;
	private GraphHelper mHandleGraph;
	private ProgressBar graphProgressBar;
	private UnfinishedEntryCount unfinishedEntryCount;
	private ConvertCursorToListString mConvertCursorToListString;
	private int STORAGE_PERMISSION_CODE = 23;
	private static final int PERMISSION_REQUEST_CODE_LOCATION = 111;

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
		FlurryAgent.onEvent(getString(R.string.home_screen));
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		bundle = new Bundle();
		mConvertCursorToListString = new ConvertCursorToListString(this);
		////// ********* Adding Click Listeners to HomeActivity ********** /////////

		((Button) findViewById(R.id.home_text)).setOnClickListener(this);
		((Button) findViewById(R.id.home_voice)).setOnClickListener(this);
		((Button) findViewById(R.id.home_camera)).setOnClickListener(this);
		((Button) findViewById(R.id.home_favorite)).setOnClickListener(this);
		((Button) findViewById(R.id.home_save_reminder)).setOnClickListener(this);
		((ImageView) findViewById(R.id.home_listview)).setOnClickListener(this);

		ImageView mainGenerateReport = (ImageView) findViewById(R.id.home_generate_report);
		mainGenerateReport.setVisibility(View.VISIBLE);
		mainGenerateReport.setOnClickListener(this);

		graphProgressBar = (ProgressBar) findViewById(R.id.graph_progress_bar);
		graphProgressBar.setVisibility(View.VISIBLE);

		if (ExpenseTrackerApplication.toSync) {
			SyncHelper.syncHelper = new SyncHelper(this);
			SyncHelper.syncHelper.execute();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//finding current location
		if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, getApplicationContext(), Home.this)) {
			LocationHelper mLocationHelper = new LocationHelper();
			Location location = mLocationHelper.getBestAvailableLocation();
			if (location == null) {
				mLocationHelper.requestLocationUpdate();
			}
		} else {
			ActivityCompat.requestPermissions(Home.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE_LOCATION);
		}

		mHandleGraph = new GraphHelper(this, graphProgressBar);
		unfinishedEntryCount = new UnfinishedEntryCount(mConvertCursorToListString.getEntryList(false, ""), null, null, null, ((TextView) findViewById(R.id.home_unfinished_entry_count)));
		unfinishedEntryCount.execute();
		mHandleGraph.execute();
	}

	@Override
	public void onClick(View clickedView) {
		boolean isMediaMounted = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (isMediaMounted) {
			if (!ExpenseTrackerApplication.isInitialized) {
				if (isReadStorageAllowed()) {
					ExpenseTrackerApplication.Initialize();
					return;
				}
				//If the app has not the permission then asking for the permission
				requestStoragePermission();
			}
		}
		int idOfClickedView = clickedView.getId();
		cancelHandleGraphTask();
		cancelUnfinishedEntryTask();
		switch (idOfClickedView) {
			case R.id.home_text:
				Intent intentTextEntry = new Intent(this, Text.class);
				intentTextEntry.putExtras(bundle);
				startActivity(intentTextEntry);
				break;

			case R.id.home_voice:
				if (isMediaMounted) {
					Intent intentVoice = new Intent(this, Voice.class);
					intentVoice.putExtras(bundle);
					startActivity(intentVoice);
				} else {
					Toast.makeText(this, "sdcard not available", Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.home_camera:
				if (isMediaMounted) {
					Intent intentCamera = new Intent(this, CameraEntry.class);
					intentCamera.putExtras(bundle);
					startActivity(intentCamera);
				} else {
					Toast.makeText(this, "sdcard not available", Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.home_favorite:
				Intent intentFavorite = new Intent(this, FavoriteEntry.class);
				intentFavorite.putExtras(bundle);
				startActivity(intentFavorite);
				break;

			case R.id.home_save_reminder:
				FlurryAgent.onEvent(getString(R.string.save_reminder));
				insertToDatabase(R.string.unknown);
				Intent intentListView = new Intent(this, ExpenseListing.class);
				startActivity(intentListView);
				SyncHelper.startSync();
				break;

			case R.id.home_listview:
				Intent intentListView2 = new Intent(this, ExpenseListing.class);
				startActivity(intentListView2);
				break;

			case R.id.home_generate_report:
				startGenerateReportActivity();
				break;
		}//end switch
	}//end onClick

//	private void createDatabaseEntry(int typeOfEntry) {
//		bundle.putLong(Constants.KEY_ID, Long.parseLong(insertToDatabase(typeOfEntry).toString()));
//
//		if(LocationHelper.currentAddress != null && !LocationHelper.currentAddress.trim().equals("")) {
//			bundle.putBoolean(Constants.KEY_SET_LOCATION, false);
//		} else {
//			bundle.putBoolean(Constants.KEY_SET_LOCATION, true);
//		}
//	}

	///////// ******** function to mark entry into the database and returns the id of the new entry ***** //////
	private Long insertToDatabase(int type) {
		Entry list = new Entry();
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setFirstDayOfWeek(Calendar.MONDAY);

		list.timeInMillis = mCalendar.getTimeInMillis();

		if (LocationHelper.currentAddress != null && !LocationHelper.currentAddress.trim().equals("")) {
			list.location = LocationHelper.currentAddress;
		}
		list.type = getString(type);
		DatabaseAdapter mDatabaseAdapter = new DatabaseAdapter(this);
		mDatabaseAdapter.open();
		long id = mDatabaseAdapter.insertToEntryTable(list);
		mDatabaseAdapter.close();
		return id;
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancelHandleGraphTask();
		cancelUnfinishedEntryTask();
	}

	private void cancelUnfinishedEntryTask() {
		if (unfinishedEntryCount != null && !unfinishedEntryCount.isCancelled()) {
			unfinishedEntryCount.cancel(true);
		}
	}

	private void cancelHandleGraphTask() {
		if (mHandleGraph != null && !mHandleGraph.isCancelled()) {
			mHandleGraph.cancel(true);
		}
	}

	private boolean isReadStorageAllowed() {
		//Getting the permission status
		int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		//If permission is granted returning true
		if (result == PackageManager.PERMISSION_GRANTED)
			return true;

		//If permission is not granted returning false
		return false;
	}

	//Requesting permission
	private void requestStoragePermission() {

		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			//If the user has denied the permission previously your code will come to this block
			//Here you can explain why you need this permission
			//Explain here why you need this permission
		}

		//And finally ask for the permission
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
	}

	//This method will be called when the user will tap on allow or deny
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		//Checking the request code of our request
		if (requestCode == STORAGE_PERMISSION_CODE) {

			//If permission is granted
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				//Displaying a toast
				ExpenseTrackerApplication.Initialize();
			} else {
				//Displaying another toast if permission is not granted
				Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
			}
		}

		if (requestCode == PERMISSION_REQUEST_CODE_LOCATION) {
			try {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					//Displaying a toast
					LocationHelper mLocationHelper = new LocationHelper();
					Location location = mLocationHelper.getBestAvailableLocation();
					if (location == null) {
						mLocationHelper.requestLocationUpdate();
					}
				} else {
					//Displaying another toast if permission is not granted
					Toast.makeText(this, "Oops you just denied the GPS Permission", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean checkPermission(String strPermission, Context _c, Activity _a) {
		int result = ContextCompat.checkSelfPermission(_c, strPermission);
		if (result == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			return false;
		}
	}
}