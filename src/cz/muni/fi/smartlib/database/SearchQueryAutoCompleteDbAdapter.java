package cz.muni.fi.smartlib.database;

import android.app.Activity;
import android.content.ContentValues;

public class SearchQueryAutoCompleteDbAdapter extends AutoCompleteDBAdapter {
	public static final String TAG = SearchQueryAutoCompleteDbAdapter.class.getSimpleName();
	
	
	public SearchQueryAutoCompleteDbAdapter(Activity activity) {
		super(activity);
	}

	public static final String TABLE_NAME = "searchquery";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_QUERY = "query";

    public boolean insertTitle(String title) {
    	boolean success = false;
    	try {
        	super.mDb.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(KEY_QUERY, title);

            success = super.mDb.insertOrThrow(TABLE_NAME, null, values) > -1;
            super.mDb.setTransactionSuccessful();
        } catch (Exception e) {
			// no need to do anything
		} finally {
        	super.mDb.endTransaction();
        }
        return success;
    }
}
