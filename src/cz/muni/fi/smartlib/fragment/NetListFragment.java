package cz.muni.fi.smartlib.fragment;

import java.util.Observable;

import android.app.Activity;
import com.actionbarsherlock.app.SherlockListFragment;

import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;

public abstract class NetListFragment extends SherlockListFragment {
	
	private ConnectivityObserver mObserver;
	protected boolean mOnline;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mObserver = new ConnectivityObserver(getActivity()) {

			@Override
			public void update(Observable observable, Object data) {
				mOnline = (Boolean) data;	
				onConnectivityChanged(mOnline);
			}
        	
        };
        mOnline = ((SmartLibMU)getActivity().getApplicationContext()).isOnline();
    }	
	
	@Override
	public void onDetach() {
		if (mObserver != null) {
			mObserver.unregister(getActivity());
			mObserver = null;
		}
		super.onDetach();
	}
	
	protected abstract void onConnectivityChanged(boolean isConnected);

}
