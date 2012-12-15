package cz.muni.fi.smartlib.json;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cz.muni.fi.smartlib.fragment.ScanLoadingDialog;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.model.Copy;
import cz.muni.fi.smartlib.model.Review;

public class JSONParser {
	public static final String TAG = JSONParser.class.getSimpleName();
	
	public static Book parseBook(String json) {
		Gson parser = new Gson();	
		return parser.fromJson(json, Book.class);
	}
	
	public static List<Book> parseBooks(String json) {
		Gson parser = new Gson();
		Type collectionType = new TypeToken<List<Book>>(){}.getType();
		List<Book> books = parser.fromJson(json, collectionType);
		
		return books;
	}
	
	public static List<Review> parseReviews(String json) {
		Gson parser = new Gson();
		Type collectionType = new TypeToken<List<Review>>(){}.getType();
		List<Review> reviews = parser.fromJson(json, collectionType);
		
		return reviews;
	}
	
	public static int[] parseRatings(String json) {
		Gson parser = new Gson();
		return parser.fromJson(json, int[].class);
	}
	
	public static Copy parseCopy(String json) {
		Gson parser = new Gson();
		return parser.fromJson(json, Copy.class);
	}
	
	public static List<Copy> parseCopies (String json) {
		Gson parser = new Gson();
		Type collectionType = new TypeToken<List<Copy>>(){}.getType();
		return parser.fromJson(json, collectionType);
	}
	
}
