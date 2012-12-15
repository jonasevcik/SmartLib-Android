package cz.muni.fi.smartlib.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.model.User;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.Validator;
import cz.muni.fi.smartlib.utils.UIUtils;

public class LoginDialogFragment extends SherlockDialogFragment implements OnClickListener {
	public static final String TAG = LoginDialogFragment.class.getSimpleName();
	
	
	public interface LoginDialogListener {
		public void onRegister();
		
		public void onLoginSuccess();
		
		public void onLoginFailed();
		
	}
	
	
	public static final String PARAM_USER_NAME = "user";
	
	
	private User mUser;
	
	private EditText mUco;
	private EditText mPassword;
	private CheckBox mRememberMe;
	private ProgressBar mProgressBar;
	private Button mLogInButton;
	private Button mSignUpButton;
	
	private Dialog mDialog;
	
	private LoginDialogListener mListener;
	
	
	    public static LoginDialogFragment newInstance() {
	    	
	    	Bundle args = new Bundle();
	        LoginDialogFragment f = new LoginDialogFragment();        
	        f.setArguments(args);
	        return f;
	    }

	    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	    try {
	    	mListener = (LoginDialogListener) activity;
	    } catch (ClassCastException e) {
	        throw new ClassCastException(activity.toString() + " must implement LoginDialogListener");
	    }
	}    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
    	Bundle args = getArguments();
    	
    	if (args != null) {
    		User user = args.getParcelable(PARAM_USER_NAME);
    		if (user != null) {
    			mUser = user;
    		}
    	}
    	 	
		AlertDialog.Builder builder;
		
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View loginView = inflater.inflate(R.layout.login_alert_dialog, null);
		mUco = (EditText) loginView.findViewById(R.id.dialog_login_uco);
		mUco.setTextColor(Color.parseColor("#686868"));
		mPassword = (EditText) loginView.findViewById(R.id.dialog_login_password);
		mPassword.setTextColor(Color.parseColor("#686868"));
		mRememberMe = (CheckBox) loginView.findViewById(R.id.dialog_login_save_password);
		mRememberMe.setButtonDrawable(R.drawable.checkbox);
		mProgressBar = (ProgressBar) loginView.findViewById(R.id.dialog_login_progress);
		mLogInButton = (Button) loginView.findViewById(R.id.dialog_button_login);
		mSignUpButton = (Button) loginView.findViewById(R.id.dialog_button_sign_up);
		mLogInButton.setOnClickListener(this);
		mSignUpButton.setOnClickListener(this);
		
		// get saved uco and password
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("rememberMe", false)) {
			mUco.setText(settings.getString("uco", ""));
			mPassword.setText(settings.getString("password", ""));
			mRememberMe.setChecked(true);
		}

		builder = new AlertDialog.Builder(getActivity()).setView(loginView).setCancelable(true);
		mDialog = builder.create();
		return mDialog;
    }
    

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_button_login) {
			onClickLogin(v);
		} else {
			onClickRegister(v);
		}
		
	}


	private void onClickRegister(View v) {
		mListener.onRegister();
	}
	    
	private void onClickLogin(View v) {
		if (Validator.isNullorEmpty(mUco.getText().toString()) || Validator.isNullorEmpty(mPassword.getText().toString())) {
			UIUtils.makeWarningToast(getActivity(), R.string.toast_empty_fields);
		} else {
			disableUI();
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
		
			RESTServiceHelper.getInstance(getActivity()).login(new LogInReceiver(), user);
		}
	}

	private void disableUI() {
		mProgressBar.setVisibility(View.VISIBLE);
		mDialog.setCancelable(false);
		mUco.setEnabled(false);
		mPassword.setEnabled(false);
		mRememberMe.setEnabled(false);
		mLogInButton.setEnabled(false);
		mSignUpButton.setEnabled(false);
	}
	
	private void enableUI() {
		mProgressBar.setVisibility(View.GONE);
		mDialog.setCancelable(true);
		mUco.setEnabled(true);
		mPassword.setEnabled(true);
		mRememberMe.setEnabled(true);
		mLogInButton.setEnabled(true);
		mSignUpButton.setEnabled(true);
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
					mListener.onLoginSuccess();
					dismiss();
					
				} else {
					mListener.onLoginFailed();
					dismiss();
				}
			} 
	    }
		
	}

}
