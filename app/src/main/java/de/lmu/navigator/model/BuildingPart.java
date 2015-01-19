package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Collections;
import java.util.List;

@Table(name = "building_part")
public class BuildingPart extends Model implements Parcelable {
    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_BUILDING_CODE = "BuildingCode";
    public static final String COLUMN_ADDRESS = "Address";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_BUILDING_CODE)
    private String buildingCode;

    @Column(name = COLUMN_ADDRESS)
    private String address;

    public BuildingPart() {
    }

    public String getCode() {
        return code;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public String getAddress() {
        return address;
    }

    public String getDisplayName() {
        int index = getAddress().indexOf("(");
        if (index < 0) {
            return "";
        }
        return getAddress().substring(index).replace("(", "").replace(")", "").trim();
    }

    public Building getBuilding() {
        return new Select().from(Building.class).where(Building.COLUMN_CODE + "=?", getBuildingCode()).executeSingle();
    }

    public List<Floor> getFloors() {
        return new Select().from(Floor.class).where(Floor.COLUMN_BUILDINGPART_CODE + "=?", getCode()).execute();
    }

    public static List<BuildingPart> getAll() {
        return new Select().from(BuildingPart.class).execute();
    }

    public Floor getStartFloor() {
        List<Floor> floors = getFloors();
        return getStartFloor(floors);
    }

    public Floor getStartFloor(List<Floor> floors) {
        Collections.sort(floors);

        Floor start = floors.get(0);
        for (Floor f : floors) {
            if (f.getName().equals("Erdgeschoss"))
                return f;
            if (!f.getName().endsWith("Untergeschoss") && f.compareTo(start) < 0)
                start = f;
        }

        return start;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.buildingCode);
        dest.writeString(this.address);
    }

    private BuildingPart(Parcel in) {
        this.code = in.readString();
        this.buildingCode = in.readString();
        this.address = in.readString();
    }

    public static final Parcelable.Creator<BuildingPart> CREATOR = new Parcelable.Creator<BuildingPart>() {
        public BuildingPart createFromParcel(Parcel source) {
            return new BuildingPart(source);
        }

        public BuildingPart[] newArray(int size) {
            return new BuildingPart[size];
        }
    };
}
