package cz.muni.fi.smartlib.loader;

import java.util.List;
import java.util.Observable;

import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

public class FancyLoader<T> extends AsyncTaskLoader<T>{
	public static final boolean DEBUG = true;
	
	public static final int BOOKS_LOADER_DB = 0;
	public static final int BOOKS_LOADER_NET = 1;
	public static final int AVAILABILITY_LOADER_NET = 2;
	public static final int REVIEWS_LOADER_NET = 3;
	public static final int DETAILS_LOADER_NET = 4;
	public static final int STATISTICS_LOADER_NET = 5;
	
	protected boolean mHasError;
	protected boolean mIsLoading;
	protected boolean mOnline;
	protected boolean mHasMoreResult;
	
	public FancyLoader(Context context) {
		super(context);
	}

	@Override
	public T loadInBackground() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isLoading() {
		return mIsLoading;
	}

	public boolean hasError() {
		return mHasError;
	}

	public boolean isOnline() {
		return mOnline;
	}
	
	public boolean hasMoreResult() {
		return mHasMoreResult;
	}

	public static boolean isLoading (Fragment context, int loaderId) {
		if (context.isAdded()) {
			final Loader loader = context.getLoaderManager().getLoader(loaderId);
			if (loader != null) {
				return ((FancyLoader)loader).isLoading();
			}
		}
		return false;
	}

	public static boolean loadingHasError(Fragment context, int loaderId) {
		if (context.isAdded()) {
			final Loader loader = context.getLoaderManager().getLoader(loaderId);
			if (loader != null) {
				return ((FancyLoader) loader).hasError();
			}
		}
		return false;
	}
	
	public static boolean isOnline(Fragment context, int loaderId) {
		if (loaderId == BOOKS_LOADER_DB) {
			return true;
		}
		if (context.isAdded()) {
			final Loader loader = context.getLoaderManager().getLoader(loaderId);
			if (loader != null) {
				return ((FancyLoader) loader).isOnline();
			}
		}
		return false;
	}
	
	public static boolean hasMoreResult(Fragment context, int loaderId) {
		if (loaderId != BOOKS_LOADER_NET) {
			return false;
		}
		if (context.isAdded()) {
			final Loader loader = context.getLoaderManager().getLoader(loaderId);
			if (loader != null) {
				return ((FancyLoader) loader).hasMoreResult();
			}
		}
		return false;
	}

	protected class NetConnectivityObserver extends ConnectivityObserver {

		public NetConnectivityObserver(Context context) {
			super(context);
		}

		@Override
		public void update(Observable observable, Object data) {
			if (DEBUG) Log.i(TAG, "+++ Connection statues changed to: " + (Boolean)data + " +++");
			if (!mOnline) {
				mOnline = (Boolean) data;
				onContentChanged();
			} else {
				mOnline = (Boolean) data;
			}
		}
	}
}
