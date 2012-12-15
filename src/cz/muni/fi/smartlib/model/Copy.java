package cz.muni.fi.smartlib.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Copy implements Parcelable{
	public static final String COPY = "copy";
	public static final String COPY_SYSNO = "sysno";
	
	private String sysno;	//primary key
	private String signature;
	private String barcode;
	private String last_check;
	private boolean status;
	private String col;
	private String panel;
	private String librarie;
	
    @SerializedName("return")
    private String checkReturn;
	
	public Copy() {
		
	}
	
	private Copy(Parcel in) {
		this();
    	readFromParcel(in);
    }
	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(sysno);
		dest.writeString(signature);
		dest.writeString(barcode);
		dest.writeString(last_check);
		dest.writeByte((byte) (status ? 1 : 0));     //if status == true, byte == 1
		dest.writeString(col);
		dest.writeString(panel);
		dest.writeString(librarie);
		dest.writeString(checkReturn);
	}
	
	private void readFromParcel(Parcel in) {
	      this.sysno = in.readString();
	      this.signature = in.readString();
	      this.barcode = in.readString();
	      this.last_check = in.readString();
	      this.status = in.readByte() == 1;     //myBoolean == true if byte == 1
	      this.col = in.readString();
	      this.panel = in.readString();
	      this.librarie = in.readString();
	      this.checkReturn = in.readString();
	}
	
	public static final Parcelable.Creator<Copy> CREATOR = new Parcelable.Creator<Copy>() {
        public Copy createFromParcel(Parcel in) {
            return new Copy(in);
        }

        public Copy[] newArray(int size) {
            return new Copy[size];
        }
    };
    
    @Override
    public int hashCode() {
    	int prime = 31;
    	return prime * Integer.parseInt(sysno) + ((barcode == null) ? 0 : barcode.hashCode());
    }
    
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Copy other = (Copy) obj;
		if (!sysno.equals(other.getSysno())) {
			return false;
		}
		return true;
	}

	
	
	@Override
	public int describeContents() {
		return 0;
	}

	public String getSysno() {
		return sysno;
	}

	public void setSysno(String sysno) {
		this.sysno = sysno;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getLastCheck() {
		return last_check;
	}

	public void setLastCheck(String lastCheck) {
		this.last_check = lastCheck;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public String getPanel() {
		return panel;
	}

	public void setPanel(String panel) {
		this.panel = panel;
	}

	public String getLibrary() {
		return librarie;
	}

	public void setLibrary(String library) {
		this.librarie = library;
	}



	public String getCheckReturn() {
		return checkReturn;
	}

	public void setCheckReturn(String checkReturn) {
		this.checkReturn = checkReturn;
	}
	
	
	
}
