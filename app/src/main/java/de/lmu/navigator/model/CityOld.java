package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Deprecated
@Table(name = "city")
public class CityOld extends Model implements Parcelable {

    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_NAME = "Name";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_NAME)
    private String name;

    public CityOld() {
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<StreetOld> getStreets() {
        return new Select().from(StreetOld.class).where(StreetOld.COLUMN_CITY_CODE + "=?", getCode()).execute();
    }

    public static List<CityOld> getAll() {
        return new Select().from(CityOld.class).execute();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.name);
    }

    private CityOld(Parcel in) {
        this.code = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<CityOld> CREATOR = new Parcelable.Creator<CityOld>() {
        public CityOld createFromParcel(Parcel source) {
            return new CityOld(source);
        }

        public CityOld[] newArray(int size) {
            return new CityOld[size];
        }
    };
}
