package cz.muni.fi.smartlib;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import cz.muni.fi.smartlib.fragment.BookListFragment;

public class BaseTabsPager extends SherlockFragmentActivity implements OnPageChangeListener {
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	// Menu IDs and selected tab for handling TabsPager workaround
	private ArrayList mMenus;
	private int mSelectedTab = 0;

	/**
	 * Add a actionbar menu ID to the menu list. Menus must be added in the same
	 * order as the fragments they apply to.
	 * 
	 * @param menuId
	 */
	protected void addActionBarMenu(int menuId) {
		mMenus.add(menuId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMenus = new ArrayList();

		setContentView(R.layout.activity_main);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
		// We must handle onPageSelected as a workaround of the TabsPager bug.
		mViewPager.setOnPageChangeListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("index", getSupportActionBar()
				.getSelectedNavigationIndex());
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 */
	@Override
	public void onPageScrollStateChanged(int state) {
		mTabsAdapter.onPageScrollStateChanged(state);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int,
	 *      float, int)
	 */
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		mTabsAdapter.onPageScrolled(position, positionOffset,
				positionOffsetPixels);
	}

	/**
	 * Set the selected tab and invalidate the options menu so it is rebuilt for
	 * the selected tab.
	 * 
	 * @see #onPrepareOptionsMenu for further details.
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
	 */
	@Override
	public void onPageSelected(int position) {
		mSelectedTab = position;
		invalidateOptionsMenu();
		// We have to tell the tabs adapter we're changing pages or the tab
		// indicator won't update.
		// Due to us overriding onPageSelected here in the activity.
		mTabsAdapter.onPageSelected(position);
	}

	/**
	 * Handling this menu creation in the parent activity of a fragment is a
	 * workaround for a race condition issue between the Tabs Pager and the
	 * ActionBar. In addition to this method, onCreateOptionsMenu is overridden
	 * as well as implementing
	 * android.support.v4.view.ViewPager.OnPageChangeListener to use the
	 * onPageSelected method.
	 * 
	 * @see<a 
	 *        href="https://github.com/jakewharton/actionbarsherlock/issues/272">
	 *        ABS #272</a> for details.
	 * @see<a 
	 *        href="https://github.com/JakeWharton/ActionBarSherlock/issues/476">
	 *        ABS #476</a> for workaround clues.
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onPrepareOptionsMenu(com.actionbarsherlock.view.Menu)
	 * @see #onCreateOptionsMenu(Menu)
	 * @see #onPageSelected
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		MenuInflater inflater = getSupportMenuInflater();
		if (mSelectedTab < mMenus.size()) {
			inflater.inflate((Integer) mMenus.get(mSelectedTab), menu);
		}

		return true;
	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public class TabsAdapter extends FragmentPagerAdapter implements
			ViewPager.OnPageChangeListener, ActionBar.TabListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList mTabs = new ArrayList();

		public TabsAdapter(FragmentActivity activity, ActionBar actionBar,
				ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = actionBar;
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class clss) {
			mTabs.add(clss.getName());
			mActionBar.addTab(tab.setTabListener(this));
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
        	switch(position) {
	    		case 0:
	    			return BookListFragment.newInstance(BookListFragment.PARAM_CATEGORY_HISTORY, null);	    			
	    		case 1:
	    			return BookListFragment.newInstance(BookListFragment.PARAM_CATEGORY_TOP, null);	
	    		case 2:
	    			return BookListFragment.newInstance(BookListFragment.PARAM_CATEGORY_RECENTLY_RATED, null);
	    		case 3:
	    			return BookListFragment.newInstance(BookListFragment.PARAM_CATEGORY_FAVOURITES, null);
        	}
        	return null;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}
	}

}