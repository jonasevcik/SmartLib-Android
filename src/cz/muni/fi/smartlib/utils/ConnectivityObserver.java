package cz.muni.fi.smartlib.utils;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.util.Log;
import cz.muni.fi.smartlib.SmartLibMU;


/**
 * 
 * Abstract class which listen to connectivity changes. 
 * 
 * */
public abstract class ConnectivityObserver implements Observer {
	public static final String TAG = ConnectivityObserver.class.getSimpleName();
	
	public ConnectivityObserver(Context context) {
		try {
			((SmartLibMU)context.getApplicationContext()).registerConnObserver(this);
		} catch (ClassCastException e) {
			Log.e(TAG, "+++ Cannot register ConnectivityObserver: " + e + " +++");
		}
	}
	
	public void unregister(Context context) {
		try {
			((SmartLibMU)context.getApplicationContext()).unregisterConnObserver(this);
		} catch (ClassCastException e) {
			Log.e(TAG, "+++ Cannot unregister ConnectivityObserver: " + e + " +++");
		}
	}

	/**
	 *  When connectivity changes, this method is called. 
	 * */
	@Override
	public abstract void update(Observable observable, Object data);

}
