package cz.muni.fi.smartlib.loader;

import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.network.SmartlibAPI;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import cz.muni.fi.smartlib.utils.StatisticsHelper;
import android.content.Context;
import android.util.Log;

public class StatisticsLoader extends FancyLoader<int[]> {
	
	private static final String TAG = StatisticsLoader.class.getSimpleName();

	public StatisticsLoader(Context context, String sysno) {
		super(context);
		init(sysno);
	}

	private String mSysno;
	private ConnectivityObserver mReceiver;
	private int[] mStatistics;
	
	private void init(String sysno) {
		mSysno = sysno;
		mOnline = ((SmartLibMU)getContext().getApplicationContext()).isOnline();
		mHasError = false;
		mIsLoading = true;
		mStatistics = null;
		mReceiver = null;
		mHasMoreResult = true;
		if (DEBUG) Log.i(TAG, "+++ Loader initiated. +++");
	}

	@Override
	public int[] loadInBackground() {
		if (DEBUG) Log.i(TAG, "+++ loadInBackground() called +++");

		mIsLoading = true;

		int[] statistics = null;
	   	if (mOnline) {
	   		statistics = SmartlibAPI.getInstance().getStatistics(mSysno);
	    }
	    
	   	if (statistics == null && mOnline) {  //if no internet then no errors
			mHasError = true;
		} else {
			mHasError = false;
		}
	   	
        if (statistics != null) {
            StatisticsHelper s = new StatisticsHelper(statistics);
            DBManager.getInstance(getContext()).updateAverageRating(mSysno, s.getAverageRating());	      	
        }

		return statistics;
	}

	@Override
	public void deliverResult(int[] statistics) {
		mIsLoading = false;
		if (isReset()) {
			if (DEBUG) Log.w(TAG, "+++ Loader delivered result while loader was reseting. +++");
			// The Loader has been reset; ignore the result and invalidate the
			// data.
			onReleaseResources(mStatistics);
			return;
		}
		
		int[] oldStatistics = null;
		
        oldStatistics = mStatistics;
        mStatistics = statistics;
        
		if (isStarted()) {
			if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager[mHasMoreResult: " + mHasMoreResult + ", mHasError: " + mHasError + ", mOnline: " + mOnline + "] +++");
			// If the Loader is in a started state, deliver the results to the
			// client. The superclass method does this for us.
			super.deliverResult(mStatistics == null ?
                    null : mStatistics);
		}

		// Invalidate the old data as we don't need it any more.
		if (oldStatistics != null && oldStatistics != statistics) {
			onReleaseResources(oldStatistics);
		}
	}

	private void onReleaseResources(int[] statistics) {
		// List dont need to release anything
	}

	@Override
	protected void onStartLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

		if (mStatistics != null) {
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
		} else if (mStatistics == null) {
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
		if (mStatistics != null) {
			onReleaseResources(mStatistics);
			mStatistics = null;
		}
		
		// The Loader is being reset, so we should stop monitoring for changes.
	    if (mReceiver != null) {
	    	mReceiver.unregister(getContext());
	    	mReceiver = null;
	    }
	    
	    mHasMoreResult = true;
	    mHasError = false;
	}

	@Override
	public void onCanceled(int[] statistics) {
		if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");
		// Attempt to cancel the current asynchronous load.
		super.onCanceled(statistics);

		// The load has been canceled, so we should release the resources
		// associated with 'data'.
		onReleaseResources(statistics);
	}
	
	@Override
	public void forceLoad() {
	    if (DEBUG) Log.i(TAG, "+++ forceLoad() called! +++");
	    super.forceLoad();
	}

}
