package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "city")
public class City extends Model implements Parcelable {

    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_NAME = "Name";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_NAME)
    private String name;

    public City() {
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<Street> getStreets() {
        return new Select().from(Street.class).where(Street.COLUMN_CITY_CODE + "=?", getCode()).execute();
    }

    public static List<City> getAll() {
        return new Select().from(City.class).execute();
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

    private City(Parcel in) {
        this.code = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        public City createFromParcel(Parcel source) {
            return new City(source);
        }

        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
