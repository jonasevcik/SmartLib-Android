package cz.muni.fi.smartlib.service;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.model.Copy;
import cz.muni.fi.smartlib.model.Review;
import cz.muni.fi.smartlib.model.User;
import cz.muni.fi.smartlib.network.HttpHelperException;
import cz.muni.fi.smartlib.network.SmartlibAPI;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

public class RESTService extends IntentService{
	public static final String TAG = RESTService.class.getSimpleName();
	
	public RESTService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
		
		if (extras == null || !extras.containsKey(RESTServiceHelper.EXTRA_RESULT_RECEIVER)) {
            // Extras contain our ResultReceiver and data is our REST action.  
            // So, without these components we can't do anything useful.
            Log.e(TAG, "You did not pass extras or data with the Intent.");
            
            return;
        }
		
		ResultReceiver receiver = extras.getParcelable(RESTServiceHelper.EXTRA_RESULT_RECEIVER);
		Bundle params = extras.getParcelable(RESTServiceHelper.EXTRA_PARAMS);
		
		int action = extras.getInt(RESTServiceHelper.EXTRA_REST_SERVICE_TYPE);
		
		Log.i(TAG, "Starting service with action: " + action);
		
		switch(action) {
			
			case RESTServiceHelper.REST_SERVICE_TYPE_SEARCH_BOOKS:
				searchBook(params, receiver);
				break;
			case RESTServiceHelper.REST_SERVICE_TYPE_GET_BOOK:
				getBook(params, receiver);
				break;
			case RESTServiceHelper.REST_SERVICE_TYPE_GET_STATISTICS:
				getStatistics(params, receiver);
				break;
			case RESTServiceHelper.REST_SERVICE_TYPE_GET_REVIEWS:
				getReviews(params, receiver);
				break;
			case RESTServiceHelper.REST_SERVICE_TYPE_GET_AVAILABILITY:
				getAvailability(params, receiver);
				break;
			case RESTServiceHelper.REST_SERVICE_TYPE_LOG_IN:
				logIn(params, receiver);
				break;			
			case RESTServiceHelper.REST_SERVICE_TYPE_POST_REVIEW:
				sendReview(params, receiver);
				break;		
			case RESTServiceHelper.REST_SERVICE_TYPE_IS_USER_LOGGED_IN:
				isUserLoggedIn(params, receiver);
				break;
			case RESTServiceHelper.REST_SERVICE_TYPE_REGISTER_USER:
				registerUser(params, receiver);
				break;
		}
		
	}
	

	private void searchBook(Bundle params, ResultReceiver receiver) {
		String query = params.getString(RESTServiceHelper.PARAM_QUERY);
		int maxResults = params.getInt(RESTServiceHelper.PARAM_MAX_RESULTS);
		int offset = params.getInt(RESTServiceHelper.PARAM_OFFSET);
		
		ArrayList<Book> books = null;
		
		
			books = (ArrayList<Book>) SmartlibAPI.getInstance().searchBooks(query, maxResults, offset);
			if (books == null) {
				books = new ArrayList<Book>();
			}
			Log.i(TAG, books.size() + " books were found.");
		Bundle resultData = new Bundle();
		resultData.putParcelableArrayList(RESTServiceHelper.PARAM_BOOKS, books);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_SEARCH_BOOKS, resultData);
	}
	
	
	private void getBook(Bundle params, ResultReceiver receiver) {
		String sysno = params.getString(RESTServiceHelper.PARAM_SYSNO);
		String isbn = params.getString(RESTServiceHelper.PARAM_ISBN);
		String barcode = params.getString(RESTServiceHelper.PARAM_BARCODE);
		Book book = null;
		

		Bundle resultData = new Bundle();
		
		try {
			if (sysno == null) {
				if (isbn == null) {
					if (barcode == null) { //ani jedno
						receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_GET_BOOK, resultData);
					} else {
						book = SmartlibAPI.getInstance().getBookByBarcode(barcode);
					}
				} else {
					book = SmartlibAPI.getInstance().getBookByIsbn(isbn);
				}
			} else {
				book = SmartlibAPI.getInstance().getBook(sysno);
			}
			Log.i(TAG, "Book with sysno = " + sysno + " was found.");
		} catch (HttpHelperException e) {
			Log.e(TAG, "Failed to fetch book with sysno = " + sysno + ".");
			receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_GET_BOOK, resultData);
		}
		resultData.putParcelable(RESTServiceHelper.PARAM_BOOK, book);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_GET_BOOK, resultData);
	}
	
	//pouziju pri nacitani caroveho kodu, napred poslu dotaz na isbn a kdyz nenajde tak na sysno
	private void getBookByBarcode(Bundle params, ResultReceiver receiver) {
		//TODO
	}
	
	
	private void getTopBooks(Bundle params, ResultReceiver receiver) {
		//TODO
	}
	
	private void getNewBooks(Bundle params, ResultReceiver receiver) {
		//TODO
	}
	
	private void getReviews(Bundle params, ResultReceiver receiver) {
		String sysno = params.getString(RESTServiceHelper.PARAM_SYSNO);
		int limit = params.getInt(RESTServiceHelper.PARAM_MAX_RESULTS);
		int offset = params.getInt(RESTServiceHelper.PARAM_OFFSET);
		
		
		List<Review> reviews = new ArrayList<Review>();
		
			reviews = SmartlibAPI.getInstance().getReviews(sysno, limit, offset);
			Log.i(TAG, "Reviews for book sysno = " + sysno + " was found.");
		Bundle resultData = new Bundle();
		resultData.putParcelableArrayList(RESTServiceHelper.PARAM_REVIEW, (ArrayList<? extends Parcelable>)reviews);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_GET_REVIEWS, resultData);
	}
	
	private void getStatistics(Bundle params, ResultReceiver receiver) {
		String sysno = params.getString(RESTServiceHelper.PARAM_SYSNO);
		
		int[] stats = new int[5];
		
			stats = SmartlibAPI.getInstance().getStatistics((sysno));
			Log.i(TAG, "Statistics for book sysno = " + sysno + " was found.");

		Bundle resultData = new Bundle();
		resultData.putIntArray(RESTServiceHelper.PARAM_STATISTICS, stats);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_GET_STATISTICS, resultData);
	}
	
	private void getAvailability(Bundle params, ResultReceiver receiver) { 
		String sysno = params.getString(RESTServiceHelper.PARAM_SYSNO);
		
		List<Copy> copies = null;
		
		copies = SmartlibAPI.getInstance().getCopies((sysno));
		Log.i(TAG, "Copies for book sysno = " + sysno + " was found.");

		Bundle resultData = new Bundle();
		resultData.putParcelableArrayList(RESTServiceHelper.PARAM_COPIES, (ArrayList<? extends Parcelable>)copies);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_GET_AVAILABILITY, resultData);
	}

	private void sendReview(Bundle params, ResultReceiver receiver) {
		Review r = params.getParcelable(RESTServiceHelper.PARAM_REVIEW);
		String sysno = params.getString(RESTServiceHelper.PARAM_SYSNO);
		
		boolean result = false;
		
		try {
			result = SmartlibAPI.getInstance().sendReview(sysno, r.getText(), r.getRating());
			Log.i(TAG, "Review was sent with result: " + result);
		} catch (HttpHelperException ex) {
			Log.e(TAG, "Failed to send review.");
		}
		Bundle resultData = new Bundle();
		resultData.putBoolean(RESTServiceHelper.PARAM_RESULT, result);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_POST_REVIEW, resultData);
	}
	
	private void logIn(Bundle params, ResultReceiver receiver) {
		String userName = params.getString(RESTServiceHelper.PARAM_UCO);
		String password = params.getString(RESTServiceHelper.PARAM_PASSWORD);
		
		boolean result = false;
		
		User u = new User();
		u.setUco(userName);
		u.setPassword(password);
		try {
			result = SmartlibAPI.getInstance().loginUser(u);
		} catch (HttpHelperException e) {
			Log.e(TAG, "Failed to log in.");
		}
		Bundle resultData = new Bundle();
		resultData.putBoolean(RESTServiceHelper.PARAM_RESULT, result);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_LOG_IN, resultData);
	}
	
	private void isUserLoggedIn(Bundle params, ResultReceiver receiver) {
		boolean result = false;
		
		result = SmartlibAPI.getInstance().isUserLoggedIn();
		Log.i(TAG, "User is logged in: " + result);
		Bundle resultData = new Bundle();
		resultData.putBoolean(RESTServiceHelper.PARAM_RESULT, result);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_IS_USER_LOGGED_IN, resultData);
	}
	
	private void logout(Bundle params, ResultReceiver receiver) {
		//TODO
	}
	
	private void changePassword(Bundle params, ResultReceiver receiver) {
		//TODO
	} 
	
	private void registerUser(Bundle params, ResultReceiver receiver) {
		boolean result = false;
		
		String uco = params.getString(RESTServiceHelper.PARAM_UCO);
		String first = params.getString(RESTServiceHelper.PARAM_FIRST_NAME);
		String last = params.getString(RESTServiceHelper.PARAM_LAST_NAME);
		
		User u = new User();
		u.setUco(uco);
		u.setFirstName(first);
		u.setLastName(last);
		result = SmartlibAPI.getInstance().registerUser(u);
		
		Bundle resultData = new Bundle();
		resultData.putBoolean(RESTServiceHelper.PARAM_RESULT, result);
		receiver.send(RESTServiceHelper.REST_SERVICE_TYPE_REGISTER_USER, resultData);
	}
}
