package cz.muni.fi.smartlib.loader;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.loader.BooksDbLoader.BookDbBroadcastReceiver;
import cz.muni.fi.smartlib.model.Book;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class SearchQueryLoader extends AsyncTaskLoader<String[]> {
	public static final String TAG = SearchQueryLoader.class.getSimpleName();
	public static final boolean DEBUG = true;
	
	private BroadcastReceiver mReceiver;
	private String[] mQueries;

	
	
	public SearchQueryLoader(Context context) {
		super(context);
		init();
	} 
	
	private void init() {
		mQueries = null;
		mReceiver = null;
		if (DEBUG) Log.i(TAG, "+++ SearchQueryLoader initiated. +++");
	}

	@Override
	public String[] loadInBackground() {
		if (DEBUG) Log.i(TAG, "+++ loadInBackground() called +++");

		DBManager dbManager = DBManager.getInstance(getContext());
		String[] queries = null;

		queries = dbManager.getAllSearchQueries();
		
		return queries;
	}

	@Override
	public void deliverResult(String[] queries) {
		if (isReset()) {
			if (DEBUG) Log.w(TAG, "+++ SearchQueryLoader delivered result while loader was reseting. +++");
			onReleaseResources(queries);
			return;
		}

		String[] oldQueries = mQueries;
		mQueries = queries;

		if (isStarted()) {
			if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager. +++");
			super.deliverResult(mQueries);
		}

		// Invalidate the old data as we don't need it any more.
		if (oldQueries != null && oldQueries != queries) {
			onReleaseResources(oldQueries);
		}
	}

	private void onReleaseResources(String[] data) {
		// List dont need to release anything
	}

	@Override
	protected void onStartLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

//		if (mQueries != null) {
//			if (DEBUG) Log.i(TAG, "+++ Delivering previously loaded data to the client...");
//			deliverResult(mQueries);
//		}

		// Register the receivers that will notify the Loader when changes are
		// made.
		if (mReceiver == null) {
			mReceiver = new SearchQueryBroadcastReceiver();
		}

		if (takeContentChanged()) {
			if (DEBUG) Log.i(TAG, "+++ A content change has been detected... so force load! +++");
			forceLoad();
		} else {
			if (DEBUG) Log.i(TAG, "+++ The current data is data is null... so force load! +++");
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStopLoading() called! +++");
		cancelLoad();

	}

	@Override
	protected void onReset() {
		if (DEBUG) Log.i(TAG, "+++ onReset() called! +++");
		onStopLoading();

		if (mQueries != null) {
			onReleaseResources(mQueries);
			mQueries = null;
		}
		
		if (mReceiver != null) {
	      getContext().unregisterReceiver(mReceiver);
	      mReceiver = null;
	    }
		
	}

	@Override
	public void onCanceled(String[] queries) {
		if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");
		super.onCanceled(queries);
		onReleaseResources(queries);
	}
	
	@Override
	public void forceLoad() {
	    if (DEBUG) Log.i(TAG, "+++ forceLoad() called! +++");
	    super.forceLoad();
	}

	public class SearchQueryBroadcastReceiver extends BroadcastReceiver {
		public static final String ACTION_SEARCH_QUERIES_CHANGED = "cz.muni.fi.smartlib.action.search.queries.changed";
		
		public SearchQueryBroadcastReceiver() {
			// Register for events related to changes in books db.
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_SEARCH_QUERIES_CHANGED);
			getContext().registerReceiver(this, filter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG) Log.i(TAG, "+++ The observer has detected an db change! Action: " + intent.getAction() + " +++");
			
			if (intent != null) {
				String action = intent.getAction();
				if (action.equals(ACTION_SEARCH_QUERIES_CHANGED)) {
					onContentChanged();
					return;
				}
			
			}
		}
	
	}
}