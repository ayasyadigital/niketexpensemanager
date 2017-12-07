package com.niketgoel.niketexpensemanager.sync;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.niketgoel.confconnect.gson.MyGson;
import com.niketgoel.confconnect.http.HTTP;
import com.niketgoel.niketexpensemanager.BaseActivity;
import com.niketgoel.niketexpensemanager.Constants;
import com.niketgoel.niketexpensemanager.ExpenseTrackerApplication;
import com.niketgoel.niketexpensemanager.Preferences;
import com.niketgoel.niketexpensemanager.R;
import com.niketgoel.niketexpensemanager.helpers.SharedPreferencesHelper;
import com.niketgoel.niketexpensemanager.models.User;
import com.niketgoel.niketexpensemanager.utils.Log;
import com.niketgoel.niketexpensemanager.utils.Strings;

import java.io.IOException;

public class UserAuthentication extends BaseActivity implements OnClickListener {

	private boolean isSignUp;
	private EditText name;
	private EditText password;
	private EditText email;
	private Button button;
	private ProgressDialog progressDialog;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_setup_user_account);
		isSignUp = getIntent().getBooleanExtra(Constants.KEY_IS_SIGNUP, false);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Wait...");
		progressDialog.setCancelable(false);
		name = (EditText) findViewById(R.id.sync_name);
		email = (EditText) findViewById(R.id.sync_email);
		password = (EditText) findViewById(R.id.sync_password);
		button = (Button) findViewById(R.id.startAuthenticate);
		handler = new Handler();
		if(!isSignUp) {
			((TextView) findViewById(R.id.headerText)).setText(getString(R.string.signin));
			name.setVisibility(View.GONE);
			button.setText(getString(R.string.signin));
		}
		
		button.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.startAuthenticate:
			progressDialog.show();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Looper.prepare();
					if(isSignUp){
						signup();
					} else {
						signin();
					}
				}
			}).start();
			
			break;

		default:
			break;
		}
	}
	
	private void signin() {
		User user = setUserData();
		
		try {
			if(user != null) {
				Gson gson = new MyGson().get();
				String postData = gson.toJson(user);
				Log.d("********************* Post Data "+postData);
				HTTP http = new HTTP(UserAuthentication.this);
				String fetchedData = http.signin(postData);
				Log.d("************** "+fetchedData);
				if(fetchedData != null) {
					if(http.getResponseCode() == 200) {
						User savedUser = gson.fromJson(fetchedData, User.class);
						if(Strings.notEmpty(savedUser.token)) {
							SharedPreferencesHelper.setUserDetails(savedUser);
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									startPrefActivity();
								}
							});
							return;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				showError();
			}
		});
	}
	
	private void signup() {
		User user = setUserData();
		
		try {
			if(user != null) {
				Gson gson = new MyGson().get();
				String postData = gson.toJson(user);
				Log.d("********************* Post Data "+postData);
				HTTP http = new HTTP(UserAuthentication.this);
				String fetchedData = http.signup(postData);
				Log.d("************** "+fetchedData);
				if(fetchedData != null) {
					if(http.getResponseCode() == 200) {
						User savedUser = gson.fromJson(fetchedData, User.class);
						if(Strings.notEmpty(savedUser.token)) {
							SharedPreferencesHelper.setUserDetails(savedUser);
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									startPrefActivity();
								}
							});
							return;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				showError();
			}
		});
	}
	
	private void showError() {
		progressDialog.dismiss();
		new AlertDialog.Builder(this)
		.setMessage("There is some problem with the Signup/Signin.....Try Again!!!!!")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Error")
		.setPositiveButton(getString(R.string.ok), (android.content.DialogInterface.OnClickListener)null)
		.show();
	}

	private void startPrefActivity() {
		ExpenseTrackerApplication.setSyncPrefs();
		if(ExpenseTrackerApplication.toSync) {
			SyncHelper.syncHelper = new SyncHelper(this);
        	SyncHelper.syncHelper.execute();
		}
		progressDialog.dismiss();
		Intent intent = new Intent(this, Preferences.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private User setUserData() {
		User user = null;
		if(isEnteredInfoCorrect()) {
			return null;
		} else {
			user = new User(name.getText().toString(), email.getText().toString(), password.getText().toString());
		    user.deviceId = ""+Build.BOARD.length()%10+ Build.BRAND.length()%10 +
	            	Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
	            	Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
	            	Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
	            	Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
	            	Build.TAGS.length()%10 + Build.TYPE.length()%10 +
	            	Build.USER.length()%10;
		}
		return user;
	}
	
	private boolean isEnteredInfoCorrect() {
		if(isSignUp) {
			return Strings.isEmpty(name.getText().toString()) || Strings.isEmpty(email.getText().toString()) || Strings.isEmpty(password.getText().toString()) || password.length() < 5 || !isEmailFormatCorrect(email.getText().toString());
		}
		return Strings.isEmpty(email.getText().toString()) || Strings.isEmpty(password.getText().toString()) || password.length() < 5 || !isEmailFormatCorrect(email.getText().toString());
	}

	private boolean isEmailFormatCorrect(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

}
