package cz.muni.fi.smartlib.service;

import cz.muni.fi.smartlib.model.Review;
import cz.muni.fi.smartlib.model.User;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class RESTServiceHelper {
	public static final String TAG = RESTServiceHelper.class.getSimpleName();
	
	public static final int REST_SERVICE_TYPE_SEARCH_BOOKS = 0;
	public static final int REST_SERVICE_TYPE_GET_BOOK = 1;
	public static final int REST_SERVICE_TYPE_GET_STATISTICS = 2;
	public static final int REST_SERVICE_TYPE_GET_REVIEWS = 3;
	public static final int REST_SERVICE_TYPE_GET_AVAILABILITY = 4;
	public static final int REST_SERVICE_TYPE_LOG_IN = 5;
	public static final int REST_SERVICE_TYPE_POST_REVIEW = 6;
	public static final int REST_SERVICE_TYPE_IS_USER_LOGGED_IN = 7;
	public static final int REST_SERVICE_TYPE_REGISTER_USER = 8;
	

    public static final String EXTRA_PARAMS          = "cz.muni.fi.smartlib.EXTRA_PARAMS";
    public static final String EXTRA_RESULT_RECEIVER = "cz.muni.fi.smartlib.EXTRA_RESULT_RECEIVER";
    public static final String EXTRA_REST_SERVICE_TYPE = "cz.muni.fi.smartlib.EXTRA_REST_SERVICE_TYPE";
    
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_MAX_RESULTS = "maxResults";
    public static final String PARAM_OFFSET = "offset";
    public static final String PARAM_BOOKS = "books";
    public static final String PARAM_BOOK = "book";
    public static final String PARAM_SYSNO = "sysno";
    public static final String PARAM_BARCODE = "barcode";
    public static final String PARAM_ISBN = "isbn";
    public static final String PARAM_REVIEW = "review";
    public static final String PARAM_FIRST_NAME = "firstName";
    public static final String PARAM_LAST_NAME = "lastName";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_OLD_PASSWORD = "oldPassword";
    public static final String PARAM_NEW_PASSWORD = "newPassword";
    public static final String PARAM_COPIES = "copies";
    public static final String PARAM_UCO = "uco";
    public static final String PARAM_STATISTICS = "statistics";
    public static final String PARAM_RESULT = "result";
	
	
	private static RESTServiceHelper instance;
	private Context context;
	
	private RESTServiceHelper(Context context) {
		this.context = context;
	}
	
	public static RESTServiceHelper getInstance(Context context) {
		if (instance == null) {
			instance = new RESTServiceHelper(context);
		}
		instance.setContext(context);
		return instance;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void searchBooks(ResultReceiver receiver, String query, int maxResult, int offset) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_QUERY, query);
		params.putInt(PARAM_MAX_RESULTS, maxResult);
		params.putInt(PARAM_OFFSET, offset);
		
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_SEARCH_BOOKS);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void getBook(ResultReceiver receiver, String sysno) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_SYSNO, sysno);
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_GET_BOOK);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void getBookByIsbn(ResultReceiver receiver, String isbn) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_ISBN, isbn);
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_GET_BOOK);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void getBookByBarcode(ResultReceiver receiver, String barcode) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_BARCODE, barcode);
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_GET_BOOK);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	
	public void getTopBooks(ResultReceiver receiver, int maxResult, int offset) {
		//TODO
	}
	
	public void getNewBooks(ResultReceiver receiver, int maxResult, int offset) {
		//TODO
	}
	
	public void getReviews(ResultReceiver receiver, String sysno, int maxResult, int offset) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_SYSNO, sysno);
		params.putInt(PARAM_MAX_RESULTS, maxResult);
		params.putInt(PARAM_OFFSET, offset);
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_GET_REVIEWS);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void getStatistics(ResultReceiver receiver, String sysno) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_SYSNO, sysno);
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_GET_STATISTICS);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void getAvailability(ResultReceiver receiver, String sysno) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_SYSNO, sysno);
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_GET_AVAILABILITY);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}

	public void sendReview(ResultReceiver receiver, Review review, String sysno) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putParcelable(PARAM_REVIEW, review);
		params.putString(PARAM_SYSNO, sysno);
		
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_POST_REVIEW);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void login(ResultReceiver receiver, User u) {
		Intent i = new Intent(context, RESTService.class); 
		
		Bundle params = new Bundle();
		params.putString(PARAM_UCO, u.getUco());
		params.putString(PARAM_PASSWORD, u.getPassword());
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_LOG_IN);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void isUserLoggedIn(ResultReceiver receiver) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_IS_USER_LOGGED_IN);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	public void logout(ResultReceiver receiver, String userName, String password) {
		//TODO
	}
	
	public void changePassword(ResultReceiver receiver, String userName, String oldPassword, String newPassword) {
		//TODO
	} 
	
	public void registerUser(ResultReceiver receiver, String uco, String firstName, String lastName) {
		Intent i = new Intent(context, RESTService.class);
		
		Bundle params = new Bundle();
		params.putString(PARAM_UCO, uco);
		params.putString(PARAM_FIRST_NAME, firstName);
		params.putString(PARAM_LAST_NAME, lastName);
		
		i.putExtra(EXTRA_REST_SERVICE_TYPE, REST_SERVICE_TYPE_REGISTER_USER);
		i.putExtra(EXTRA_RESULT_RECEIVER, receiver);
		i.putExtra(EXTRA_PARAMS, params);
		
		context.startService(i);
	}
	
	
	
	
	
	
	
	
	
}
