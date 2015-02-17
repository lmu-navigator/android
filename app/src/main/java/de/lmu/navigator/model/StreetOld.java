package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Deprecated
@Table(name = "street")
public class StreetOld extends Model implements Parcelable {
    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_CITY_CODE = "CityCode";
    public static final String COLUMN_NAME = "Name";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_CITY_CODE)
    private String cityCode;

    @Column(name = COLUMN_NAME)
    private String name;

    public StreetOld() {
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

    public CityOld getCity() {
        return new Select().from(CityOld.class).where(CityOld.COLUMN_CODE + "=?", getCityCode()).executeSingle();
    }

    public List<BuildingOld> getBuildings() {
        return new Select().from(BuildingOld.class).where(BuildingOld.COLUMN_STREET_CODE + "=?", getCode()).execute();
    }

    public static List<StreetOld> getAll() {
        return new Select().from(StreetOld.class).execute();
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

    private StreetOld(Parcel in) {
        this.code = in.readString();
        this.cityCode = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<StreetOld> CREATOR = new Parcelable.Creator<StreetOld>() {
        public StreetOld createFromParcel(Parcel source) {
            return new StreetOld(source);
        }

        public StreetOld[] newArray(int size) {
            return new StreetOld[size];
        }
    };
}
