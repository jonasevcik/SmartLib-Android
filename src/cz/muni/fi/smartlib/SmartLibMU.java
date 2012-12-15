package cz.muni.fi.smartlib;

import java.util.Observable;
import java.util.Observer;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.ReportingInteractionMode;

import com.loopj.android.http.PersistentCookieStore;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import cz.muni.fi.smartlib.cache.FileSystemPersistence;
import cz.muni.fi.smartlib.cache.HttpImageManager;
import cz.muni.fi.smartlib.cache.ResizeFilter;


@ReportsCrashes(formKey = "dDdrSWFaZlVtQmU0TkIzYklSTllyNnc6MQ",

				mode = ReportingInteractionMode.DIALOG,
				   resDialogText = R.string.crash_dialog_text,
				   resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
				   resDialogOkToast = R.string.crash_dialog_ok_toast) // optional. displays a Toast message when the user accepts to send a report.

public class SmartLibMU extends Application {
	public static final String TAG = SmartLibMU.class.getSimpleName();
	
	public static final String BASEDIR = Environment.getExternalStorageDirectory() + "/SmartLib/cache";
	
	private HttpImageManager mHttpImageManager;
	private ResizeFilter mFilter;
	
	private static SmartLibMU mAppContext;
	
	private static PersistentCookieStore mCookieStore;
	
	private ConnectivityBroadcastReceiver mConnReceiver;
	
	public SmartLibMU () {
		mAppContext = this;
	}
	
	@Override 
	public void onCreate() {
		mConnReceiver = new ConnectivityBroadcastReceiver();
		
		mHttpImageManager = new HttpImageManager(HttpImageManager.createDefaultMemoryCache(), new FileSystemPersistence(BASEDIR));
		mFilter = new ResizeFilter(this);
		mHttpImageManager.setBitmapFilter(mFilter);
		ACRA.init(this);	
		mCookieStore = new PersistentCookieStore(this);
		
		
	    super.onCreate();
	 }
	
	public static Context getAppContext() {
		return mAppContext;
	}
	
	public boolean isOnline() {
		if (mConnReceiver != null) {
			return mConnReceiver.isOnline();
		}
		return false;
	}

	public HttpImageManager getHttpImageManager() {
		return mHttpImageManager;
	}
	
	public ResizeFilter getResizeFilter() {
		return mFilter;
	}
	
	public static PersistentCookieStore getCookieStore () {
		return mCookieStore;
	}

	public void registerConnObserver(Observer observer) {
		if (mConnReceiver != null) {
			mConnReceiver.registerConnObserver(observer);
		}
	}
	
	public void unregisterConnObserver(Observer observer) {
		if (mConnReceiver != null) {
			mConnReceiver.unregisterConnObserver(observer);
		}
	}
	
	//Listen to connectivity changes and reports that changes to registered observers
	public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
		static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
		
		private ConnectivityObservable mConnObservable;
		private boolean mOnline;
		
		public ConnectivityBroadcastReceiver() {
			//mOnline = StateChecker.isOnline(SmartLibMU.this);
			IntentFilter filter = new IntentFilter(ACTION);
		    registerReceiver(this, filter);
		    mConnObservable = new ConnectivityObservable();
		}
		
		@Override
	    public void onReceive(Context context, Intent intent) {
	        boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
	        String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
	        boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
	        NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
	        NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
	        mOnline = !noConnectivity;
	        Log.i(TAG, "Status : " + mOnline + ", Reason :" + reason + ", FailOver :" + isFailover + ", Current Network Info : " + currentNetworkInfo + ", OtherNetwork Info :" + otherNetworkInfo);
	        mConnObservable.notifyConnectivity(mOnline);
		}

		public void registerConnObserver(Observer observer) {
			mConnObservable.addObserver(observer);
		}
		
		public void unregisterConnObserver(Observer observer) {
			mConnObservable.deleteObserver(observer);
		}
		
		public boolean isOnline() {
			return mOnline;
		}
		
	}
	
	public class ConnectivityObservable extends Observable {
		public void notifyConnectivity(boolean online) {
			setChanged();
			notifyObservers(Boolean.valueOf(online));
		}
	}

	
}
