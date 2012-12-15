package cz.muni.fi.smartlib.loader;

import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.network.SmartlibAPI;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import android.content.Context;
import android.util.Log;

public class BookDetailsLoader extends FancyLoader<Book> {
	
	private static final String TAG = BookDetailsLoader.class.getSimpleName();

	public BookDetailsLoader(Context context, Book book) {
		super(context);
		init(book);
	}

	private ConnectivityObserver mReceiver;
	private Book mBook;
	private Book mInit;
	
	private void init(Book book) {
		mOnline = ((SmartLibMU)getContext().getApplicationContext()).isOnline();
		mHasError = false;
		mIsLoading = true;
		mInit = book;
		mBook = null;
		mReceiver = null;
		mHasMoreResult = true;
		if (DEBUG) Log.i(TAG, "+++ Loader initiated. +++");
	}

	@Override
	public Book loadInBackground() {
		if (DEBUG) Log.i(TAG, "+++ loadInBackground() called +++");

		mIsLoading = true;        
        
        Book book = null;
        DBManager dbManager = DBManager.getInstance(getContext());
        
        if (dbManager.isBookInDB(mInit.getSysno())) {
        	book = dbManager.fetchBook(mInit.getSysno()); 
        } else {
        	if (mOnline) { 
        		book = SmartlibAPI.getInstance().completeBook(mInit);
        	} 
        	
        	if (book == null && mOnline) {
        		mHasError = true;
        	} else {
        		mHasError = false;
        	}
        	
        }
        dbManager.close();
        
        
        

		return book;
	}

	@Override
	public void deliverResult(Book book) {
		mIsLoading = false;
		if (isReset()) {
			if (DEBUG) Log.w(TAG, "+++ Loader delivered result while loader was reseting. +++");
			// The Loader has been reset; ignore the result and invalidate the
			// data.
			onReleaseResources(mBook);
			return;
		}
		
		Book oldBook = null;
		
        oldBook = mBook;
        mBook = book;
        
		if (isStarted()) {
			if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager[mHasMoreResult: " + mHasMoreResult + ", mHasError: " + mHasError + ", mOnline: " + mOnline + "] +++");
			// If the Loader is in a started state, deliver the results to the
			// client. The superclass method does this for us.
			super.deliverResult(mBook == null ?
                    null : mBook);
		}

		// Invalidate the old data as we don't need it any more.
		if (oldBook != null && oldBook != book) {
			onReleaseResources(oldBook);
		}
	}

	private void onReleaseResources(Book book) {
		// List dont need to release anything
	}

	@Override
	protected void onStartLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

		if (mBook != null) {
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
		} else if (mBook == null) {
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
		if (mBook != null) {
			onReleaseResources(mBook);
			mBook = null;
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
	public void onCanceled(Book book) {
		if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");
		// Attempt to cancel the current asynchronous load.
		super.onCanceled(book);

		// The load has been canceled, so we should release the resources
		// associated with 'data'.
		onReleaseResources(book);
	}
	
	@Override
	public void forceLoad() {
	    if (DEBUG) Log.i(TAG, "+++ forceLoad() called! +++");
	    super.forceLoad();
	}

}

