package cz.muni.fi.smartlib.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Validator;

public class RegisterDialogFragment extends SherlockDialogFragment implements OnClickListener{
	public static final String TAG = RegisterDialogFragment.class.getSimpleName();

	public interface RegisterDialogListener {
		public void onRegister();
		
		public void onRegisterSuccess();
		
		public void onRegisterFailed();
		
	}
	
	
	public static final String PARAM_USER_NAME = "user";
	
	private EditText mUco;
	private EditText mFirstName;
	private EditText mLastName;
	private ProgressBar mProgressBar;
	private Button mSignUpButton;
	
	private Dialog mDialog;
	
	private RegisterDialogListener mListener;
	
	
	public static RegisterDialogFragment newInstance() {
		Bundle args = new Bundle();
		RegisterDialogFragment f = new RegisterDialogFragment();        
	    f.setArguments(args);
	    return f;
	}

	    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	    try {
	    	mListener = (RegisterDialogListener) activity;
	    } catch (ClassCastException e) {
	        throw new ClassCastException(activity.toString() + " must implement RegisterDialogListener");
	    }
	}    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
		AlertDialog.Builder builder;
		
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View registerView = inflater.inflate(R.layout.register_alert_dialog, null);
		mUco = (EditText) registerView.findViewById(R.id.dialog_register_uco);
		mFirstName = (EditText) registerView.findViewById(R.id.dialog_registration_first_name);
		mLastName = (EditText) registerView.findViewById(R.id.dialog_registration_last_name);
		mUco.setTextColor(Color.parseColor("#686868"));
		mFirstName.setTextColor(Color.parseColor("#686868"));
		mLastName.setTextColor(Color.parseColor("#686868"));
		
		mProgressBar = (ProgressBar) registerView.findViewById(R.id.dialog_registration_progress);
		mSignUpButton = (Button) registerView.findViewById(R.id.dialog_button_sign_up);
		mSignUpButton.setOnClickListener(this);
		
		
		builder = new AlertDialog.Builder(getActivity()).setView(registerView).setCancelable(true);
		mDialog = builder.create();
		return mDialog;
    }
    

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_button_sign_up) {
			onClickRegister(v);
		}
	}


	private void onClickRegister(View v) {

		String uco = mUco.getText().toString();
		String first = mFirstName.getText().toString();
		String last = mLastName.getText().toString();
		
		if (!Validator.isNullorEmpty(uco) && !Validator.isNullorEmpty(first) && !Validator.isNullorEmpty(last)) {
			disableUI();
			RESTServiceHelper.getInstance(getSherlockActivity()).registerUser(new RegisterReceiver(), uco, first, last);
		} else {
			UIUtils.makeWarningToast(getSherlockActivity(), R.string.toast_empty_fields);
		}
	}
	  
	private void disableUI() {
		mProgressBar.setVisibility(View.VISIBLE);
		mDialog.setCancelable(false);
		mUco.setEnabled(false);
		mFirstName.setEnabled(false);
		mLastName.setEnabled(false);
		mSignUpButton.setEnabled(false);
	}
	
	private void enableUI() {
		mProgressBar.setVisibility(View.GONE);
		mDialog.setCancelable(true);
		mUco.setEnabled(true);
		mFirstName.setEnabled(true);
		mLastName.setEnabled(true);
		mSignUpButton.setEnabled(true);
	}
	

	public class RegisterReceiver extends ResultReceiver {

		private boolean enabled;
		
		public RegisterReceiver() {
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
				enableUI();
				if (result) {
					mListener.onRegisterSuccess();
					dismiss();
					
				} else {
					mListener.onRegisterFailed();
					dismiss();
				}
			} 
	    }
		
	}

}
