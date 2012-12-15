package cz.muni.fi.smartlib.loader;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.model.Book;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BooksDbLoader extends FancyLoader<List<Book>> {

	private static final String TAG = BooksDbLoader.class.getSimpleName();

	public static final int BOOKS_DB_LOADER_TYPE_VIEWED = 0;
	public static final int BOOKS_DB_LOADER_TYPE_SAVED = 1;

	private int mLoaderType;
	private BroadcastReceiver mReceiver;
	protected List<Book> mBooks;

	public BooksDbLoader(Context context, int loaderType) {
		super(context);
		init(loaderType);
	}

	private void init(int loaderType) {
		mLoaderType = loaderType;
		mHasError = false;
		mIsLoading = true;
		mBooks = null;
		mReceiver = null;
		if (DEBUG)
			Log.i(TAG, "+++ Loader [" + mLoaderType + "] initiated. +++");
	}

	@Override
	public List<Book> loadInBackground() {
		if (DEBUG)
			Log.i(TAG, "+++ loadInBackground(" + mLoaderType + ") called +++");

		mIsLoading = true;

		DBManager dbManager = DBManager.getInstance(getContext());
		List<Book> books = null;

		switch (mLoaderType) {
		case BOOKS_DB_LOADER_TYPE_SAVED:
			books = dbManager.fetchSavedBooksList();
			break;
		case BOOKS_DB_LOADER_TYPE_VIEWED:
			books = dbManager.fetchLatestBooksList();
			break;
		}

		mHasError = (books == null)? true : false;
		
//		if (books == null || books.isEmpty()) {
//			return null;
//		}
		
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

		// Hold a reference to the old data so it doesn't get garbage collected.
		// The old data may still be in use (i.e. bound to an adapter, etc.), so
		// we must protect it until the new data has been delivered.
		List<Book> oldBooks = mBooks;
		mBooks = books;

		if (isStarted()) {
			if (DEBUG)
				Log.i(TAG, "+++ Delivering results to the LoaderManager. +++");
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
			deliverResult(mBooks);
		}

		// Register the receivers that will notify the Loader when changes are
		// made.
		if (mReceiver == null) {
			mReceiver = new BookDbBroadcastReceiver();
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
	      getContext().unregisterReceiver(mReceiver);
	      mReceiver = null;
	    }
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
	    super.forceLoad();
	}

	public class BookDbBroadcastReceiver extends BroadcastReceiver {
		public static final String ACTION_VIEWED_BOOKS_CHANGED = "cz.muni.fi.smartlib.action.viewed.books.changed";
		public static final String ACTION_SAVED_BOOKS_CHANGED = "cz.muni.fi.smartlib.action.saved.books.changed";

		public BookDbBroadcastReceiver() {
			// Register for events related to changes in books db.
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_SAVED_BOOKS_CHANGED);
			filter.addAction(ACTION_VIEWED_BOOKS_CHANGED);
			getContext().registerReceiver(this, filter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG) Log.i(TAG, "+++ The observer has detected an db change! Action: " + intent.getAction() + " +++");
			
			if (intent != null) {
				String action = intent.getAction();
				if (action.equals(ACTION_SAVED_BOOKS_CHANGED) && mLoaderType == BOOKS_DB_LOADER_TYPE_SAVED) {
					onContentChanged();
					return;
				}
				if (action.equals(ACTION_VIEWED_BOOKS_CHANGED) && mLoaderType == BOOKS_DB_LOADER_TYPE_VIEWED) {
					onContentChanged();
				}
			}
			
		}
	}

}
