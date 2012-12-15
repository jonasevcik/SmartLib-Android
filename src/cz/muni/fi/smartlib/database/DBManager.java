package cz.muni.fi.smartlib.database;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.loader.BooksDbLoader;
import cz.muni.fi.smartlib.loader.SearchQueryLoader.SearchQueryBroadcastReceiver;
import cz.muni.fi.smartlib.model.Author;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.utils.Validator;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
	public static final String TAG = DBManager.class.getSimpleName();
	 


	public static final String QUERY_TABLE_NAME = "searchquery";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_QUERY = "query";
	
    public static final String TABLE_NAME = "book";
    public static final String KEY_SYSNO= "_sysno";
    public static final String KEY_ISBN = "isbn";
    public static final String KEY_TITLE = "title";
    public static final String KEY_AUTHORS = "authors";
    public static final String KEY_PUBLISHER = "publisher";
    public static final String KEY_PUBLISHED_DATE = "publishedDate";
    public static final String KEY_COVER_THUMB = "thumbnail";
    public static final String KEY_PDF_LINK = "pdflink";
    public static final String KEY_PAGE_COUNT = "pageCount";
    public static final String KEY_AVERAGE_RATING = "averageRating";
    public static final String KEY_RATING_COUNT = "ratingCount";
    public static final String KEY_LAST_MODIFIED = "lastModified";
    public static final String KEY_SAVED = "saved";	//SQLite nema boolean, proto 0 - false, 1 - true
    
    public static final String STRING_SEPARATOR = "&";
    
    public static final String[] BOOK_COLUMNS = new String[] {KEY_SYSNO, KEY_ISBN, KEY_TITLE, KEY_AUTHORS, KEY_PUBLISHER, KEY_PUBLISHED_DATE, 
    														  KEY_COVER_THUMB, KEY_PDF_LINK, KEY_PAGE_COUNT, KEY_AVERAGE_RATING, KEY_RATING_COUNT
    														  , KEY_LAST_MODIFIED, KEY_SAVED};
    public static final int MAX_VIEWED_BOOKS = 10;

    private static DBHelper dbHelper;

    private static DBManager instance;
    
    public static DBManager getInstance(Context context) {
    	if (instance == null) {
    		instance = new DBManager(context);
    	}
    	return instance;
    }
    
    private DBManager(Context context) {
		if (context == null) {
			Log.e(TAG, "Context cannot be null.");
    		throw new IllegalArgumentException("Context cannot be null.");
		}
        dbHelper = new DBHelper(context);
    }

   
    /**
     * Closes the database.
     */
    public void close() {
        dbHelper.close();
    }

    //vrati 10 nejnovejsich knih
    public Cursor fetchLatestBooks() {
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor c = db.query(TABLE_NAME, BOOK_COLUMNS, null, null, null, null, KEY_LAST_MODIFIED + " DESC");
    	Log.i(TAG, c.getCount() + "books loaded from db.");
    	return c;
	}
    
    public List<Book> fetchLatestBooksList() {
    	Cursor c = fetchLatestBooks();
    	Log.i(TAG, c.getCount() + "books loaded from db.");
    	return booksCursorToList(c);
    }
    
    public Cursor fetchSavedBooks() {
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor c = db.query(TABLE_NAME, BOOK_COLUMNS, KEY_SAVED + "=1", null, null, null, KEY_LAST_MODIFIED + " DESC");
    	Log.i(TAG, c.getCount() + "books loaded from db.");
    	return c;
    }
    
    public List<Book> fetchSavedBooksList() {
    	Cursor c = fetchSavedBooks();
    	Log.i(TAG, c.getCount() + "books loaded from db.");
    	return booksCursorToList(c);
    }
    
    
    public boolean isBookInDB(String sysno) {
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor c = db.query(TABLE_NAME, new String[] {KEY_SYSNO}, KEY_SYSNO + "='" + sysno +"'" , null, null, null, null);
    	Log.i(TAG, (c.getCount() == 0)? "Book with sysno: " + sysno + "is not in db." : "Book with sysno: " + sysno + "is in db.");
    	return c.getCount() != 0;
    }
    
    public Book fetchBook(String sysno) {
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor c = db.query(TABLE_NAME, BOOK_COLUMNS, KEY_SYSNO + "='" + sysno +"'" , null, null, null, null);
    	c.moveToFirst();
    	return cursorToBook(c);
    }
    
    public boolean isBookSavedInDB(String sysno) {
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor c = db.query(TABLE_NAME, new String[] {KEY_SYSNO}, KEY_SYSNO + "='" + sysno +"' AND " + KEY_SAVED + "=1" , null, null, null, null);
    	return c.getCount() != 0;
    }
    
    public boolean insertSavedBook(Book book) {
    	boolean success = false;

    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	
    	if (isBookInDB(book.getSysno())) {
    		ContentValues v = new ContentValues();
    		v.put(KEY_SAVED, 1);
    		v.put(KEY_LAST_MODIFIED, System.currentTimeMillis());
    		db.beginTransaction();
    		success = db.update(TABLE_NAME, v, KEY_SYSNO + "='" + book.getSysno() +"'", null) == 1;
    		deleteOldestBook();
    		db.setTransactionSuccessful();
    		db.endTransaction();
    	} else {
    		ContentValues values = new ContentValues();
            values.put(KEY_SYSNO, book.getSysno());
            values.put(KEY_ISBN, book.getIsbn());
            values.put(KEY_TITLE, book.getTitle());
            values.put(KEY_AUTHORS, constructAuthorsString(book.getAuthors()));
            values.put(KEY_PUBLISHER, book.getPublisher());
            values.put(KEY_COVER_THUMB, book.getCoverUrl());
            values.put(KEY_PUBLISHED_DATE, book.getPublishedDate());
            values.put(KEY_PDF_LINK, book.getPreviewUrl());
            values.put(KEY_PAGE_COUNT, book.getPageCount());
            values.put(KEY_LAST_MODIFIED, System.currentTimeMillis());
            values.put(KEY_SAVED, 1);
            values.put(KEY_AVERAGE_RATING, book.getAverageRating());
        	db.beginTransaction();
        	success = db.insert(TABLE_NAME, null, values) > -1;
        	db.setTransactionSuccessful();
        	db.endTransaction();
    	}
    	notifyListener(BooksDbLoader.BookDbBroadcastReceiver.ACTION_SAVED_BOOKS_CHANGED);
    	
    	Log.i(TAG, "Viewed book sysno " + book.getSysno() + " was added with success = " + success + ".");
        return success;
    }
    
    public boolean updateAverageRating(String sysno, double averageRating) {
    	boolean success = false;
    	
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	ContentValues v = new ContentValues();
    	v.put(KEY_AVERAGE_RATING, averageRating);
    	db.beginTransaction();
    	success = db.update(TABLE_NAME, v, KEY_SYSNO + "='" + sysno +"'", null) == 1;
    	db.setTransactionSuccessful();
    	db.endTransaction();
    	notifyListener(BooksDbLoader.BookDbBroadcastReceiver.ACTION_VIEWED_BOOKS_CHANGED);
    	notifyListener(BooksDbLoader.BookDbBroadcastReceiver.ACTION_SAVED_BOOKS_CHANGED);
    	return success;
    }
    
    public boolean insertViewedBook(Book book) {
    	boolean success = false;
    	
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	
    	if (isBookInDB(book.getSysno())) {
    		ContentValues v = new ContentValues();
    		v.put(KEY_LAST_MODIFIED, System.currentTimeMillis());
    		db.beginTransaction();
    		success = db.update(TABLE_NAME, v, KEY_SYSNO + "='" + book.getSysno() +"'", null) == 1;
    		deleteOldestBook();
    		db.setTransactionSuccessful();
    		db.endTransaction();
    	} else {
    		ContentValues values = new ContentValues();
            values.put(KEY_SYSNO, book.getSysno());
            values.put(KEY_ISBN, book.getIsbn());
            values.put(KEY_TITLE, book.getTitle());
            values.put(KEY_AUTHORS, constructAuthorsString(book.getAuthors()));
            values.put(KEY_PUBLISHER, book.getPublisher());
            values.put(KEY_COVER_THUMB, book.getCoverUrl());
            values.put(KEY_PUBLISHED_DATE, book.getPublishedDate());
            values.put(KEY_PDF_LINK, book.getPreviewUrl());
            values.put(KEY_PAGE_COUNT, book.getPageCount());
            values.put(KEY_LAST_MODIFIED, System.currentTimeMillis());
            values.put(KEY_SAVED, 0);
            values.put(KEY_AVERAGE_RATING, book.getAverageRating());
        	db.beginTransaction();
        	success = db.insert(TABLE_NAME, null, values) > -1;
        	db.setTransactionSuccessful();
        	db.endTransaction();
    	}
    	notifyListener(BooksDbLoader.BookDbBroadcastReceiver.ACTION_VIEWED_BOOKS_CHANGED);
    	Log.i(TAG, "Viewed book sysno " + book.getSysno() + " was added with success = " + success + ".");
    	return success;
    }
    
    private boolean deleteBook(String sysno) {
    	Book book = new Book();
    	book.setSysno(sysno);
    	return deleteBook(book);
    }
    
    private boolean deleteBook(Book book) {
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = db.delete(TABLE_NAME, KEY_SYSNO + "='" + book.getSysno() +"'", null) > 0;
        Log.i(TAG, "Book sysno " + book.getSysno() + " was deleted with success = " + success + ".");
        return success;
    }
    
    //zmeni knizce flag saved na false 
    public boolean deleteSavedBook(String sysno) {
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	boolean success = false;
    	
    	ContentValues v = new ContentValues();
    	v.put(KEY_SAVED, 0);
    	v.put(KEY_LAST_MODIFIED, System.currentTimeMillis());
    	db.beginTransaction();
    	success = db.update(TABLE_NAME, v, KEY_SYSNO + "='" + sysno + "'", null) == 1;
    	db.setTransactionSuccessful();
    	db.endTransaction();
    	notifyListener(BooksDbLoader.BookDbBroadcastReceiver.ACTION_SAVED_BOOKS_CHANGED);
    	
    	return success;
    }
    
    public boolean deleteAllBooks() {
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(TABLE_NAME, null, null) > 0;
    }
    
    public String[] getAllSearchQueries() {
    	SQLiteDatabase db =dbHelper.getReadableDatabase();
    	Cursor c = db.query(QUERY_TABLE_NAME, new String[]{KEY_QUERY}, null, null, null, null, null);
    	return queriesCursorToArray(c);
    }
    
    public boolean insertQuery(String query) {
    	boolean success = false;
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(KEY_QUERY, query);
        try {
        	success = db.insertOrThrow(QUERY_TABLE_NAME, null, values) > -1;
        } catch (SQLiteConstraintException e) {
        	success = false;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        notifyListener(SearchQueryBroadcastReceiver.ACTION_SEARCH_QUERIES_CHANGED);
        return success;
    }
    
    private boolean deleteOldestBook() {
    	Cursor latest = fetchLatestBooks();
    	if (latest.getCount() > MAX_VIEWED_BOOKS) {
    		latest.moveToLast();
			String sysnoToDelete = getValue(latest, KEY_SYSNO);
			return deleteBook(sysnoToDelete);
    	}
    	return true;
    }
    
    private String getValue(Cursor c, String key) {
    	int index = c.getColumnIndex(key);
    	return c.getString(index);
    }
    
    private String[] queriesCursorToArray(Cursor c) {
    	if (c == null) {
    		throw new IllegalArgumentException("Cursor cannot be null.");
    	}
    	List<String> output = new ArrayList<String>();
    	
    	for(c.moveToPosition(-1); c.moveToNext(); c.isAfterLast()) {
    		output.add(getValue(c, KEY_QUERY));
    	}
    	return output.toArray(new String[output.size()]);
    	
    }
    
    private List<Book> booksCursorToList(Cursor c) {
    	if (c == null) {
    		throw new IllegalArgumentException("Cursor cannot be null.");
    	}
    	List<Book> output = new ArrayList<Book>();
    	
    	for(c.moveToPosition(-1); c.moveToNext(); c.isAfterLast()) {
    		Book book = cursorToBook(c);
    		output.add(book);
    	}
    	return output;
    }
    
    private Book cursorToBook(Cursor c) {
    	Book book = new Book();
    	
    	book.setSysno(getValue(c, KEY_SYSNO));
		book.setIsbn(getValue(c, KEY_ISBN));
		book.setTitle(getValue(c, KEY_TITLE));
		book.setAuthors(getAuthorsFromString(getValue(c, KEY_AUTHORS)));
		book.setPublisher(getValue(c, KEY_PUBLISHER));
		book.setCoverUrl(getValue(c, KEY_COVER_THUMB));
		book.setPublishedDate(getValue(c, KEY_PUBLISHED_DATE));
		book.setPreviewUrl(getValue(c, KEY_PDF_LINK));
		book.setPageCount(c.getInt(c.getColumnIndex(KEY_PAGE_COUNT)));
		book.setAverageRating(c.getDouble(c.getColumnIndex(KEY_AVERAGE_RATING)));
		book.setRatingCount(c.getInt(c.getColumnIndex(KEY_RATING_COUNT)));
		book.setCoverUrl(getValue(c, KEY_COVER_THUMB));
		book.setSaved(c.getInt(c.getColumnIndex(KEY_SAVED)));
		return book;
    }
    
    /**
     * Deserializes list of authors to insert to the db
     * 
     * */
    private String constructAuthorsString (List<Author> authors) {
    	if (authors == null || authors.isEmpty()) {
    		return "";
    	}
    	
    	StringBuilder builder = new StringBuilder();
    	if (authors.size() > 1) {
    		for (int i = 0; i < authors.size() - 1; i++) {
        		builder.append(authors.get(i).getName());
        		builder.append(STRING_SEPARATOR);
        	}
    	}
		builder.append(authors.get(authors.size() - 1).getName());
    	return builder.toString();
    }
    
    /**
     * Serializes authors string from db to list
     * 
     * */
    private List<Author> getAuthorsFromString (String authorsString) {
    	List<Author> authors = new ArrayList<Author>();
    	    	
    	if (Validator.isNullorEmpty(authorsString)) {
    		return authors;
    	}
    	
    	Author author;
    	String[] tmp = authorsString.split(STRING_SEPARATOR);
	    for (int i = 0; i < tmp.length; i++) {
	    	author = new Author();
	    	author.setName(tmp[i]);
	    	authors.add(author);
	    }
    	
    	return authors;
    }
    
    private void notifyListener(String action) {
    	Intent i = new Intent(action);
    	SmartLibMU.getAppContext().sendBroadcast(i);
    }
    
    

}
