package cz.muni.fi.smartlib.model;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable{
	public static final String BOOK = "book";
	public static final String BOOK_SYSNO = "sysno";
	public static final String BOOK_TITLE = "title";
	public static final String BOOK_AUTHOR = "author";
	
	private String sysno;	//primary key
	private String isbn;
	private String title;
	private List<Author> authors;
	private String publisher;
	private String publishedDate;
	private String language;
	private int pageCount;
	private String pageType;
	private String pageDesc;
	private String previewUrl;
	private List<Review> reviews;
	private String coverUrl;
	private double averageRating;
	private int ratingCount;
	private int saved;
	
	public Book(){
		sysno = "";
		isbn = "";
		title = "";
		authors = new ArrayList<Author>();
		publisher = "";
		publishedDate = "";
		language = "";
		pageCount = -1;
		pageType = "";
		pageDesc = "";
		previewUrl = "";
		reviews = new ArrayList<Review>();
		coverUrl = "";
		averageRating = -1;
		ratingCount = -1;
		saved = 0;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	private Book(Parcel in) {
		this();
    	readFromParcel(in);
    }
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(sysno);
		dest.writeString(isbn);
		dest.writeString(title);
		dest.writeTypedList(authors);
		dest.writeString(publisher);
		dest.writeString(publishedDate);
		dest.writeString(language);
		dest.writeInt(pageCount);
		dest.writeString(pageType);
		dest.writeString(pageDesc);
		dest.writeString(previewUrl);
		dest.writeTypedList(reviews);
		dest.writeString(coverUrl);
		dest.writeDouble(averageRating);
		dest.writeInt(ratingCount);
		dest.writeInt(saved);
	}
	
	private void readFromParcel(Parcel in) {
	      this.sysno = in.readString();
	      this.isbn = in.readString();
	      this.title = in.readString();
	      if (authors == null) {	   
	    	  authors = new ArrayList<Author>();
	      }
	      in.readTypedList(authors, Author.CREATOR);
	      this.publisher = in.readString();
	      this.publishedDate = in.readString();
	      this.language = in.readString();
	      this.pageCount = in.readInt();
	      this.pageType = in.readString();
	      this.pageDesc = in.readString();
	      this.previewUrl = in.readString();
	      if (reviews == null) {	   
	    	  reviews = new ArrayList<Review>();
	      }
	      in.readTypedList(reviews, Review.CREATOR);	
	      this.coverUrl = in.readString();
	      this.averageRating = in.readDouble();
	      this.ratingCount = in.readInt();
	      this.saved = in.readInt();
	}
	
	public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
    
    @Override
    public int hashCode() {
    	int prime = 31;
    	return prime * Integer.parseInt(sysno) + ((publisher == null) ? 0 : publisher.hashCode());
    }
    
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		if (!sysno.equals(other.getSysno())) {
			return false;
		}
		return true;
	}

    
    /*
	 * Getters and Setters
	 * 
	 * */
	
	
	public String getSysno() {
		return sysno;
	}

	public void setSysno(String sysno) {
		this.sysno = sysno;
	}
	
	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(String publishedDate) {
		this.publishedDate = publishedDate;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	public String getPageDesc() {
		return pageDesc;
	}

	public void setPageDesc(String pageDesc) {
		this.pageDesc = pageDesc;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	
	public double getAverageRating() {
		return averageRating;
	}
	
	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}

	public int getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(int ratingCount) {
		this.ratingCount = ratingCount;
	}

	public int getSaved() {
		return saved;
	}

	public void setSaved(int saved) {
		this.saved = saved;
	}
	
}
