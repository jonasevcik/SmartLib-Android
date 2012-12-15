package cz.muni.fi.smartlib;

import java.util.Observable;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.fragment.RegisterDialogFragment;
import cz.muni.fi.smartlib.fragment.RegisterDialogFragment.RegisterDialogListener;
import cz.muni.fi.smartlib.model.User;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Utils;
import cz.muni.fi.smartlib.utils.Validator;

public class SplashActivity extends SherlockFragmentActivity implements OnClickListener, RegisterDialogListener {
	private static final String TAG = SplashActivity.class.getSimpleName();
	
	private EditText mUco;
	private EditText mPassword;
	private Button mSkip;
	private Button mLogIn;
	private Button mSignUp;
	private CheckBox mRememberMe;
	private ProgressBar mProgress;
	
	private boolean mOnline;
	private InternetObserver mObserver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//		boolean firstTime = settings.getBoolean("first_time", true);
//		if (firstTime) {
//			Editor editor = settings.edit();
//			editor.putBoolean("first_time",	false);
//			editor.commit();
//			DBManager.getInstance(this);	//create db
//		} else {
//			Utils.startMainActivity(this);
//			return;
//		}
		
		setContentView(R.layout.activity_splash_screen);

		mUco = (EditText) findViewById(R.id.activity_splash_uco);
		mPassword = (EditText) findViewById(R.id.activity_splash_password);
		mSkip = (Button) findViewById(R.id.activity_splash_notnow);
		mLogIn = (Button) findViewById(R.id.activity_splash_login);
		mSignUp = (Button) findViewById(R.id.activity_splash_signup);
		mRememberMe = (CheckBox) findViewById(R.id.activity_splash_remember);
		mProgress = (ProgressBar) findViewById(R.id.activity_splash_progress);
		
		mUco.setTextColor(Color.parseColor("#686868"));
		mPassword.setTextColor(Color.parseColor("#686868"));
		
		
		mSkip.setOnClickListener(this);
		mLogIn.setOnClickListener(this);
		mSignUp.setOnClickListener(this);

	}
	
	@Override
    protected void onResume () {
    	mObserver = new InternetObserver(this);
        mOnline = ((SmartLibMU)getApplicationContext()).isOnline();
		if (mUco != null) mUco.setEnabled(mOnline);
		if (mPassword != null) mPassword.setEnabled(mOnline);
		if (mRememberMe != null) mRememberMe.setEnabled(mOnline);
		if (mSignUp != null) mSignUp.setEnabled(mOnline);
		if (mLogIn != null) mLogIn.setEnabled(mOnline);
    	super.onResume();
    }

    @Override
    protected void onPause() {
		if (mObserver != null) {
			mObserver.unregister(this);
			mObserver = null;
		}
    	super.onPause();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_splash_login:
			onClickLogin(v);
			break;
		case R.id.activity_splash_signup:
			onClickRegister(v);
			break;
		case R.id.activity_splash_notnow:
			Utils.startMainActivity(this);
			break;
		}
		
	}
	
	public void onClickRegister(View v) {

		RegisterDialogFragment registerDialog = RegisterDialogFragment.newInstance();
		registerDialog.show(getSupportFragmentManager(), "REGISTER_TAG");	
		
	}
	
	private void onClickLogin(View v) {
		if (Validator.isNullorEmpty(mUco.getText().toString()) || Validator.isNullorEmpty(mPassword.getText().toString())) {
			UIUtils.makeWarningToast(this, R.string.toast_empty_fields);
		} else {
			disableUI();
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			Editor editor = settings.edit();
			if (mRememberMe.isChecked()) {
				editor.putBoolean("rememberMe",	true);
				editor.putString("uco", mUco.getText().toString());
				editor.putString("password", mPassword.getText().toString());
				editor.commit();
			} else {
				editor.putBoolean("rememberMe", false);
				editor.putString("uco", "");
				editor.putString("password", "");
			}
				
			User user = new User();
			user.setUco(mUco.getText().toString());
			user.setPassword(mPassword.getText().toString());
		
			RESTServiceHelper.getInstance(this).login(new LogInReceiver(), user);
		}
	}

	private void disableUI() {
		mProgress.setVisibility(View.VISIBLE);
		mUco.setEnabled(false);
		mPassword.setEnabled(false);
		mRememberMe.setEnabled(false);
		mLogIn.setEnabled(false);
		mSignUp.setEnabled(false);
		mSkip.setEnabled(false);
	}
	
	private void enableUI() {
		mProgress.setVisibility(View.GONE);
		mUco.setEnabled(true);
		mPassword.setEnabled(true);
		mRememberMe.setEnabled(true);
		mLogIn.setEnabled(true);
		mSignUp.setEnabled(true);
		mSkip.setEnabled(true);
	}

	public void onLoginSuccess() {
		UIUtils.makeToast(this, R.string.toast_login_succ);
		Utils.startMainActivity(this);
	}

	public void onLoginFailed() {
		UIUtils.makeWarningToast(this, R.string.toast_login_fail);
		
	}
	
	@Override
	public void onRegister() {
	}
	


	@Override
	public void onRegisterSuccess() {
		UIUtils.makeLongToast(this, R.string.toast_registration_success);
		
	}

	@Override
	public void onRegisterFailed() {
		UIUtils.makeWarningToast(this, R.string.toast_registration_fail);
	}
	
	public class LogInReceiver extends ResultReceiver {

		private boolean enabled;
		
		public LogInReceiver() {
			super(new Handler());
			this.enabled = true;
		}

		public void unregister() {
			enabled = false;
		}
		
		@Override
	    protected void onReceiveResult (int resultCode, Bundle resultData) {
			if (enabled) {
				Log.i(TAG, "Result from REST "+ resultCode + "received.");
				boolean result = resultData.getBoolean(RESTServiceHelper.PARAM_RESULT);
				if (result) {
					onLoginSuccess();
				} else {
					onLoginFailed();
				}
				enableUI();
			} 
	    }
		
	}

	public class InternetObserver extends ConnectivityObserver {

		public InternetObserver(Context context) {
			super(context);
		}

		@Override
		public void update(Observable observable, Object data) {
			mOnline = (Boolean) data;
			if (mUco != null) mUco.setEnabled(mOnline);
			if (mPassword != null) mPassword.setEnabled(mOnline);
			if (mRememberMe != null) mRememberMe.setEnabled(mOnline);
			if (mSignUp != null) mSignUp.setEnabled(mOnline);
			if (mLogIn != null) mLogIn.setEnabled(mOnline);
		}
	} 
	

}
