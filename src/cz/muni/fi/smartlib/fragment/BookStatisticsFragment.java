package cz.muni.fi.smartlib.fragment;

import java.math.BigDecimal;
import java.util.Observable;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.loader.FancyLoader;
import cz.muni.fi.smartlib.loader.StatisticsLoader;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import cz.muni.fi.smartlib.utils.StatisticsHelper;

public class BookStatisticsFragment extends NetFragment implements LoaderCallbacks<int[]> {
	public static final String TAG = BookStatisticsFragment.class.getSimpleName();
	public static final boolean DEBUG = true;
	
	public interface LoadStatisticsListener {
    }
	
	private LoadStatisticsListener mListener;

	private int[] mStatistics;
	private String mSysno;
	
	private ProgressBar mProgressBar;
	
	public static BookStatisticsFragment newInstance(String sysno) {
		BookStatisticsFragment f = new BookStatisticsFragment();
	    Bundle args = new Bundle();
	    args.putString(RESTServiceHelper.PARAM_SYSNO, sysno);
	    f.setArguments(args);
	    return f;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LoadStatisticsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoadStatisticsListener");
        }
    }
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		
		if (args != null) {
			mSysno = args.getString(RESTServiceHelper.PARAM_SYSNO);
		}
		
		mStatistics = new int[5];
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_book_statistics, container, false);
		
		mProgressBar = (ProgressBar) view.findViewById(R.id.display_book_progress);

		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//mListener.onLoadStatistics(new Book());
		
		Bundle bundle = new Bundle();
        bundle.putString(RESTServiceHelper.PARAM_SYSNO, mSysno);
		
		getLoaderManager().initLoader(FancyLoader.STATISTICS_LOADER_NET, bundle, this);
	}
	

	@Override
	public Loader<int[]> onCreateLoader(int loader, Bundle bundle) {
		if (DEBUG) Log.i(TAG, "+++ onCreateLoader() called +++");
	
		mProgressBar.setVisibility(View.VISIBLE);
		
		String sysno = null;
		if (bundle != null) {
			sysno = bundle.getString(RESTServiceHelper.PARAM_SYSNO);
		}
		return new StatisticsLoader(getActivity(), sysno);
	}

	@Override
	public void onLoadFinished(Loader<int[]> loader, int[] statistics) {
		if (DEBUG) Log.i(TAG, "+++ onLoadFinished() called +++");
		mProgressBar.setVisibility(View.GONE);
    	
		if (statistics != null && !FancyLoader.loadingHasError(this, FancyLoader.STATISTICS_LOADER_NET)) {
			mStatistics = statistics;
        	setStatistics(mStatistics);
		} else {
			//TODO: Error
		}
	}

	@Override
	public void onLoaderReset(Loader<int[]> loader) {
    	if (DEBUG) Log.i(TAG, "+++ onLoaderRestart() called +++");
    	mProgressBar.setVisibility(View.VISIBLE);
    	mStatistics = new int[5];
    	setStatistics(mStatistics);
	}
	
	public void refresh() {
		if (DEBUG) Log.i(TAG, "+++ refresh() called +++");
		Bundle bundle = new Bundle();
        bundle.putString(RESTServiceHelper.PARAM_SYSNO, mSysno);
		
		getLoaderManager().restartLoader(FancyLoader.STATISTICS_LOADER_NET, bundle, this);	
	}
		
	private void setStatistics(int[] statistics) {
		StatisticsHelper stat = new StatisticsHelper(statistics);
		Activity a = getActivity();
		
		TextView averageRating = (TextView) a.findViewById(R.id.display_book_average_rating);
		TextView ratingCount = (TextView) a.findViewById(R.id.display_book_rating_count);
		
		TextView oneStarBar = (TextView) a.findViewById(R.id.display_book_1stars_bar);
		TextView twoStarsBar = (TextView) a.findViewById(R.id.display_book_2stars_bar);
		TextView threeStarsBar = (TextView) a.findViewById(R.id.display_book_3stars_bar);
		TextView fourStarsBar = (TextView) a.findViewById(R.id.display_book_4stars_bar);
		TextView fiveStarsBar = (TextView) a.findViewById(R.id.display_book_5stars_bar);
		
		TextView oneStarCount = (TextView) a.findViewById(R.id.display_book_1stars_count);
		TextView twoStarsCount = (TextView) a.findViewById(R.id.display_book_2stars_count);
		TextView threeStarsCount = (TextView) a.findViewById(R.id.display_book_3stars_count);
		TextView fourStarsCount = (TextView) a.findViewById(R.id.display_book_4stars_count);
		TextView fiveStarsCount = (TextView) a.findViewById(R.id.display_book_5stars_count);
		
		BigDecimal bd = new BigDecimal(stat.getAverageRating());
	    BigDecimal rounded = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
	    		
		if (averageRating != null) averageRating.setText(String.valueOf(rounded.doubleValue()));
		if (ratingCount != null) ratingCount.setText(String.valueOf(stat.getOveralRatingsCount()));

		Resources r = getResources();
		
		if (oneStarBar != null) oneStarBar.setWidth(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stat.getWidthForView(StatisticsHelper.RATING_STARS_ONE_COUNT), r.getDisplayMetrics())));
		if (twoStarsBar != null) twoStarsBar.setWidth(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stat.getWidthForView(StatisticsHelper.RATING_STARS_TWO_COUNT), r.getDisplayMetrics())));
		if (threeStarsBar != null) threeStarsBar.setWidth(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stat.getWidthForView(StatisticsHelper.RATING_STARS_THREE_COUNT), r.getDisplayMetrics())));
		if (fourStarsBar != null) fourStarsBar.setWidth(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stat.getWidthForView(StatisticsHelper.RATING_STARS_FOUR_COUNT), r.getDisplayMetrics())));
		if (fiveStarsBar != null) fiveStarsBar.setWidth(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stat.getWidthForView(StatisticsHelper.RATING_STARS_FIVE_COUNT), r.getDisplayMetrics())));

		if (oneStarCount != null) oneStarCount.setText(String.valueOf(stat.getRatingsCount(StatisticsHelper.RATING_STARS_ONE_COUNT)));
		if (twoStarsCount != null) twoStarsCount.setText(String.valueOf(stat.getRatingsCount(StatisticsHelper.RATING_STARS_TWO_COUNT)));
		if (threeStarsCount != null) threeStarsCount.setText(String.valueOf(stat.getRatingsCount(StatisticsHelper.RATING_STARS_THREE_COUNT)));
		if (fourStarsCount != null) fourStarsCount.setText(String.valueOf(stat.getRatingsCount(StatisticsHelper.RATING_STARS_FOUR_COUNT)));
		if (fiveStarsCount != null) fiveStarsCount.setText(String.valueOf(stat.getRatingsCount(StatisticsHelper.RATING_STARS_FIVE_COUNT)));
	}

	@Override
	protected void onConnectivityChanged(boolean isConnected) {
		if (mOnline) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}
}
