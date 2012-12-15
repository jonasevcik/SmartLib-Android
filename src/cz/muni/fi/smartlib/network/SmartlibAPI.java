package cz.muni.fi.smartlib.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.params.HttpConnectionParams;

import com.google.gson.JsonSyntaxException;

import android.util.Log;

import cz.muni.fi.smartlib.json.JSONParser;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.model.Copy;
import cz.muni.fi.smartlib.model.Review;
import cz.muni.fi.smartlib.model.User;
import cz.muni.fi.smartlib.network.HttpHelper.HttpConnectResult;
import cz.muni.fi.smartlib.network.HttpHelper.HttpConnectResult.Status;


/*
 * Singleton trida slouzici jako "API" pro ziskavani dat ze serveru
 * 
 * **/
public class SmartlibAPI {
	public static final String TAG = SmartlibAPI.class.getSimpleName();
	public static final boolean DEBUG = true;
	
	public static final int GET_BOOK_BY_ISBN = 0;
	public static final int GET_BOOK_BY_BARCODE = 1;
	public static final int GET_BOOK_BY_SYSNO = 2;
	public static final int GET_BOOK_REVIEWS = 3;
	public static final int GET_BOOK_RATINGS = 4;
	public static final int GET_BOOK_DETAILS = 5;
	public static final int SEARCH_BOOKS = 6;
	public static final int POST_BOOK_REVIEW = 7;

	
	//http parametry
	public static final String PARAM_QUERY = "query";
	public static final String PARAM_ISBN = "isbn";
	public static final String PARAM_BARCODE = "barcode";
	public static final String PARAM_BARCODES = "barcodes";
	public static final String PARAM_SYSNO = "sysno";
	public static final String PARAM_AUTHOR = "author";
	public static final String PARAM_TITLE = "title";
	public static final String PARAM_LIMIT = "limit";
	public static final String PARAM_OFFSET = "offset";
	public static final String PARAM_FIRSTNAME = "firstName";
	public static final String PARAM_LASTNAME = "lastName";
	public static final String PARAM_UCO = "uco";
	public static final String PARAM_PASSWORD = "password";
	public static final String PARAM_TEXT = "text";
	public static final String PARAM_RATING = "rating";
	public static final String PARAM_OLD_PASSWORD = "oldPassword";
	public static final String PARAM_NEW_PASSWORD = "newPassword";
	public static final String PARAM_CATEGORY = "category";
	public static final String PARAM_CATEGORY_TOP = "top";
	public static final String PARAM_CATEGORY_NEW = "new";
	
	
	public static final String SMARTLIB_URL = "https://web-smartlibweb.rhcloud.com/api/";

	private static SmartlibAPI instance;
	private HttpHelper httpHelper;
	
	private SmartlibAPI () {
		httpHelper = HttpHelper.getInstance();
	}
	
	public static SmartlibAPI getInstance() {
		if (instance == null) {
			instance = new SmartlibAPI();
		}
		return instance;
	}
	
	
	/*
	 *  GET /books/search?query={query}&limit={limit}&offset={offset}
	 *  
	 * */
	public List<Book> searchBooks(String query, int limit, int offset) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_QUERY, query);
		params.put(PARAM_LIMIT, String.valueOf(limit));
		params.put(PARAM_OFFSET, String.valueOf(offset));
	
		HttpConnectResult result = null;
		List<Book> books = null;
		try {
			result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/search", params, null);
			books = JSONParser.parseBooks(result.response);
		} catch (HttpHelperException ex) {
			if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + ex.getMessage());
			return null;
		} catch (JsonSyntaxException ex) {
			if (DEBUG) Log.e(TAG, "+++ JSON Error: " + ex.getMessage());
			return null;
		}
		return books;
		
	}
	
	/*
	 *  GET /books/list?category={category}&limit={limit}&offset={offset}
	 *  
	 * */
	public List<Book> getTopBooks(int limit, int offset) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_LIMIT, String.valueOf(limit));
		params.put(PARAM_OFFSET, String.valueOf(offset));
		params.put(PARAM_CATEGORY, PARAM_CATEGORY_TOP);
		
		HttpConnectResult result = null;
		List<Book> books = null;
		try {
			result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/list", params, null);
			books = JSONParser.parseBooks(result.response);
		} catch (HttpHelperException ex) {
			if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + ex.getMessage());
			return null;
		} catch (JsonSyntaxException ex) {
			if (DEBUG) Log.e(TAG, "+++ JSON Error: " + ex.getMessage());
			return null;
		}
		return books;
	}
	
	
	/*
	 *  GET /books/list?category={category}&limit={limit}&offset={offset}
	 *  
	 * */
	public List<Book> getRecentlyRatedBooks(int limit, int offset) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_LIMIT, String.valueOf(limit));
		params.put(PARAM_OFFSET, String.valueOf(offset));
		params.put(PARAM_CATEGORY, PARAM_CATEGORY_NEW);
		
		HttpConnectResult result = null;
		List<Book> books = null;
		try {
			result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/list", params, null);
			books = JSONParser.parseBooks(result.response);
		} catch (HttpHelperException ex) {
			if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + ex.getMessage());
			return null;
		} catch (JsonSyntaxException ex) {
			if (DEBUG) Log.e(TAG, "+++ JSON Error: " + ex.getMessage());
			return null;
		}
		return books;
	}
	
	public Book getBook(String sysno) throws HttpHelperException {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_SYSNO, sysno);
		
		HttpConnectResult result = httpHelper.httpConnectGET(SMARTLIB_URL + "books", params, null);
		
		Book book = null;
		if (result != null) {
			book = JSONParser.parseBook(result.response);
		} 
		return book;
	}
	
	public Book getBookByIsbn(String isbn) throws HttpHelperException {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_ISBN, isbn);
		
		HttpConnectResult result = httpHelper.httpConnectGET(SMARTLIB_URL + "books", params, null);
		
		Book book = null;
		if (result != null) {
			book = JSONParser.parseBook(result.response);
		}
		return book;
	}
	
	public Book getBookByBarcode(String barcode) throws HttpHelperException {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_BARCODE, barcode);
		
		HttpConnectResult result = httpHelper.httpConnectGET(SMARTLIB_URL + "books", params, null);
		
		Book book = null;
		if (result != null) {
			book = JSONParser.parseBook(result.response);
		}
		return book;
	}
	
	public List<Copy> getCopies(String sysno) {
		Map<String, String> params = new HashMap<String, String>();
		
		HttpConnectResult result = null;
		try {
			result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/" + sysno + "/copies", params, null);
		} catch (HttpHelperException e) {
			if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + e.getMessage());
			return null;
		}
		
		List<Copy> copies = null;
		
		if (result != null && result.getStatus() == Status.STATUS_OK) {
			try {
				copies = JSONParser.parseCopies(result.response);
				return copies;
			} catch (JsonSyntaxException e) {
				if (DEBUG) Log.e(TAG, "+++ JSON Error: " + e.getMessage());
				return null;
			}
		}
		return null;
	}
	
	public List<Copy> refreshCopies(List<Copy> oldCopies, String sysno) {
		if (oldCopies == null) {
			if (DEBUG) Log.w(TAG, "+++ oldCopies is null");
			return null;
		}
		
		StringBuilder barcodesBuilder = new StringBuilder();
		
		for (Copy c : oldCopies) {
			barcodesBuilder.append(c.getBarcode()).append(",");
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_BARCODES, barcodesBuilder.toString());
		
		HttpConnectResult result = null; 
		try{
			result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/" + sysno + "/check", params, null);
		} catch (HttpHelperException e) {
			if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + e.getMessage());
			return null;
		}
		
		List<Copy> newCopies = null;
		
		if (result != null && result.getStatus() == Status.STATUS_OK) {
			try {
				newCopies = JSONParser.parseCopies(result.response);
				return updateCopyStatuses(oldCopies, newCopies);
			} catch (JsonSyntaxException e) {
				if (DEBUG) Log.e(TAG, "+++ JSON Error: " + e.getMessage());
				return null;
			}
		}
		return null;
		
	}
	
	private List<Copy> updateCopyStatuses (List<Copy> oldCopies, List<Copy> newCopies) {
		
		List<Copy> result = new ArrayList<Copy>(oldCopies);
		
		for (Copy oldCopy : result) {
			for (Copy newCopy : newCopies) {
				if (oldCopy.getBarcode().equals(newCopy.getBarcode())) {
					oldCopy.setStatus(newCopy.getStatus());
					if (!oldCopy.getStatus()) { //if copy is not available then set return date
						oldCopy.setCheckReturn(newCopy.getCheckReturn());
					}
					break;
				}
			}
		}
		return result;
	}
	
	
	
	/*
	 *  GET /books/sysno/reviews?sysno={sysno}&limit={limit}&offset={offset} 
	 * 
	 * */
	public List<Review> getReviews(String sysno, int limit, int offset) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_LIMIT, String.valueOf(limit));
		params.put(PARAM_OFFSET, String.valueOf(offset));
		
		HttpConnectResult result = null;
		try { 
			result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/" + sysno + "/reviews", params, null);
		} catch (HttpHelperException e) {
			if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + e.getMessage());
			return null;
		}
		List<Review> reviews = null;
		if (result != null && result.getStatus() == Status.STATUS_OK) {
			try {
				reviews = JSONParser.parseReviews(result.response);
				return reviews;
			} catch (JsonSyntaxException e) {
				if (DEBUG) Log.e(TAG, "+++ JSON Error: " + e.getMessage());
				return null;
			}
		}
		return null;
	}
	
	
	/*
	 * GET /books/{sysno}/ratings
	 * 
	 * */
	public int[] getStatistics(String sysno) {
		Map<String, String> params = new HashMap<String, String>();
		
		HttpConnectResult result = null;
		try {
			result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/" + sysno + "/ratings", params  , null);
		} catch (HttpHelperException e) {
			if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + e.getMessage());
			return null;
		}
		
		int[] ratings = null;
		if (result != null && result.getStatus() == Status.STATUS_OK) {
			try {
				ratings = JSONParser.parseRatings(result.response);
				return ratings;
			} catch (JsonSyntaxException e) {
				if (DEBUG) Log.e(TAG, "+++ JSON Error: " + e.getMessage());
				return null;
			}
		}
		return null;
	}
	
	public Book completeBook(Book incompleteBook) {
		if (incompleteBook.getSysno() != null && !incompleteBook.getSysno().equals("")) {
			String sysno = incompleteBook.getSysno();
			Map<String,String> params = new HashMap<String, String>();
			
			HttpConnectResult result = null;
			try {
				result = httpHelper.httpConnectGET(SMARTLIB_URL + "books/" + sysno + "/details", params, null);
			} catch (HttpHelperException e) {
				if (DEBUG) Log.e(TAG, "+++ HTTP Error: " + e.getMessage());
				return null;
			}
			Book partBook = null;
			if (result != null && result.getStatus() == Status.STATUS_OK) {
				try {
					partBook = JSONParser.parseBook(result.response);
					return mergeBook(incompleteBook, partBook);
				} catch (JsonSyntaxException e) {
					if (DEBUG) Log.e(TAG, "+++ JSON Error: " + e.getMessage());
					return null;
				}
			}
			return null;
		}
		return null;
	}
	
	public boolean isUserLoggedIn() {
		Map<String, String> params = new HashMap<String, String>();
		
		HttpConnectResult result = null;
		
		try {
			result = httpHelper.httpConnectPOST(SMARTLIB_URL + "user/authentication", params, null);
		} catch (HttpHelperException e) {
			return false;
		}
		if (result != null && result.getStatus() == Status.STATUS_OK) {
			return true;
		}
		return false;
	}
	
	public boolean registerUser(User user) {
		Map<String, String> params = new HashMap<String, String>();

		params.put(PARAM_FIRSTNAME, user.getFirstName());
		params.put(PARAM_LASTNAME, user.getLastName());
		params.put(PARAM_UCO, String.valueOf(user.getUco()));
		
		HttpConnectResult result = null;
		try {
			result = httpHelper.httpConnectPOST(SMARTLIB_URL + "user/registration", params, null);
		} catch (HttpHelperException e) {
			return false;
		}
		if (result.getStatus() == Status.STATUS_OK) {
			return true;
		}
		return false;	
		
	}
	
	public boolean loginUser(User user) throws HttpHelperException {
		Map<String, String> params = new HashMap<String, String>();

		params.put(PARAM_UCO, String.valueOf(user.getUco()));
		params.put(PARAM_PASSWORD, user.getPassword());
		
		HttpConnectResult result = httpHelper.httpConnectPOST(SMARTLIB_URL + "user/login", params, null);
		
		if (result.getStatus() == Status.STATUS_OK) {
			return true;
		}
		HttpHelper.getInstance().deleteCookies();
		return false;	
	}
	
	public boolean logout(User user) throws HttpHelperException {
		Map<String, String> params = new HashMap<String, String>();		
		
		HttpConnectResult result = httpHelper.httpConnectPOST(SMARTLIB_URL + "user/logout", params, null);
		HttpHelper.getInstance().deleteCookies();
		if (result.getStatus() == Status.STATUS_OK) {
			return true;
		}
		return false;
	}
	
	public boolean changePassword(String uco, String oldPassword, String newPassword) throws HttpHelperException {
		Map<String, String> params = new HashMap<String, String>();		
		
		params.put(PARAM_OLD_PASSWORD, oldPassword);
		params.put(PARAM_NEW_PASSWORD, newPassword);
		params.put(PARAM_UCO, uco);
		
		
		HttpConnectResult result = httpHelper.httpConnectPOST(SMARTLIB_URL + "user/changepassword", params, null);
		if (result.getStatus() == Status.STATUS_OK) {
			return true;
		}
		return false;
	}
	
	public boolean sendReview(String sysno, String  text, int rating) throws HttpHelperException {
		Map<String, String> params = new HashMap<String, String>();

		params.put(PARAM_TEXT, text);
		params.put(PARAM_RATING, String.valueOf(rating));
		
		HttpConnectResult result = httpHelper.httpConnectPOST(SMARTLIB_URL + "books/" + sysno + "/reviews", params, null);
		
		if (result.getStatus() == Status.STATUS_OK) {
			return true;
		}
		
		return false;
	}
	
	private Book mergeBook(Book incompleteBook, Book partBook) {
		Book completeBook = incompleteBook;
		
		completeBook.setIsbn(partBook.getIsbn());
		completeBook.setPublisher(partBook.getPublisher());
		completeBook.setPublishedDate(partBook.getPublishedDate());
		completeBook.setLanguage(partBook.getLanguage());
		completeBook.setPageType(partBook.getPageType());
		completeBook.setPageCount(partBook.getPageCount());
		completeBook.setPreviewUrl(partBook.getPreviewUrl());
		
		return completeBook;
	}
	
}
