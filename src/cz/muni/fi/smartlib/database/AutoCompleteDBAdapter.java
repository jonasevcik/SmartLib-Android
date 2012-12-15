package cz.muni.fi.smartlib.database;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class AutoCompleteDBAdapter {
	public static final String TAG = AutoCompleteDBAdapter.class.getSimpleName();
	
	private DBHelper mDbHelper;
    protected SQLiteDatabase mDb;
    private final Activity mActivity;
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param activity
     *            the Activity that is using the database
     */
    public AutoCompleteDBAdapter(Activity activity) {
    	mActivity = activity;
        mDbHelper = new DBHelper(activity);
        mDb = mDbHelper.getWritableDatabase();
	}
    
    public Cursor getMatchingResult(String constraint, String columnName, String tableName) throws SQLException {

        String queryString =
                "SELECT DISTINCT _id, " + columnName + " FROM " + tableName;

        if (constraint != null) {
            constraint = constraint.trim() + "%";
            queryString += " WHERE " + columnName + " LIKE ?";
        }
        String params[] = { constraint };

        if (constraint == null) {
            params = null;
        }
        try {
            Cursor cursor = mDb.rawQuery(queryString, params);
            if (cursor != null) {
                this.mActivity.startManagingCursor(cursor);
                cursor.moveToFirst();
                return cursor;
            }
        }
        catch (SQLException e) {
            Log.e("AutoCompleteDbAdapter", e.toString());
            throw e;
        }

        return null;
    }
    
    /**
     * Closes the database.
     */
    public void close() {
        mDbHelper.close();
    }
}
