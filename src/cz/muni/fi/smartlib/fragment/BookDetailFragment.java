package cz.muni.fi.smartlib.fragment;
import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cz.muni.fi.smartlib.R;
import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.cache.HttpImageManager;
import cz.muni.fi.smartlib.database.DBManager;
import cz.muni.fi.smartlib.loader.BookDetailsLoader;
import cz.muni.fi.smartlib.model.Book;
import cz.muni.fi.smartlib.utils.Utils;
import cz.muni.fi.smartlib.utils.Validator;

public class BookDetailFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Book> {	
	public static final String TAG = BookDetailFragment.class.getSimpleName();
	
	public interface BookUpdateListener {
				
	}

	public static final int BOOKS_DETAILS_LOADER = 0;
	
	public static final String PARAM_BOOK_NAME = "book";
	
	private TextView mTitle;
	private TextView mPublisher;
	private TextView mPublishedDate;
	private TextView mIsbnText;
	private TextView mPageCount;
	private TextView mLanguage;
	private TextView mAuthors;
	private ImageView mThumbnail;
	private ProgressBar mProgressBar;

	private BookUpdateListener mListener;
	
	private Book mBook;
	private HttpImageManager mImageManager;
	
	public static BookDetailFragment newInstance(Book book) {
		BookDetailFragment f = new BookDetailFragment();
	    Bundle args = new Bundle();
	    args.putParcelable(PARAM_BOOK_NAME, book);
	    f.setArguments(args);
	    return f;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (BookUpdateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BookUpdateListener");
        }
    }
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		
		if (args != null) {
			mBook = args.getParcelable(PARAM_BOOK_NAME);
		}
		mImageManager = ((SmartLibMU) getActivity().getApplication()).getHttpImageManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

		mTitle = (TextView) view.findViewById(R.id.display_book_title);
		mThumbnail = (ImageView) view.findViewById(R.id.display_book_thumbnail);
		mPublisher = (TextView) view.findViewById(R.id.display_book_publisher);
		mPublishedDate = (TextView) view.findViewById(R.id.display_book_published_date);
		mIsbnText = (TextView) view.findViewById(R.id.display_book_isbn);
		mPageCount = (TextView) view.findViewById(R.id.display_book_page_count);
		mLanguage = (TextView) view.findViewById(R.id.display_book_language);
		mAuthors = (TextView) view.findViewById(R.id.display_book_authors);
		mProgressBar = (ProgressBar) view.findViewById(R.id.display_book_progress);
		
		if (!Validator.isNullorEmpty(mBook.getTitle())) {
			mTitle.setText(mBook.getTitle());
		}
		if (!Validator.isNullorEmpty(mBook.getAuthors())) {
			mAuthors.setText(Utils.getAuthorsString(mBook.getAuthors()));
		}
		
		showThumbnail();
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_BOOK_NAME, mBook);
        
        getLoaderManager().initLoader(BOOKS_DETAILS_LOADER, bundle, this);
		
	}
	
	private void refreshBook() {
		if (!Validator.isNullorEmpty(mBook.getPublisher())) mPublisher.setText(mBook.getPublisher());
		if (!Validator.isNullorEmpty(mBook.getPublishedDate())) mPublishedDate.setText(mBook.getPublishedDate());
		if (!Validator.isNullorEmpty(mBook.getIsbn())) mIsbnText.setText(mBook.getIsbn());
		if ((mBook.getPageCount() != -1)) mPageCount.setText(String.valueOf(mBook.getPageCount()));
		if (!Validator.isNullorEmpty(mBook.getLanguage())) mLanguage.setText(mBook.getLanguage());
	}
	
	private void showThumbnail() {
		mThumbnail.setImageResource(R.drawable.no_cover_thumb);
		if (!Validator.isNullorEmpty(mBook.getCoverUrl())) {
			Uri imgUri = Uri.parse(mBook.getCoverUrl()); 
			if (imgUri != null) {
				Bitmap bitmap = mImageManager.loadImage(new HttpImageManager.LoadRequest(imgUri, mThumbnail));
				if (bitmap != null) {
					mThumbnail.setImageBitmap(bitmap);
				}
			}
			
		} else {
			mThumbnail.setImageResource(R.drawable.no_cover_thumb);
		} 
	}
	
	@Override
	public Loader<Book> onCreateLoader(int loader, Bundle bundle) {
		Log.i(TAG, "Loader created.");
		mProgressBar.setVisibility(View.VISIBLE);
		
		Book book = null;
    	if (bundle != null) {
    		book = bundle.getParcelable(PARAM_BOOK_NAME);
    	}
    	return new BookDetailsLoader(getActivity(), book);
		
	}

	@Override
	public void onLoadFinished(Loader<Book> listLoader, Book book) {
		mProgressBar.setVisibility(View.GONE);
		if (book != null) {
			mBook = book;	
		}
		refreshBook();
		DBManager.getInstance(getActivity()).insertViewedBook(mBook);
	}

	@Override
	public void onLoaderReset(Loader<Book> loader) {
    	mProgressBar.setVisibility(View.VISIBLE);
	}
	
}
