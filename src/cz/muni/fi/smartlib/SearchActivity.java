package cz.muni.fi.smartlib;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.Intents.Scan;

import cz.muni.fi.smartlib.fragment.BookListFragment;
import cz.muni.fi.smartlib.fragment.ScanLoadingDialog;
import cz.muni.fi.smartlib.fragment.SearchFormFragment;
import cz.muni.fi.smartlib.fragment.ScanLoadingDialog.ScanDialogListener;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Utils;
import cz.muni.fi.smartlib.utils.Validator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class SearchActivity extends SherlockFragmentActivity implements BookListFragment.BookListListener, SearchFormFragment.SearchBookListener, ScanDialogListener {
	public static final String TAG = SearchActivity.class.getSimpleName();
	
	private FragmentManager mFragmentManager;
	
	private boolean mOnline;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        mFragmentManager = getSupportFragmentManager();
        
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        Fragment searchFormFrag = SearchFormFragment.newInstance();
        ft.add(R.id.search_activity_fragment_container, searchFormFrag);
        ft.commit();
        Log.i(TAG, "Starting fragment: " + SearchFormFragment.class.getName());
        
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				Utils.startMainActivity(this);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
    
	
	
	@Override
	public void onBookClicked(String sysno) {
		Intent i = new Intent(this, BookDetailActivity.class);
    	i.putExtra(RESTServiceHelper.PARAM_SYSNO, sysno);
    	startActivity(i);
	}

	@Override
	public void onSearchStarted(String query) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
        BookListFragment bookListFragment = BookListFragment.newInstance(BookListFragment.PARAM_CATEGORY_QUERY, query);
        ft.replace(R.id.search_activity_fragment_container, bookListFragment);
        ft.addToBackStack(null);
        ft.commit();
	}


	@Override
	public void onSearchBtnClicked() {
		Utils.startSearchActivity(this);	
	}


	@Override
	public void onScanBtnClicked() {

		Intent intent = new Intent("cz.muni.fi.smartlib.com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_FORMATS", BarcodeFormat.EAN_13 + "," + BarcodeFormat.CODE_39);
		startActivityForResult(intent, 0);
		
	}
	
	private String mContents = null;
	private String mFormat = null;
	private boolean mReturnedFromScan = false;
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				mContents = intent.getStringExtra(Scan.RESULT);
				mFormat = intent.getStringExtra(Scan.RESULT_FORMAT);
				mReturnedFromScan = true;
				Log.i("BARCODE", "Barcode ["+ mFormat +"]: " + mContents);
			}
		}
	}
	
	@Override
	public void onResume () {
		super.onResume();
		if (mReturnedFromScan) {
			mReturnedFromScan = false;
			if (!Validator.isNullorEmpty(mContents) && !Validator.isNullorEmpty(mFormat)) {
				ScanLoadingDialog f = ScanLoadingDialog.newInstance(mContents, mFormat);
				f.show(getSupportFragmentManager(), "SCAN_DIALOG");		
			}
		}
		
	}




	@Override
	public void onScanClicked() {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_FORMATS", BarcodeFormat.EAN_13 + "," + BarcodeFormat.CODE_39);
		startActivityForResult(intent, 0);
		
	}


	@Override
	public void onBookFound(Book book) {
		Utils.startBookDetails(this, book);
	}


	@Override
	public void onBookNotFound() {
		
		
	}
	
}
