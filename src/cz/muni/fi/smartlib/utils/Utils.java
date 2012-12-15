package cz.muni.fi.smartlib.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo.DetailedState;
import android.util.Log;

import cz.muni.fi.smartlib.BookDetailActivity;
import cz.muni.fi.smartlib.MainActivity;
import cz.muni.fi.smartlib.RateBookActivity;
import cz.muni.fi.smartlib.SearchActivity;
import cz.muni.fi.smartlib.model.Author;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.service.RESTServiceHelper;

public class Utils {
	public static final String TAG = Utils.class.getSimpleName();
	public static final boolean DEBUG = true; 

	public static String getAuthorsString (List<Author> authors) {
		StringBuilder builder = new StringBuilder();
		if (authors == null) {
			return "";
		}
		if (authors.size() == 1) {
			builder.append(authors.get(0).getName());
		} else {
			for (int i = 1; i < authors.size() - 1; i++) {
				builder.append(authors.get(i).getName()).append("; ");
			}
			builder.append(authors.get(authors.size() - 1).getName());	
		}
		return builder.toString();
	}
	
	public static void startSearchActivity(Context context) {
		if (DEBUG) Log.i(TAG, "+++ startSearchActivity() +++");
		
		if (context == null) return;
		
		Intent i = new Intent(context, SearchActivity.class);
		context.startActivity(i);
	}
	
	public static void startRateActivity(Context context, String sysno) {
		if (context == null || Validator.isNullorEmpty(sysno)) return;
		

		Intent i = new Intent(context, RateBookActivity.class);
		i.putExtra(RESTServiceHelper.PARAM_SYSNO, sysno);
		context.startActivity(i);
	}
	
	public static void startBookDetails(Context context, Book book) {
		if (context == null || book == null) return;
		
		Intent i = new Intent(context, BookDetailActivity.class);
		i.putExtra(RESTServiceHelper.PARAM_BOOK, book);
		context.startActivity(i);
	}
	
	public static void startMainActivity(Context context) {
		if (context == null) return;
		
		Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
	}
	
	public static String repairUrl(String url) {
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			return "http://" + url;
		return url;
	}
	
	
	
}
