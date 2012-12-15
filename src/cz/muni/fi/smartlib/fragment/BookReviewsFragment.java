package cz.muni.fi.smartlib.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import com.actionbarsherlock.app.SherlockFragment;

import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.loader.FancyLoader;
import cz.muni.fi.smartlib.loader.ReviewsLoader;
import cz.muni.fi.smartlib.model.Review;
import cz.muni.fi.smartlib.model.User;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Validator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

public class BookReviewsFragment extends NetFragment implements LoaderCallbacks<List<Review>>, OnClickListener {
	public static final String TAG = BookReviewsFragment.class.getSimpleName();
	public static final boolean DEBUG = true;
	
	
	public interface BookReviewsListener {
		public void onAddReview();
	}
	
	private BookReviewsListener mListener;
	
	private List<Review> mReviews = new ArrayList<Review>();
	private String mSysno;
	
	private ProgressBar mProgressBar;
	private TextView mMessage;
	private Button mAddReview;
	
	private LinearLayout mReviewsContainer;
	
	public static BookReviewsFragment newInstance(String sysno) {
		BookReviewsFragment f = new BookReviewsFragment();
	    Bundle args = new Bundle();
	    args.putString(RESTServiceHelper.PARAM_SYSNO, sysno);
	    f.setArguments(args);
	    return f;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (BookReviewsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BookReviewsListener");
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_book_reviews, container, false);

		mProgressBar = (ProgressBar) view.findViewById(R.id.display_book_progress);
		mMessage = (TextView) view.findViewById(R.id.display_book_message);
		mAddReview = (Button) view.findViewById(R.id.display_book_add_review_button);
		mAddReview.setEnabled(mOnline);
		mReviewsContainer = (LinearLayout) view.findViewById(R.id.display_book_reviews_list);
		mAddReview.setOnClickListener(this);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = new Bundle();
		bundle.putString(RESTServiceHelper.PARAM_SYSNO, mSysno);
		
		getLoaderManager().initLoader(FancyLoader.REVIEWS_LOADER_NET, bundle, this);
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.display_book_add_review_button) {
			disableUI();
			mListener.onAddReview();	
		}
	}
	
	@Override
	public Loader<List<Review>> onCreateLoader(int loader, Bundle bundle) {
		if (DEBUG) Log.i(TAG, "+++ onCreateLoader() called +++");	
		mProgressBar.setVisibility(View.VISIBLE);
		
		String sysno = null;
		if (bundle != null) {
			sysno = bundle.getString(RESTServiceHelper.PARAM_SYSNO);
		}
		return new ReviewsLoader(getActivity(), sysno);
	}

	@Override
	public void onLoadFinished(Loader<List<Review>> loader, List<Review> reviews) {
		if (DEBUG) Log.i(TAG, "+++ onLoadFinished() called: mOnline: "+ FancyLoader.isOnline(this, FancyLoader.REVIEWS_LOADER_NET) +" +++");
		mProgressBar.setVisibility(View.GONE);
        mReviews.clear();
		if (reviews != null) {
			mReviews.addAll(reviews);
		}
		setReviews();
	}

	@Override
	public void onLoaderReset(Loader<List<Review>> loader) {
		if (DEBUG) Log.i(TAG, "+++ onLoaderRestart() called +++");
    	mProgressBar.setVisibility(View.VISIBLE);
    	disableMessage();
		mReviewsContainer.removeAllViews();
	}	
	
	public void refresh() {
		if (DEBUG) Log.i(TAG, "+++ refresh() called +++");
		Bundle bundle = new Bundle();
		bundle.putString(RESTServiceHelper.PARAM_SYSNO, mSysno);
		getLoaderManager().restartLoader(FancyLoader.REVIEWS_LOADER_NET, bundle, this);
	}
	
    private void setReviews() {
    	mReviewsContainer.removeAllViews();
    	if (!Validator.isNullorEmpty(mReviews)) {
    		Activity a = getSherlockActivity();
    		if (a != null) {
    			LayoutInflater inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			for (Review review : mReviews) {
    				View view = inflater.inflate(R.layout.list_reviews, null);
    				TextView userName = (TextView) view
    						.findViewById(R.id.list_reviews_user_name);
    				TextView date = (TextView) view
    						.findViewById(R.id.list_reviews_date);
    				TextView reviewText = (TextView) view
    						.findViewById(R.id.list_reviews_review_text);
    				RatingBar ratingBar = (RatingBar) view
    						.findViewById(R.id.list_reviews_rating_bar);
    				
    				if (review != null) {
    					User user = review.getUser();
    					if (user != null) {
    						String firstName = user.getFirstName();
    						String lastName = user.getLastName();
    						firstName = (firstName == null) ? "" : firstName;
    						lastName = (lastName == null) ? "" : lastName;
    						userName.setText(review.getUser().getFirstName() + ", "
    								+ review.getUser().getLastName());
    					}
    					date.setText(UIUtils.parseDateFromString(review.getDate()));
    					reviewText.setText(review.getText());
    					ratingBar.setRating((float) review.getRating());
    				}
    				mReviewsContainer.addView(view);
    			}
    			disableMessage();
    		} else {
    			setErrorMessage();
    		}
    	} else {
    		if (!FancyLoader.isOnline(this, FancyLoader.REVIEWS_LOADER_NET)) {	//neni internet
    			setNoInternetMessage();
    			return;
    		}
    		if (FancyLoader.loadingHasError(this, FancyLoader.REVIEWS_LOADER_NET)) {	//chyba
    			setErrorMessage();
    		} else {	//zadne recenze
    			setEmptyMessage();
    		}
    	}
    }
    
    private void setErrorMessage() {
    	mMessage.setText(R.string.loading_error);
    	mMessage.setVisibility(View.VISIBLE);
    }
	
    private void setEmptyMessage() {
    	mMessage.setText(R.string.display_book_empty_review);
    	mMessage.setVisibility(View.VISIBLE);
    	
    }
    
    private void setNoInternetMessage() {
    	mMessage.setText(R.string.no_internet);
    	mMessage.setVisibility(View.VISIBLE);
    }
    
    private void disableMessage() {
    	mMessage.setVisibility(View.GONE);
    }
	
	public void disableUI() {
		mProgressBar.setVisibility(View.VISIBLE);
		mAddReview.setEnabled(false);
	}
	
	public void enableUI() {
		mProgressBar.setVisibility(View.GONE);
		mAddReview.setEnabled(true);
	}

	@Override
	protected void onConnectivityChanged(boolean isConnected) {
		mAddReview.setEnabled(mOnline);
		if (mOnline) {
			disableMessage();
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}
	

	
	
}
