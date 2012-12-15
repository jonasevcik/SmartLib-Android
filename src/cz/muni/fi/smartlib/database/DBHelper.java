package cz.muni.fi.smartlib.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	public static final String TAG = DBHelper.class.getSimpleName();
	
	public static final String DATABASE_NAME = "smartlib";

    public static final int DATABASE_VERSION = 1;
    
    private static final String CREATE_TABLE_SEARCH_QUERY = 
    	"create table " + SearchQueryAutoCompleteDbAdapter.TABLE_NAME 
    	+ " (" + SearchQueryAutoCompleteDbAdapter.KEY_ROWID + " integer primary key autoincrement"
    	+ ", " + SearchQueryAutoCompleteDbAdapter.KEY_QUERY + " text not null unique);";
    
    private static final String CREATE_TABLE_BOOK =
    	"create table " + DBManager.TABLE_NAME
        + "("+DBManager.KEY_SYSNO +" text primary key"
        + ", "+DBManager.KEY_ISBN +" text"
        + ", "+DBManager.KEY_TITLE +" text not null"
        + ", "+DBManager.KEY_AUTHORS +" text not null"
        + ", "+DBManager.KEY_PUBLISHER +" text"
        + ", "+DBManager.KEY_PUBLISHED_DATE +" text"
        + ", "+DBManager.KEY_COVER_THUMB +" text"
        + ", "+DBManager.KEY_PDF_LINK +" text"
        + ", "+DBManager.KEY_PAGE_COUNT +" integer"
        + ", "+DBManager.KEY_AVERAGE_RATING +" real"
        + ", "+DBManager.KEY_LAST_MODIFIED +" long not null"
        + ", "+DBManager.KEY_SAVED +" integer not null"
    	+ ", "+DBManager.KEY_RATING_COUNT +" integer)";

    	protected DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION+1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	db.execSQL(CREATE_TABLE_SEARCH_QUERY);
            db.execSQL(CREATE_TABLE_BOOK);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
        	db.execSQL("DROP TABLE IF EXISTS " + SearchQueryAutoCompleteDbAdapter.TABLE_NAME);
        	db.execSQL("DROP TABLE IF EXISTS " + DBManager.TABLE_NAME);
            onCreate(db);
        }
}
