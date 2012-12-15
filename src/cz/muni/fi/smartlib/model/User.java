package cz.muni.fi.smartlib.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{
	private String uco;
	private String firstName;
	private String lastName;
	private String password;
	
	public User() {
		uco = null;
		firstName = null;
		lastName = null;
		password = null;
	}

	public User(Parcel in) {
		this();
    	readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uco);
		dest.writeString(firstName);
		dest.writeString(lastName);
		dest.writeString(password);
	}
	
	private void readFromParcel(Parcel in) {
	      this.uco = in.readString();
	      this.firstName = in.readString();
	      this.lastName = in.readString();
	      this.password = in.readString();
	}
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
	
    
    @Override
    public int hashCode() {
    	int prime = 31;
    	int result = 1;
    	result = prime * ((Integer.valueOf(uco) / 1000000 > 1 && Integer.valueOf(uco) / 1000000 < 10) ? Integer.valueOf(uco) : result);	//test if uco is six-digit number
    	result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
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
		User other = (User) obj;
		if (uco.equals(getUco())) {
			return false;
		}
		if (!lastName.equals(other.getLastName())) {
			return false;
		}
		return true;
	}
    
	
	/*
	 * Getters and Setters
	 * 
	 * */
	
	public String getUco() {
		return uco;
	}

	public void setUco(String uco) {
		this.uco = uco;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	
}
