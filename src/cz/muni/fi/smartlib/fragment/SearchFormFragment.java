package cz.muni.fi.smartlib.fragment;

import java.util.Observable;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.database.AutoCompleteDBAdapter;
import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.database.SearchQueryAutoCompleteDbAdapter;
import cz.muni.fi.smartlib.loader.SearchQueryLoader;
import cz.muni.fi.smartlib.utils.ConnectivityObserver;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Validator;

public class SearchFormFragment extends SherlockFragment implements LoaderCallbacks<String[]> {
	public static final String TAG = SearchFormFragment.class.getSimpleName();
	public static final boolean DEBUG = true;
	
	public static final int SEARCH_QUERY_LOADER_ID = 0;
	
	public interface SearchBookListener {
        public void onSearchStarted(String query);
        public void onScanClicked();
    }
	
	final static int[] to = new int[] { android.R.id.text1 };
	
	private AutoCompleteTextView mSearchEditText;
	private Button mSearchBtn;
	private SearchBookListener mListener;
	private boolean mOnline;
	private InternetObserver mObserver;
	
	private ArrayAdapter<String> mAdapter;
	
	public static SearchFormFragment newInstance() {
		SearchFormFragment f = new SearchFormFragment();
	    return f;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SearchBookListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SearchBookListener");
        }
        mObserver = new InternetObserver(getActivity());
        mOnline = ((SmartLibMU)getActivity().getApplicationContext()).isOnline();
    }
	
	@Override
	public void onDetach() {
		if (mObserver != null) {
			mObserver.unregister(getActivity());
			mObserver = null;
		}
		super.onDetach();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_search_form, container, false);
		
		mSearchEditText = (AutoCompleteTextView) view.findViewById(R.id.fragment_search_form_search_text_view);
		mSearchEditText.setTextColor(Color.parseColor("#686868"));
		mSearchBtn = (Button) view.findViewById(R.id.fragment_search_form_search_button);
		
		mSearchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//schovat klavesnici
				InputMethodManager imm = (InputMethodManager) v.getContext()
			            .getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				search();
			}
		});
		mSearchBtn.setEnabled(false);
		
		return view;
	}
	
	
	private boolean mItemSelected;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line);
		
		mSearchEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}		
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				//do nothing
			}		
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() != 0 && mOnline) {
					mSearchBtn.setEnabled(true);
				} else {
					mSearchBtn.setEnabled(false);
				}
				

				if (DEBUG) Log.i(TAG, "<<< onTextChanged: mItemSelected = "+ mItemSelected + " >>>");
				if (!mItemSelected) {
					mAdapter.getFilter().filter(s);
				} 
				mItemSelected = false;
			}
		});
		
		mSearchEditText.setAdapter(mAdapter);
		mSearchEditText.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (DEBUG) Log.i(TAG, "<<< onClick >>>");
				mItemSelected = true;
				
			}
		});
        getLoaderManager().initLoader(SEARCH_QUERY_LOADER_ID, null, this);
		
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override 
	public void onStop() {
		super.onStop();
	}
	
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (inflater != null) {
			inflater.inflate(R.menu.search, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_scan:
				if (mOnline)
					mListener.onScanClicked();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private void search() {
		
		if (!mOnline) {
			UIUtils.makeWarningToast(getActivity(), R.string.dialog_text_no_internet_conection);
			return;
		}
		
		String searchQuery = mSearchEditText.getText().toString().trim();
		
		//save parameters for autocomplete
		if (!Validator.isNullorEmpty(searchQuery)) {
			DBManager.getInstance(getActivity()).insertQuery(searchQuery);
			
			mSearchBtn.setEnabled(false);
			mListener.onSearchStarted(searchQuery);
		}
		
	}
	
	public class InternetObserver extends ConnectivityObserver {

		public InternetObserver(Context context) {
			super(context);
		}

		@Override
		public void update(Observable observable, Object data) {
			mOnline = (Boolean) data;
			mSearchBtn.setEnabled(mOnline);
		}
		
	}

	@Override
	public Loader<String[]> onCreateLoader(int arg0, Bundle arg1) {
		return new SearchQueryLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<String[]> loader, String[] result) {
		if (result != null) {
			if (mAdapter != null) {
				mAdapter.clear();
				addAllToAdapter(mAdapter, result);
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onLoaderReset(Loader<String[]> arg0) {
		if (mAdapter != null) {
			mAdapter.notifyDataSetInvalidated();
		}
	} 
	
	private void addAllToAdapter(ArrayAdapter<String> adapter, String[] queries) {
		for (int i = 0; i < queries.length; i++) {
			adapter.add(queries[i]);
		}
	}

}
