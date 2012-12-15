package cz.muni.fi.smartlib.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.loader.AvailabilityLoader;
import cz.muni.fi.smartlib.loader.FancyLoader;
import cz.muni.fi.smartlib.model.Copy;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;

public class BookAvailabilityFragment extends NetFragment implements LoaderCallbacks<List<Copy>>, OnClickListener {
	private static final String TAG = BookAvailabilityFragment.class.getSimpleName();
	private static final boolean DEBUG = true;
	
	public static final String PARAM_BOOK_SYSNO = "sysno";
	
	public interface LoadAvailabilityListener {
    }
	
	private LoadAvailabilityListener mListener;

	private List<Copy> mCopies;	
	private String mSysno;
	private ProgressBar mProgressBar;
	private Button mCheckBtn;
	private TextView mEmpty;
	
	public static BookAvailabilityFragment newInstance(String sysno) {
		BookAvailabilityFragment f = new BookAvailabilityFragment();
	    Bundle args = new Bundle();
	    args.putString(RESTServiceHelper.PARAM_SYSNO, sysno);
	    f.setArguments(args);
	    return f;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LoadAvailabilityListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoadAvailabilityListener");
        }
    }	
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		
		if (args != null) {
			mSysno = args.getString(RESTServiceHelper.PARAM_SYSNO);
		}
		
		mCopies = new ArrayList<Copy>();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_book_availability, container, false);
		mProgressBar = (ProgressBar) view.findViewById(R.id.display_book_progress);
		mCheckBtn = (Button) view.findViewById(R.id.display_book_refresh_availability_button);
		mCheckBtn.setEnabled(mOnline);
		mCheckBtn.setOnClickListener(this);
		mEmpty = (TextView) view.findViewById(R.id.display_book_empty_copies);

		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.i(TAG, "+++ onActivityCreated() called +++");
       

		Bundle bundle = new Bundle();
        bundle.putString(PARAM_BOOK_SYSNO, mSysno);
		
		getLoaderManager().initLoader(FancyLoader.AVAILABILITY_LOADER_NET, bundle, this);
	}
	
	
	@Override
	public Loader<List<Copy>> onCreateLoader(int loader, Bundle bundle) {
    	if (DEBUG) Log.i(TAG, "+++ onCreateLoader() called +++");
    	
    	String sysno = null;
    	if (bundle != null) {
    		sysno = bundle.getString(PARAM_BOOK_SYSNO);
    	}
    	disableUI();
    	return new AvailabilityLoader(getActivity(), sysno);
	}
    
    @Override
    public void onLoadFinished(Loader<List<Copy>> loader, List<Copy> copies) {
    	if (DEBUG) Log.i(TAG, "+++ onLoadFinished() called +++");
    	enableUI();
    	if (copies != null && !FancyLoader.loadingHasError(this, FancyLoader.AVAILABILITY_LOADER_NET)) {
            mCopies.clear();
            mCopies.addAll(copies);
        	setAvailability(mCopies);
        } else {
        }
    }
    
    
    @Override
    public void onLoaderReset(Loader<List<Copy>> loader) {
    	if (DEBUG) Log.i(TAG, "+++ onLoaderRestart() called +++");
    	disableUI();
    	mCopies.clear();
    	clearAvailability();
    }
	
	
	private void clearAvailability() {
		Activity a = getActivity();
		if (a != null) {
			LinearLayout list = (LinearLayout) a.findViewById(R.id.display_book_availability_container);
			if (list != null) list.removeAllViews();
		} else {
			Log.e(TAG, "+++ getActivity() returns null +++");
		}
	}
	
	private void setAvailability(List<Copy> copies) {
		Activity a = getActivity();
		if (a != null) {
			LayoutInflater inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout list = (LinearLayout) a.findViewById(R.id.display_book_availability_container);
			list.removeAllViews();
			if (copies != null && copies.size() > 0) {
				mEmpty.setVisibility(View.GONE);
				for (Copy copy : copies) {
					View view = inflater.inflate(R.layout.list_availability, null);
					TextView library = (TextView) view
							.findViewById(R.id.list_availability_library);
					TextView status = (TextView) view
							.findViewById(R.id.list_availability_return);
					
					library.setText(copy.getLibrary());
					status.setText((copy.getStatus())? getString(R.string.copy_available): getString(R.string.copy_not_available));
					
					list.addView(view);
				}
			} else {
				mEmpty.setVisibility(View.VISIBLE);
			}
		} else {
			Log.e(TAG, "+++ getActivity() returns null +++");
		}
	}
	
	private void enableUI() {
		mCheckBtn.setEnabled(mOnline);
		mProgressBar.setVisibility(View.GONE);
	}
	
	private void disableUI() {
		mCheckBtn.setEnabled(false);
		mProgressBar.setVisibility(View.VISIBLE);
	}
	
	public void refresh() {
		if (DEBUG) Log.i(TAG, "+++ refresh() called +++");
		disableUI();
		AvailabilityLoader.refreshAvailability(this, FancyLoader.AVAILABILITY_LOADER_NET);		
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.display_book_refresh_availability_button) {
			refresh();
		}
		
	}

	@Override
	protected void onConnectivityChanged(boolean isConnected) {
		mCheckBtn.setEnabled(mOnline);
		if (mOnline) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}
	

	
}
