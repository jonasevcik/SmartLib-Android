package cz.muni.fi.smartlib;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.Intents.Scan;

import cz.muni.fi.smartlib.fragment.BookListFragment;
import cz.muni.fi.smartlib.fragment.BookListFragment.BookListListener;
import cz.muni.fi.smartlib.fragment.BookListFragment.LoadingListener;
import cz.muni.fi.smartlib.fragment.ScanLoadingDialog;
import cz.muni.fi.smartlib.fragment.ScanLoadingDialog.ScanDialogListener;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.network.HttpHelper;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Utils;
import cz.muni.fi.smartlib.utils.Validator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends BaseTabsPager implements BookListListener, ScanDialogListener {
	public static final String TAG = MainActivity.class.getSimpleName();
	
	private boolean mOnline;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        HttpHelper.getInstance().deleteCookies();
        
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin_width));
        ActionBar actionBar = getSupportActionBar();
       
        mTabsAdapter.addTab(actionBar.newTab()
                .setText(R.string.history), BookListFragment.class);
        mTabsAdapter.addTab(actionBar.newTab()
                .setText(R.string.top), BookListFragment.class);
        mTabsAdapter.addTab(actionBar.newTab()
                .setText(R.string.lately_rated), BookListFragment.class);
        mTabsAdapter.addTab(actionBar.newTab()
                .setText(R.string.favourites), BookListFragment.class);

        addActionBarMenu(R.menu.home);
        addActionBarMenu(R.menu.home);
        addActionBarMenu(R.menu.home);
        addActionBarMenu(R.menu.home);
    
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public void onBookClicked(String sysno) {
		// TODO Auto-generated method stub
		
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
	public void onBookFound(Book book) {
		Utils.startBookDetails(this, book);
	}


	@Override
	public void onBookNotFound() {
		
		
	}

}