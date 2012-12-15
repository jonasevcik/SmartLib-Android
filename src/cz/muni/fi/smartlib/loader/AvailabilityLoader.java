package cz.muni.fi.smartlib.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.util.Log;

import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.model.Copy;
import cz.muni.fi.smartlib.network.SmartlibAPI;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;

public class AvailabilityLoader extends FancyLoader<List<Copy>> {
	private static final String TAG = AvailabilityLoader.class.getSimpleName();

	private String mSysno;
	private ConnectivityObserver mReceiver;
	private List<Copy> mCopies;
	private boolean mRefresh;
	
	public AvailabilityLoader(Context context, String sysno) {
		super(context);
		init(sysno);
	} 


	private void init(String sysno) {
		mSysno = sysno;
		mOnline = ((SmartLibMU)getContext().getApplicationContext()).isOnline();
		mHasError = false;
		mIsLoading = true;
		mCopies = null;
		mReceiver = null;
		mHasMoreResult = true;
		mRefresh = false;
		if (DEBUG) Log.i(TAG, "+++ Loader initiated. +++");
	}

	@Override
	public List<Copy> loadInBackground() {
		if (DEBUG) Log.i(TAG, "+++ loadInBackground() called +++");

		mIsLoading = true;

		List<Copy> copies = null;
	   	if (mOnline) {
	   		if (!mRefresh) {
		    	copies = SmartlibAPI.getInstance().getCopies(mSysno);
	   		} else {
	   			copies = SmartlibAPI.getInstance().refreshCopies(mCopies, mSysno);
	   		}
	    }
	    
	   	if (copies == null && mOnline) {  //if no internet then no errors
			mHasError = true;
		} else {
			mHasError = false;
		}

		return copies;
	}

	@Override
	public void deliverResult(List<Copy> copies) {
		mIsLoading = false;
		mRefresh = false;
		if (isReset()) {
			if (DEBUG) Log.w(TAG, "+++ Loader delivered result while loader was reseting. +++");
			// The Loader has been reset; ignore the result and invalidate the
			// data.
			onReleaseResources(mCopies);
			return;
		}
		
		List<Copy> oldCopies = null;
		
        oldCopies = mCopies;
        mCopies = copies;
        
		if (isStarted()) {
			if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager[mHasMoreResult: " + mHasMoreResult + ", mHasError: " + mHasError + ", mOnline: " + mOnline + "] +++");
			// If the Loader is in a started state, deliver the results to the
			// client. The superclass method does this for us.
			super.deliverResult(mCopies == null ?
                    null : new ArrayList<Copy>(mCopies));
		}

		// Invalidate the old data as we don't need it any more.
		if (oldCopies != null && oldCopies != copies) {
			onReleaseResources(oldCopies);
		}
	}

	private void onReleaseResources(List<Copy> data) {
		// List dont need to release anything
	}

	@Override
	protected void onStartLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

		if (mCopies != null) {
			if (DEBUG) Log.i(TAG, "+++ Delivering previously loaded data to the client...");
			// Deliver any previously loaded data immediately.
			deliverResult(null);
		}

		// Register the receivers that will notify the Loader when changes are
		// made.
		if (mReceiver == null) {
			mReceiver = new NetConnectivityObserver(getContext());
		}

		if (takeContentChanged()) {
			// When the observer detects a books db change, it will call
			// onContentChanged() on the Loader, which will cause the next call
			// to
			// takeContentChanged() to return true. If this is ever the case (or
			// if
			// the current data is null), we force a new load.
			if (DEBUG) Log.i(TAG, "+++ A content change has been detected... so force load! +++");
			forceLoad();
		} else if (mCopies == null) {
			// If the current data is null... then we should load data!
			if (DEBUG) Log.i(TAG, "+++ The current data is data is null... so force load! +++");
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStopLoading() called! +++");
		mIsLoading = false;
		// The Loader is in a stopped state, so we should attempt to cancel the
		// current load (if there is one).
		cancelLoad();

	}

	@Override
	protected void onReset() {
		if (DEBUG) Log.i(TAG, "+++ onReset() called! +++");
		// Ensure the loader has been stopped.
		onStopLoading();

		// At this point we can release the resources associated with 'mData'.
		if (mCopies != null) {
			onReleaseResources(mCopies);
			mCopies = null;
		}
		
		// The Loader is being reset, so we should stop monitoring for changes.
	    if (mReceiver != null) {
	    	mReceiver.unregister(getContext());
	    	mReceiver = null;
	    }
	    
	    mHasMoreResult = true;
	    mHasError = false;
	    mRefresh = false;
	}

	@Override
	public void onCanceled(List<Copy> copies) {
		if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");
		// Attempt to cancel the current asynchronous load.
		super.onCanceled(copies);

		// The load has been canceled, so we should release the resources
		// associated with 'data'.
		onReleaseResources(copies);
	}
	
	@Override
	public void forceLoad() {
	    if (DEBUG) Log.i(TAG, "+++ forceLoad() called! +++");
	    super.forceLoad();
	}
	
	public void refresh() {
		if (DEBUG) Log.i(TAG, "+++ refresh called! +++");
		mHasError = false;
		mHasMoreResult = true;
		mRefresh = true;
		forceLoad();
	}
	
	public static void refreshAvailability(Fragment context, int loaderId) {
		if (context.isAdded()) {
			final Loader loader = context.getLoaderManager().getLoader(loaderId);
			if (loader != null) {
				((AvailabilityLoader) loader).refresh();
			}
		}
	}
}
