package cz.muni.fi.smartlib.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.model.Review;
import cz.muni.fi.smartlib.network.SmartlibAPI;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;

public class ReviewsLoader extends FancyLoader<List<Review>> {
	private static final String TAG = ReviewsLoader.class.getSimpleName();

	private static final int MAX_RESULTS = 5;
	
	public ReviewsLoader(Context context, String sysno) {
		super(context);
		init(sysno);
	}

	private String mSysno;
	private ConnectivityObserver mReceiver;
	private List<Review> mReviews;
	
	private void init(String sysno) {
		mSysno = sysno;
		mOnline = ((SmartLibMU)getContext().getApplicationContext()).isOnline();
		mHasError = false;
		mIsLoading = true;
		mReviews = null;
		mReceiver = null;
		mHasMoreResult = true;
		if (DEBUG) Log.i(TAG, "+++ Loader initiated. +++");
	}

	@Override
	public List<Review> loadInBackground() {
		if (DEBUG) Log.i(TAG, "+++ loadInBackground() called +++");

		mIsLoading = true;

		List<Review> reviews = null;
	   	if (mOnline) {
	   		reviews = SmartlibAPI.getInstance().getReviews(mSysno, MAX_RESULTS, 0);
	    }
	    
	   	if (reviews == null && mOnline) {  //if no internet then no errors
			mHasError = true;
		} else {
			mHasError = false;
		}
		return reviews;
	}

	@Override
	public void deliverResult(List<Review> reviews) {
		mIsLoading = false;
		if (isReset()) {
			if (DEBUG) Log.w(TAG, "+++ Loader delivered result while loader was reseting. +++");
			// The Loader has been reset; ignore the result and invalidate the
			// data.
			onReleaseResources(mReviews);
			return;
		}
		
		List<Review> oldReviews = null;
		
        oldReviews = mReviews;
        mReviews = reviews;
        
		if (isStarted()) {
			if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager[mHasMoreResult: " + mHasMoreResult + ", mHasError: " + mHasError + ", mOnline: " + mOnline + "] +++");
			// If the Loader is in a started state, deliver the results to the
			// client. The superclass method does this for us.
			super.deliverResult(mReviews == null ?
                    null : new ArrayList<Review>(mReviews));
		}

		// Invalidate the old data as we don't need it any more.
		if (oldReviews != null && oldReviews != reviews) {
			onReleaseResources(oldReviews);
		}
	}

	private void onReleaseResources(List<Review> reviews) {
		// List dont need to release anything
	}

	@Override
	protected void onStartLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

		if (mReviews != null) {
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
		} else if (mReviews == null) {
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
		if (mReviews != null) {
			onReleaseResources(mReviews);
			mReviews = null;
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
	public void onCanceled(List<Review> reviews) {
		if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");
		// Attempt to cancel the current asynchronous load.
		super.onCanceled(reviews);

		// The load has been canceled, so we should release the resources
		// associated with 'data'.
		onReleaseResources(reviews);
	}
	
	@Override
	public void forceLoad() {
	    if (DEBUG) Log.i(TAG, "+++ forceLoad() called! +++");
	    super.forceLoad();
	}

}
