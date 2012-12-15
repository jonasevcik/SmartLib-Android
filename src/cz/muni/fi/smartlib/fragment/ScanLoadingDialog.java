package cz.muni.fi.smartlib.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Validator;

public class ScanLoadingDialog extends SherlockDialogFragment implements OnClickListener {
	public static final String TAG = ScanLoadingDialog.class.getSimpleName();

	public interface ScanDialogListener {
		public void onBookFound(Book book);
		
		public void onBookNotFound();
		
	}
	

	public static final String PARAM_BARCODE_NAME = "barcode";
	public static final String PARAM_FORMAT_NAME = "format";
	
	private TextView mNotFound;
	private ProgressBar mProgressBar;
	private Button mCloseBtn;
	
	private Dialog mDialog;
	
	private ScanDialogListener mListener;
	
	private String mBarcode;
	private String mFormat;
	
	public static ScanLoadingDialog newInstance(String barcode, String format) {
		Bundle args = new Bundle();
		args.putString(PARAM_BARCODE_NAME, barcode);
		args.putString(PARAM_FORMAT_NAME, format);
		ScanLoadingDialog f = new ScanLoadingDialog();        
	    f.setArguments(args);
	    return f;
	}
	    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	    try {
	    	mListener = (ScanDialogListener) activity;
	    } catch (ClassCastException e) {
	        throw new ClassCastException(activity.toString() + " must implement ScanDialogListener");
	    }
	}    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		
		if (args != null) {
			mBarcode = args.getString(PARAM_BARCODE_NAME);
			mFormat = args.getString(PARAM_FORMAT_NAME);
		}
		
		if (mFormat.equals("CODE_39")) {
			RESTServiceHelper.getInstance(getSherlockActivity()).getBookByBarcode(new ScanReceiver(), mBarcode);
		}
		
		if (mFormat.equals("EAN_13")) {
			if (Validator.isISBN13(mBarcode)) {
				RESTServiceHelper.getInstance(getSherlockActivity()).getBookByIsbn(new ScanReceiver(), mBarcode);
			} else if (Validator.isISBN10(Validator.constructISBN10FromEAN13(mBarcode))){
				RESTServiceHelper.getInstance(getSherlockActivity()).getBookByIsbn(new ScanReceiver(), mBarcode);
				
			} else {
				UIUtils.makeToast(getSherlockActivity(), R.string.toast_scan_invalid_isbn);
			}
		}
		
	}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
		AlertDialog.Builder builder;
		
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View scanView = inflater.inflate(R.layout.scan_alert_dialog, null);
		
		mNotFound = (TextView) scanView.findViewById(R.id.dialog_scan_not_found_text);
		mProgressBar = (ProgressBar) scanView.findViewById(R.id.dialog_scan_progress);
		mCloseBtn = (Button) scanView.findViewById(R.id.dialog_scan_close);
		mCloseBtn.setOnClickListener(this);
		
		
		builder = new AlertDialog.Builder(getActivity()).setView(scanView).setCancelable(false);
		mDialog = builder.create();
		
		return mDialog;
    }
    

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.dialog_scan_close) {
			dismiss();
		}
	}

  
	private void disableUI() {
		mProgressBar.setVisibility(View.VISIBLE);
		mDialog.setCancelable(false);
		mNotFound.setVisibility(View.GONE);
		mCloseBtn.setEnabled(false);
	}
	
	private void enableUI(boolean bookFound) {
		mProgressBar.setVisibility(View.GONE);
		mDialog.setCancelable(true);
		mCloseBtn.setEnabled(true);
		if (!bookFound) mNotFound.setVisibility(View.VISIBLE);
	}
	

	public class ScanReceiver extends ResultReceiver {

		private boolean enabled;
		
		public ScanReceiver() {
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
				Book result = resultData.getParcelable(RESTServiceHelper.PARAM_BOOK);
				if (result != null) {
					enableUI(true);
					mListener.onBookFound(result);
					dismiss();
				} else {
					enableUI(false);
				}
			} 
	    }
		
	}

}
