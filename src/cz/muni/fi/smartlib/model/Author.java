package cz.muni.fi.smartlib.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Author implements Parcelable {
	private String type;
	private String name;
	
	public Author() {
		type = null;
		name = null;
	}

	public Author(Parcel in) {
		this();
    	readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(name);
	}
	
	private void readFromParcel(Parcel in) {
	      this.type = in.readString();
	      this.name = in.readString();
	}
	
	public static final Parcelable.Creator<Author> CREATOR = new Parcelable.Creator<Author>() {
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
    
    @Override
    public int hashCode() {
    	int prime = 31;
    	int result = 1;
    	result = prime * result + ((type == null) ? 0 : type.hashCode());
    	result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Author other = (Author) obj;
		if (!type.equals(other.getType())) {
			return false;
		}
		if (!name.equals(other.getName())) {
			return false;
		}
		return true;
	}

    /*
     * Getters and Setters
     * 
     * */
    
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    
}
