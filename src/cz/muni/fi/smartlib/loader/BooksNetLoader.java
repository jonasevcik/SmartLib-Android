package cz.muni.fi.smartlib.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.network.SmartlibAPI;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import cz.muni.fi.smartlib.utils.Validator;

public class BooksNetLoader extends FancyLoader<List<Book>> {
	private static final String TAG = BooksNetLoader.class.getSimpleName();
	
	public interface OnLoadListener {
		public void onStartLoading();
	}
	
	private OnLoadListener mLoadingListener;
	
	public void registerLoadStatusListener(OnLoadListener listener) {
		mLoadingListener = listener;
	}
	
	public void unregisterLoadStatusListener() {
		mLoadingListener = null;
	}

	private static final int MAX_RESULTS_IN_LIST = 50;
	private static final int MAX_RESULTS_PER_REQUEST = 10;
	
	public static final int BOOKS_NET_LOADER_TYPE_TOP = 0;
	public static final int BOOKS_NET_LOADER_TYPE_RECENTLY_RATED = 1;
	public static final int BOOKS_NET_LOADER_TYPE_SEARCH = 2;
	
	private int mLoaderType;
	private String mQuery;
	private int mOffset;
	private ConnectivityObserver mReceiver;
	private List<Book> mBooks;

	public static BooksNetLoader newTopLoader(Context context) {
		return new BooksNetLoader(context, BOOKS_NET_LOADER_TYPE_TOP, null);
	}
	
	public static BooksNetLoader newRecentlyRatedLoader(Context context) {		
		return new BooksNetLoader(context, BOOKS_NET_LOADER_TYPE_RECENTLY_RATED, null);
	}
		
	public static BooksNetLoader newQueryLoader(Context context, String query) {
		return new BooksNetLoader(context, BOOKS_NET_LOADER_TYPE_SEARCH, (Validator.isNullorEmpty(query))? "" : query);
	}
	
	private BooksNetLoader(Context context, int loaderType, String query) {
		super(context);
		init(loaderType, query);
	}

	private void init(int loaderType, String query) {
		mLoaderType = loaderType;
		mOnline = ((SmartLibMU)getContext().getApplicationContext()).isOnline();
		mHasError = false;
		mIsLoading = true;
		mBooks = null;
		mReceiver = null;
		mHasMoreResult = true;
		mOffset = 0;
		if (mLoaderType == BOOKS_NET_LOADER_TYPE_SEARCH) {
			mQuery = query;
		}
		if (DEBUG) Log.i(TAG, "+++ Loader [" + mLoaderType + "] initiated. +++");
	}

	@Override
	public List<Book> loadInBackground() {
		if (DEBUG) Log.i(TAG, "+++ loadInBackground(" + mLoaderType + ") called +++");

		mIsLoading = true;
		mHasError = false;

		List<Book> books = null;

		switch (mLoaderType) {
	    	case BOOKS_NET_LOADER_TYPE_SEARCH:
	    		if (mOnline) {
	        		books = SmartlibAPI.getInstance().searchBooks(mQuery, MAX_RESULTS_PER_REQUEST, mOffset);
	    		}
	    		break;
	    	case BOOKS_NET_LOADER_TYPE_RECENTLY_RATED:
	    		if (mOnline) {
	    			books = SmartlibAPI.getInstance().getRecentlyRatedBooks(MAX_RESULTS_PER_REQUEST, mOffset);
	    		}
	    		break;
	    	case BOOKS_NET_LOADER_TYPE_TOP:
	    		if (mOnline) {
	    			books = SmartlibAPI.getInstance().getTopBooks(MAX_RESULTS_PER_REQUEST, mOffset);
	    		}
	    		break;
		}
		if (books == null && mOnline) {  //if no internet then no errors
			mHasError = true;
		} else {
			mHasError = false;
		}

		return books;
	}

	@Override
	public void deliverResult(List<Book> books) {
		mIsLoading = false;
		if (isReset()) {
			if (DEBUG) Log.w(TAG, "+++ Loader [" + mLoaderType + "] delivered result while loader was reseting. +++");
			// The Loader has been reset; ignore the result and invalidate the
			// data.
			onReleaseResources(mBooks);
			return;
		}
		
		List<Book> oldBooks = null;
		
        if (books != null) {
            if (mBooks == null) {
            	// Hold a reference to the old data so it doesn't get garbage collected.
        		// The old data may still be in use (i.e. bound to an adapter, etc.), so
        		// we must protect it until the new data has been delivered.
            	oldBooks = mBooks;
        		mBooks = books;
                mOffset += books.size();
            } else {
                mBooks.addAll(books);
                mOffset += books.size();                
            }
        	if (books.size() < MAX_RESULTS_PER_REQUEST) {
        		mHasMoreResult = false;
        	}
        }
        if (mOffset >= MAX_RESULTS_IN_LIST) {
        	mHasMoreResult = false;
        }

		if (isStarted()) {
			if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager[mHasMoreResult: " + mHasMoreResult + ", mHasError: " + mHasError + ", mOffset: " + mOffset + ", mOnline: " + mOnline + "] +++");
			// If the Loader is in a started state, deliver the results to the
			// client. The superclass method does this for us.
			super.deliverResult(mBooks == null ?
                    null : new ArrayList<Book>(mBooks));
		}

		// Invalidate the old data as we don't need it any more.
		if (oldBooks != null && oldBooks != books) {
			onReleaseResources(oldBooks);
		}
	}

	private void onReleaseResources(List<Book> data) {
		// List dont need to release anything
	}

	@Override
	protected void onStartLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStartLoading(" + mLoaderType + ") called! +++");
		
		if (mBooks != null) {
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
		} else if (mBooks == null) {
			// If the current data is null... then we should load data!
			if (DEBUG) Log.i(TAG, "+++ The current data is data is null... so force load! +++");
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStopLoading(" + mLoaderType + ") called! +++");
		mIsLoading = false;
		// The Loader is in a stopped state, so we should attempt to cancel the
		// current load (if there is one).
		cancelLoad();

	}

	@Override
	protected void onReset() {
		if (DEBUG) Log.i(TAG, "+++ onReset(" + mLoaderType + ") called! +++");
		// Ensure the loader has been stopped.
		onStopLoading();

		// At this point we can release the resources associated with 'mData'.
		if (mBooks != null) {
			onReleaseResources(mBooks);
			mBooks = null;
		}
		
		// The Loader is being reset, so we should stop monitoring for changes.
	    if (mReceiver != null) {
	    	mReceiver.unregister(getContext());
	    	mReceiver = null;
	    }
	    
	    mHasMoreResult = true;
	    mHasError = false;
		mOffset = 0;
		
	}

	@Override
	public void onCanceled(List<Book> books) {
		if (DEBUG) Log.i(TAG, "+++ onCanceled(" + mLoaderType + ") called! +++");
		// Attempt to cancel the current asynchronous load.
		super.onCanceled(books);

		// The load has been canceled, so we should release the resources
		// associated with 'data'.
		onReleaseResources(books);
	}
	
	@Override
	public void forceLoad() {
	    if (DEBUG) Log.i(TAG, "+++ forceLoad(" + mLoaderType + ") called! +++");
	    if (mLoadingListener != null) {
			mLoadingListener.onStartLoading();
		}
	    super.forceLoad();
	}
}