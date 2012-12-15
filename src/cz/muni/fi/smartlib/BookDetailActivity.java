package cz.muni.fi.smartlib;

import java.util.Observable;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.fragment.BookAvailabilityFragment;
import cz.muni.fi.smartlib.fragment.BookDetailFragment;
import cz.muni.fi.smartlib.fragment.BookReviewsFragment;
import cz.muni.fi.smartlib.fragment.LoginDialogFragment;
import cz.muni.fi.smartlib.fragment.RegisterDialogFragment;
import cz.muni.fi.smartlib.fragment.BookAvailabilityFragment.LoadAvailabilityListener;
import cz.muni.fi.smartlib.fragment.BookReviewsFragment.BookReviewsListener;
import cz.muni.fi.smartlib.fragment.BookStatisticsFragment;
import cz.muni.fi.smartlib.fragment.LoginDialogFragment.LoginDialogListener;
import cz.muni.fi.smartlib.fragment.RegisterDialogFragment.RegisterDialogListener;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Utils;
import cz.muni.fi.smartlib.utils.Validator;

public class BookDetailActivity extends SherlockFragmentActivity implements BookDetailFragment.BookUpdateListener, BookStatisticsFragment.LoadStatisticsListener, 
		BookReviewsListener, LoginDialogListener, RegisterDialogListener, LoadAvailabilityListener {
	public static final String TAG = BookDetailActivity.class.getSimpleName();
	public static final boolean DEBUG = true;

	private static final String DETAILS_FRAGMENT = "details";
	private static final String STATISTICS_FRAGMENT = "statistics";
	private static final String REVIEWS_FRAGMENT = "reviews";
	private static final String AVAILABILITY_FRAGMENT = "availability";
	
	FragmentManager mFm;
	RESTServiceHelper mRest;
	Menu mMenu;
	
	Book mBook;
	
	private boolean mOnline;
	private InternetObserver mObserver;
	
    @Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        Intent incoming = getIntent();
        mBook = incoming.getParcelableExtra(RESTServiceHelper.PARAM_BOOK);
                
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        mFm = getSupportFragmentManager();
        FragmentTransaction ft = mFm.beginTransaction();
        BookDetailFragment details = BookDetailFragment.newInstance(mBook);
        ft.add(R.id.fragment_book_detail, details, DETAILS_FRAGMENT);
        BookStatisticsFragment statistics = BookStatisticsFragment.newInstance(mBook.getSysno());
        ft.add(R.id.fragment_book_statistics, statistics, STATISTICS_FRAGMENT);
        BookReviewsFragment reviews = BookReviewsFragment.newInstance(mBook.getSysno());
        ft.add(R.id.fragment_book_reviews, reviews, REVIEWS_FRAGMENT);
        BookAvailabilityFragment availability = BookAvailabilityFragment.newInstance(mBook.getSysno());
        ft.add(R.id.fragment_book_availability, availability, AVAILABILITY_FRAGMENT);
        ft.commit();
        
    }
    
    @Override
    protected void onResume () {
    	mObserver = new InternetObserver(this);
        mOnline = ((SmartLibMU)getApplicationContext()).isOnline();
    	super.onResume();
    }

    @Override
    protected void onPause() {
		if (mObserver != null) {
			mObserver.unregister(this);
			mObserver = null;
		}
    	super.onPause();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.display_book, menu);
        mMenu = menu;
        if (DBManager.getInstance(this).isBookSavedInDB(mBook.getSysno())) {
        	menu.findItem(R.id.menu_details_save).setIcon(R.drawable.content_discard);
        }
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if (Validator.isNullorEmpty(mBook.getPreviewUrl()) || !mOnline) {
			menu.findItem(R.id.menu_details_pdf).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_details_pdf).setEnabled(true);
		}
		menu.findItem(R.id.menu_details_refresh).setEnabled(mOnline);
		
    	return true;
    }
	
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				Utils.startMainActivity(this);
				break;
			case R.id.menu_details_save:
				DBManager db = DBManager.getInstance(this);
				if (!db.isBookSavedInDB(mBook.getSysno())) {
					if (db.insertSavedBook(mBook)) {
						UIUtils.makeToast(this, R.string.toast_add_book_succ);
						item.setIcon(R.drawable.content_discard);
						invalidateOptionsMenu();
						break;
					}
					UIUtils.makeToast(this, R.string.toast_add_book_fail);
				} else {
					if (db.deleteSavedBook(mBook.getSysno())) {
						UIUtils.makeToast(this, R.string.toast_book_deleted_succ);
						item.setIcon(R.drawable.content_save);
						invalidateOptionsMenu();
						break;
					}
					UIUtils.makeWarningToast(this, R.string.toast_book_deleted_fail);
				}
				break;
			case R.id.menu_details_pdf:
				Log.e(TAG, " aaaa " + mBook.getPreviewUrl());
				Uri url = Uri.parse(Utils.repairUrl(mBook.getPreviewUrl()));
				Intent viewIntent = new Intent("android.intent.action.VIEW", url);
				Log.i(TAG, "Starting PDF Preview: " + url.toString());
				startActivity(viewIntent);  
				break;
			case R.id.menu_details_refresh:
				refresh();
				break;
			case R.id.menu_search:
				Utils.startSearchActivity(this);	
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void refresh() {
		if (DEBUG) Log.i(TAG, "--- refresh called ---");
		if (mFm == null) {
			mFm = getSupportFragmentManager();
		}
		Fragment tmp = mFm.findFragmentByTag(AVAILABILITY_FRAGMENT);
		if (tmp != null) ((BookAvailabilityFragment)tmp).refresh();

		tmp = mFm.findFragmentByTag(REVIEWS_FRAGMENT);
		if (tmp != null) ((BookReviewsFragment)tmp).refresh();
		
		tmp = mFm.findFragmentByTag(STATISTICS_FRAGMENT);
		if (tmp != null) ((BookStatisticsFragment)tmp).refresh();
		
	}




	@Override
	public void onAddReview() {
		RESTServiceHelper.getInstance(this).isUserLoggedIn(new IsUserLoggedInReceiver());
	}

	@Override
	public void onRegister() {

		RegisterDialogFragment registerDialog = RegisterDialogFragment.newInstance();
		registerDialog.show(getSupportFragmentManager(), "REGISTER_TAG");	
		
	}
	


	@Override
	public void onRegisterSuccess() {
		UIUtils.makeLongToast(this, R.string.toast_registration_success);
		
	}

	@Override
	public void onRegisterFailed() {
		UIUtils.makeWarningToast(this, R.string.toast_registration_fail);
	}
	
	

	@Override
	public void onLoginSuccess() {
		UIUtils.makeToast(this, R.string.toast_login_succ);
		Utils.startRateActivity(this, mBook.getSysno());
	}

	@Override
	public void onLoginFailed() {
		UIUtils.makeWarningToast(this, R.string.toast_login_fail);
		
	}
	
	public class IsUserLoggedInReceiver extends ResultReceiver {

		private boolean enabled;
		
		public IsUserLoggedInReceiver() {
			super(new Handler());
			this.enabled = true;
		}

		public void unregister() {
			enabled = false;
		}
		
		@Override
	    protected void onReceiveResult (int resultCode, Bundle resultData) {
			if (enabled) {
				boolean result = resultData.getBoolean(RESTServiceHelper.PARAM_RESULT);
				BookReviewsFragment f = (BookReviewsFragment) getSupportFragmentManager().findFragmentByTag(REVIEWS_FRAGMENT);
				if (f != null) {
					f.enableUI();
				}
				if (result) {
					f.enableUI();
					Utils.startRateActivity(BookDetailActivity.this, mBook.getSysno());
				} else {
					LoginDialogFragment loginDialog = LoginDialogFragment.newInstance();
					loginDialog.show(getSupportFragmentManager(), "LOGIN_TAG");					
				}
			} 
	    }
		
	}
	
	public class InternetObserver extends ConnectivityObserver {

		public InternetObserver(Context context) {
			super(context);
		}

		@Override
		public void update(Observable observable, Object data) {
			if (DEBUG) Log.i(TAG, "+++ Connection statues changed to: " + (Boolean)data + " +++");
			mOnline = (Boolean) data;
		}
		
	} 

	
	
}