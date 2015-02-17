package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Collections;
import java.util.List;

@Deprecated
@Table(name = "building_part")
public class BuildingPartOld extends Model implements Parcelable {
    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_BUILDING_CODE = "BuildingCode";
    public static final String COLUMN_ADDRESS = "Address";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_BUILDING_CODE)
    private String buildingCode;

    @Column(name = COLUMN_ADDRESS)
    private String address;

    public BuildingPartOld() {
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

    public BuildingOld getBuilding() {
        return new Select().from(BuildingOld.class).where(BuildingOld.COLUMN_CODE + "=?", getBuildingCode()).executeSingle();
    }

    public List<FloorOld> getFloors() {
        return new Select().from(FloorOld.class).where(FloorOld.COLUMN_BUILDINGPART_CODE + "=?", getCode()).execute();
    }

    public static List<BuildingPartOld> getAll() {
        return new Select().from(BuildingPartOld.class).execute();
    }

    public FloorOld getStartFloor() {
        List<FloorOld> floors = getFloors();
        return getStartFloor(floors);
    }

    public FloorOld getStartFloor(List<FloorOld> floors) {
        Collections.sort(floors);

        FloorOld start = floors.get(0);
        for (FloorOld f : floors) {
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

    private BuildingPartOld(Parcel in) {
        this.code = in.readString();
        this.buildingCode = in.readString();
        this.address = in.readString();
    }

    public static final Parcelable.Creator<BuildingPartOld> CREATOR = new Parcelable.Creator<BuildingPartOld>() {
        public BuildingPartOld createFromParcel(Parcel source) {
            return new BuildingPartOld(source);
        }

        public BuildingPartOld[] newArray(int size) {
            return new BuildingPartOld[size];
        }
    };
}
