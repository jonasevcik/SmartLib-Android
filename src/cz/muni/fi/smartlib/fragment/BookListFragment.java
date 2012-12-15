 package cz.muni.fi.smartlib.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.actionbarsherlock.app.SherlockListFragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import cz.muni.fi.smartlib.BookDetailActivity;
import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.cache.HttpImageManager;
import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.loader.BooksDbLoader;
import cz.muni.fi.smartlib.loader.BooksNetLoader;
import cz.muni.fi.smartlib.loader.BooksNetLoader.OnLoadListener;
import cz.muni.fi.smartlib.loader.FancyLoader;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Utils;
import cz.muni.fi.smartlib.utils.Validator;

public class BookListFragment extends NetListFragment implements AbsListView.OnScrollListener, LoaderManager.LoaderCallbacks<List<Book>>, OnLoadListener {
	public static final String TAG = BookListFragment.class.getSimpleName();
	public static final boolean DEBUG = true;
	
	public interface BookListListener {
        public void onBookClicked(String sysno);
        
        public void onSearchBtnClicked();
        public void onScanBtnClicked();
        
    }
	
	public interface LoadingListener {
		public void onLoadingStatusChanged(boolean isLoading);
	}
	
	private BookListListener mListener;
	private HttpImageManager mImageManager;
	
	private List<Book> mBooks = new ArrayList<Book>();
	private BooksAdapter mAdapter = new BooksAdapter();
	
	private boolean mLoading;
	
	//kdyz bude mQuery null, overi se category (TOP, RECENTLY COMMENTED, FAVOURITES, HISTORY)
	public static final String PARAM_CATEGORY_NAME = "category";
	public static final String PARAM_QUERY_NAME = "query";
	
	public static final int PARAM_CATEGORY_TOP = 0;
	public static final int PARAM_CATEGORY_RECENTLY_RATED = 1;
	public static final int PARAM_CATEGORY_FAVOURITES = 2;
	public static final int PARAM_CATEGORY_HISTORY = 3;
	public static final int PARAM_CATEGORY_QUERY = 4;
	
	private int mCategory;
	private String mQuery;
	private int mLoaderId;
	

	public static BookListFragment newInstance(int category, String query) {
	    BookListFragment f = new BookListFragment();
	    Bundle args = new Bundle();
	    if (!Validator.isNullorEmpty(query)) {
		    args.putString(RESTServiceHelper.PARAM_QUERY, query);
		    args.putInt(PARAM_CATEGORY_NAME, PARAM_CATEGORY_QUERY);
	    } else {
	    	args.putInt(PARAM_CATEGORY_NAME, category);
	    }
	    f.setArguments(args);
	    if (DEBUG) Log.i(TAG, "+++ BookListFragment instantiated. Category: " + category + ", Query: " + query + " +++");
	    return f;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (BookListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BookListListener");
        }
    }
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.i(TAG, "+++ onCreate() called +++");
		//setRetainInstance(true);
		setHasOptionsMenu(true);
		Bundle args = getArguments();
		
		if (args != null) {
			mQuery = args.getString(RESTServiceHelper.PARAM_QUERY);
			if (!Validator.isNullorEmpty(mQuery)) {
				mCategory = PARAM_CATEGORY_QUERY;
			} else {
				mCategory = args.getInt(PARAM_CATEGORY_NAME);
			}
		}
		
		mLoaderId = (mCategory == PARAM_CATEGORY_FAVOURITES || mCategory == PARAM_CATEGORY_HISTORY)? FancyLoader.BOOKS_LOADER_DB : FancyLoader.BOOKS_LOADER_NET;
		
		mImageManager = ((SmartLibMU) getActivity().getApplication()).getHttpImageManager();
		setListAdapter(mAdapter);
		
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG) Log.i(TAG, "+++ onActivityCreated() called +++");
        //setEmptyText(getString(R.string.empty_book_list));
        
        if (mCategory == PARAM_CATEGORY_FAVOURITES) {
        	registerForContextMenu(getListView());
        }
        
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_CATEGORY_NAME, mCategory);
        bundle.putString(PARAM_QUERY_NAME, mQuery);
        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(mLoaderId, bundle, this);
	}
	
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (DEBUG) Log.i(TAG, "+++ onViewCreated() called +++");
        view.setBackgroundColor(getResources().getColor(R.color.background));

        final ListView listView = getListView();
        listView.setCacheColorHint(Color.WHITE);
        listView.setOnScrollListener(this);
        listView.setDivider(getActivity().getResources().getDrawable(R.color.text_light));
        listView.setDividerHeight(1);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Book b = mBooks.get(position);
        //UIUtils.makeToast(getActivity(), "Book " + b.getTitle() + "was selected.");
        Intent bookDetails = new Intent(getActivity(), BookDetailActivity.class);
        bookDetails.putExtra(RESTServiceHelper.PARAM_BOOK, b);
        startActivity(bookDetails);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.favourites_menu, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
    	if (DBManager.getInstance(getActivity()).deleteSavedBook(mBooks.get(index).getSysno())) {
			UIUtils.makeToast(getActivity(), R.string.toast_book_deleted_succ);
			return true;
		}
    	UIUtils.makeWarningToast(getActivity(), R.string.toast_book_deleted_fail);
    	return false;
	}
    
    @Override
	public Loader<List<Book>> onCreateLoader(int loader, Bundle bundle) {
    	if (DEBUG) Log.i(TAG, "+++ onCreateLoader(" + loader + ") called +++");
    	int category = -1;
    	String query = null;
    	if (bundle != null) {
    		category = bundle.getInt(PARAM_CATEGORY_NAME);
    		query = bundle.getString(PARAM_QUERY_NAME);
    	}
    	
    	Loader<List<Book>> bookLoader = null;
    	
    	switch (category) {
    		case PARAM_CATEGORY_FAVOURITES:
    			bookLoader = new BooksDbLoader(getActivity(), BooksDbLoader.BOOKS_DB_LOADER_TYPE_SAVED);
    			break;
    		case PARAM_CATEGORY_HISTORY:
    			bookLoader = new BooksDbLoader(getActivity(), BooksDbLoader.BOOKS_DB_LOADER_TYPE_VIEWED);
    			break;
    		case PARAM_CATEGORY_QUERY: 
    			bookLoader =  BooksNetLoader.newQueryLoader(getActivity(), query);
    			((BooksNetLoader)bookLoader).registerLoadStatusListener(this);
    			break;
    		case PARAM_CATEGORY_RECENTLY_RATED:
    			bookLoader =  BooksNetLoader.newRecentlyRatedLoader(getActivity());
    			((BooksNetLoader)bookLoader).registerLoadStatusListener(this);
    			break;
    		case PARAM_CATEGORY_TOP:
    		default:
    			bookLoader =  BooksNetLoader.newTopLoader(getActivity());
    			((BooksNetLoader)bookLoader).registerLoadStatusListener(this);
    	}
    	return bookLoader;
	}
    
    @Override
    public void onLoadFinished(Loader<List<Book>> listLoader, List<Book> books) {
    	if (DEBUG) Log.i(TAG, "+++ onLoadFinished() called +++");
    	mLoading = false;
    	if (books != null && !FancyLoader.loadingHasError(this, mLoaderId)) {
           mBooks.clear();
           mBooks.addAll(books);
        }
    	mAdapter.notifyDataSetChanged();
    }
    
    
    @Override
    public void onLoaderReset(Loader<List<Book>> listLoader) {
    	if (DEBUG) Log.i(TAG, "+++ onLoaderRestart() called +++");
    	mBooks.clear();
    	mAdapter.notifyDataSetChanged();
    }

    public void refreshLoader() {
    	Bundle bundle = new Bundle();
        bundle.putInt(PARAM_CATEGORY_NAME, mCategory);
        bundle.putString(PARAM_QUERY_NAME, mQuery);
    	mBooks.clear();
    	mAdapter.notifyDataSetChanged();
    	getLoaderManager().restartLoader(mLoaderId, bundle, this);
        
    }
    
    public void loadMoreResults() {
        if (isAdded()) {
            Loader<List<Book>> loader = getLoaderManager().getLoader(mLoaderId);
            if (loader != null) {
                loader.forceLoad();
            }
        }
    }
    
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
        if (!FancyLoader.isLoading(this, mLoaderId)
        		&& !FancyLoader.loadingHasError(this, mLoaderId)
                && FancyLoader.hasMoreResult(this, mLoaderId)
                && FancyLoader.isOnline(this, mLoaderId)
                && visibleItemCount != 0
                && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
        	if (DEBUG) Log.i(TAG, "--- It is about to call loadMoreResults(): " + !FancyLoader.isLoading(this, mLoaderId)
                    + FancyLoader.hasMoreResult(this, mLoaderId)
                    + FancyLoader.isOnline(this, mLoaderId)
                    + (visibleItemCount != 0)
                    + (firstVisibleItem + visibleItemCount >= totalItemCount - 1) + " +++");
            loadMoreResults();
        }
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		if (DEBUG) Log.i(TAG, "+++ onOptionsItemSelected: "+ item.getTitle() + " +++");
		switch(item.getItemId()) {
			case android.R.id.home:
				Utils.startMainActivity(getActivity());
				break;
			case R.id.menu_refresh:
				refreshLoader();
				break;
			case R.id.menu_search:
				mListener.onSearchBtnClicked();
				break;
			case R.id.menu_scan:
				if (mOnline) {
					mListener.onScanBtnClicked();
				} else {
					//TODO: toast 
				}
				break;
		}
		return true;
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

	private class BooksAdapter extends BaseAdapter {
		public final String TAG =  BooksAdapter.class.getSimpleName();
        public final boolean DEBUG = true;
		
		private static final int VIEW_TYPE_BOOK = 0;
        private static final int VIEW_TYPE_LOADING = 1;

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == VIEW_TYPE_BOOK;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getCount() {
        	if (DEBUG) Log.i(TAG, "<<< getCount called >>>");
            return mBooks.size() + (
                    // show the status list row if...
                   // ((FancyLoader.isLoading(BookListFragment.this, mLoaderId) && mBooks.size() == 0) // ...this is the first load
                     	((mBooks.size() == 0)
            				|| FancyLoader.hasMoreResult(BookListFragment.this, mLoaderId) // ...or there's another page
                            || FancyLoader.loadingHasError(BookListFragment.this, mLoaderId)) // ...or there's an error
                            ? 1 : 0);
        }

		@Override
        public int getItemViewType(int position) {
            return (position >= mBooks.size())
                    ? VIEW_TYPE_LOADING
                    : VIEW_TYPE_BOOK;
        }

        @Override
        public Object getItem(int position) {
            return (getItemViewType(position) == VIEW_TYPE_BOOK)
                    ? mBooks.get(position)
                    : null;
        }

        @Override
        public long getItemId(int position) {
            // TODO: better unique ID heuristic
            return (getItemViewType(position) == VIEW_TYPE_BOOK)
                    ? Long.parseLong(mBooks.get(position).getSysno()) : -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	if (DEBUG) Log.i(TAG, "<<< getView() called. Position: " + position + ", convertView: " + convertView+" >>>");
            if (getItemViewType(position) == VIEW_TYPE_LOADING) {
                if (convertView == null) {
                    convertView = getLayoutInflater(null).inflate(
                            R.layout.list_book_status, parent, false);
                }

                if (!FancyLoader.isOnline(BookListFragment.this, mLoaderId)) {	//no internet
                	convertView.findViewById(android.R.id.progress).setVisibility(View.GONE);
                    ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                            R.string.no_internet);
                    ((Button) convertView.findViewById(R.id.retry)).setVisibility(View.GONE);
                    return convertView;
                }
                
                if (FancyLoader.loadingHasError(BookListFragment.this, mLoaderId)) {	//error
                    convertView.findViewById(android.R.id.progress).setVisibility(View.GONE);
                    ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                            R.string.loading_error);
                    ((Button) convertView.findViewById(R.id.retry)).setVisibility(View.VISIBLE);
                    ((Button) convertView.findViewById(R.id.retry)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							loadMoreResults();
						}
					});
                    
                    return convertView;
                } 
                if (!mLoading && !FancyLoader.loadingHasError(BookListFragment.this, mLoaderId)) {
                	convertView.findViewById(android.R.id.progress).setVisibility(View.GONE);	//empty list
                    ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                            R.string.empty_book_list);
                    ((Button) convertView.findViewById(R.id.retry)).setVisibility(View.GONE);
                
                } else {
                	convertView.findViewById(android.R.id.progress).setVisibility(View.VISIBLE);	//loading
                    ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                            R.string.loading);	
                    ((Button) convertView.findViewById(R.id.retry)).setVisibility(View.GONE);
                }

                return convertView;

            } else {
                Book book = (Book) getItem(position);
                if (convertView == null) {
                    convertView = getLayoutInflater(null).inflate(
                            R.layout.list_book_item, parent, false);
                }

                BookRowBinder.bindBookView(convertView, book, mImageManager);
                return convertView;
            }
        }
        
    }
    

    
	private static class BookRowBinder {
		
		private static class ViewHolder {
			private ImageView thumbnail;
			private TextView title;
			private TextView author;
			private RatingBar averageRating;
		}
		
	
		private static void bindBookView (final View rootView, Book book, HttpImageManager imageManager) {
			ViewHolder temp = (ViewHolder) rootView.getTag();
			final ViewHolder views;
			if (temp != null) {
				views = temp;
			} else {
				views = new ViewHolder();
				rootView.setTag(views);

				views.author = (TextView) rootView.findViewById(R.id.list_item_author);
				views.title = (TextView) rootView.findViewById(R.id.list_item_title);
				views.averageRating = (RatingBar) rootView.findViewById(R.id.list_item_rating);
				views.thumbnail = (ImageView) rootView.findViewById(R.id.list_item_image);
				
			}
			
			
			//set authors
			String authors = "";
			if (book.getAuthors().size() > 1) {
				for (int i = 0; i < book.getAuthors().size() - 1; i++) {
					authors += (book.getAuthors().get(i).getName() + ", ");
				}
				authors += book.getAuthors().get(book.getAuthors().size() - 1).getName();
			} else {
				if (!book.getAuthors().isEmpty()) {
					authors = book.getAuthors().get(0).getName();
				}
			}
			views.author.setText(authors);
			
			//set title
			views.title.setText(book.getTitle());
			
			//set average rating
			views.averageRating.setRating((float)book.getAverageRating());

			//set thumbnail
			views.thumbnail.setImageResource(R.drawable.no_cover_thumb);
			if (!Validator.isNullorEmpty(book.getCoverUrl())) {
				Uri imgUri = Uri.parse(book.getCoverUrl()); 
				if (imgUri != null) {
					Bitmap bitmap = imageManager.loadImage(new HttpImageManager.LoadRequest(imgUri, views.thumbnail));
					if (bitmap != null) {
						views.thumbnail.setImageBitmap(bitmap);
					}
				}
				
			} else {
				views.thumbnail.setImageResource(R.drawable.no_cover_thumb);
			}
		}
		
	}
	
	public BooksAdapter getBookAdapter() {
		return mAdapter;
	}

	@Override
	protected void onConnectivityChanged(boolean isConnected) {
//		if (DEBUG) Log.i(TAG, "+++ onConnectivityChanged() called +++");
//		if (mAdapter != null) {
//			mAdapter.notifyDataSetChanged();	//change text status of list to loading...
//		}
	}

	@Override
	public void onStartLoading() {
		if (DEBUG) Log.i(TAG, "+++ onStartLoading() --notifying fragment from loader +++");
		mLoading = true;
		mAdapter.notifyDataSetChanged();
	}
	
       
}
