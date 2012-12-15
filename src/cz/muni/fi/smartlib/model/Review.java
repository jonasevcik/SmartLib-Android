package cz.muni.fi.smartlib.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {
	private User user;
	private String text;
	private int rating;
	private String date;

	public Review() {
		user = null;
		text = null;
		rating = -1;
		date = null;
	}
	
	public Review(Parcel in) {
		this();
    	readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(user, flags);
		dest.writeString(text);
		dest.writeInt(rating);
		dest.writeString(date);
	}
	
	private void readFromParcel(Parcel in) {
	      this.user = in.readParcelable(User.class.getClassLoader());
	      this.text = in.readString();
	      this.rating = in.readInt();
	      this.date = in.readString();
	}
	
	public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
    
    
    @Override
    public int hashCode() {
    	int prime = 31;
    	int result = 1;
    	result = prime * result + ((user == null) ? 0 : user.hashCode());
    	result = prime * result + ((text == null) ? 0 : text.hashCode());
    	result = prime * result + rating;
    	return result;
    }
    
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Review other = (Review) obj;
		if (!user.equals(other.getUser())) {
			return false;
		}
		if (!text.equals(other.getText())) {
			return false;
		}
		if (rating != other.getRating()) {
			return false;
		}
		return true;
	}
	

	/*
	 * Getters and Setters
	 * 
	 * */
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	
}
