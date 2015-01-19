package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "street")
public class Street extends Model implements Parcelable {
    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_CITY_CODE = "CityCode";
    public static final String COLUMN_NAME = "Name";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_CITY_CODE)
    private String cityCode;

    @Column(name = COLUMN_NAME)
    private String name;

    public Street() {
    }

    public String getCode() {
        return code;
    }

    public String getCityCode() {
        return cityCode;
    }

    public String getName() {
        return name;
    }

    public City getCity() {
        return new Select().from(City.class).where(City.COLUMN_CODE + "=?", getCityCode()).executeSingle();
    }

    public List<Building> getBuildings() {
        return new Select().from(Building.class).where(Building.COLUMN_STREET_CODE + "=?", getCode()).execute();
    }

    public static List<Street> getAll() {
        return new Select().from(Street.class).execute();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.cityCode);
        dest.writeString(this.name);
    }

    private Street(Parcel in) {
        this.code = in.readString();
        this.cityCode = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Street> CREATOR = new Parcelable.Creator<Street>() {
        public Street createFromParcel(Parcel source) {
            return new Street(source);
        }

        public Street[] newArray(int size) {
            return new Street[size];
        }
    };
}
