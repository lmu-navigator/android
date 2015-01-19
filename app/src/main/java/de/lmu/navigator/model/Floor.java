package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

@Table(name = "floor")
public class Floor extends Model implements Parcelable, Comparable<Floor> {

    private static final String TILES_BASE_PATH = "http://141.84.213.246/navigator/tiles/";

    public static final List<String> FLOOR_ORDER = Arrays.asList("2.Untergeschoss",
            "1.Untergeschoss", "Erdgeschoss", "Zwischengeschoss", "1.Obergeschoss",
            "1.Zwischengeschoss", "2.Obergeschoss", "2.Zwischengeschoss", "3.Obergeschoss",
            "4.Obergeschoss", "5.Obergeschoss", "6.Obergeschoss");

    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_BUILDINGPART_CODE = "BuildingPartCode";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_LEVEL = "FloorLevel";
    public static final String COLUMN_MAP_URI = "MapUri";
    public static final String COLUMN_SIZE_X = "MapSizeX";
    public static final String COLUMN_SIZE_Y = "MapSizeY";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @SerializedName("buildingPart")
    @Column(name = COLUMN_BUILDINGPART_CODE)
    private String buildingPartCode;

    @Column(name = COLUMN_NAME)
    private String name;

    @Column(name = COLUMN_LEVEL)
    private String level;

    @Column(name = COLUMN_MAP_URI)
    private String mapUri;

    @Column(name = COLUMN_SIZE_X)
    private int mapSizeX;

    @Column(name = COLUMN_SIZE_Y)
    private int mapSizeY;

    public Floor() {
    }

    public String getCode() {
        return code;
    }

    public String getBuildingPartCode() {
        return buildingPartCode;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getMapUri() {
        return mapUri;
    }

    public int getMapSizeX() {
        return mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
    }

    public String getSamplePath() {
        return "samples/" + mapUri.split("\\.")[0] + ".png";
    }

    public String getDetailLevelTilesPath(String detailLevel) {
        return TILES_BASE_PATH + mapUri.split("\\.")[0] + "/" + detailLevel
                + "/%col%/%row%.png";
    }

    public BuildingPart getBuildingPart() {
        return new Select().from(BuildingPart.class).where(BuildingPart.COLUMN_CODE + "=?", getBuildingPartCode()).executeSingle();
    }

    public List<Room> getRooms() {
        return new Select().from(Room.class).where(Room.COLUMN_FLOOR_CODE + "=?", getCode()).execute();
    }

    public List<Floor> getFloorsWithSameMap() {
        return new Select().from(Floor.class).where(COLUMN_MAP_URI + "=?", getMapUri()).execute();
    }

    public List<Room> getRoomsIncludeAdjacent() {
        StringBuilder inList = new StringBuilder("(");
        for (Floor f : getFloorsWithSameMap()) {
            inList.append("'").append(f.code).append("'").append(",");
        }
        inList.deleteCharAt(inList.length() - 1).append(")");

        return new Select().from(Room.class).where(Room.COLUMN_FLOOR_CODE + " in " + inList.toString()).execute();
    }

    public static List<Floor> getAll() {
        return new Select().from(Floor.class).execute();
    }

    public String getDisplayName() {
        return name.replace(".", ". ");
    }

    public String getShortName() {
        if (name.equals("2.Untergeschoss"))
            return "UG2";
        if (name.equals("1.Untergeschoss"))
            return "UG1";
        if (name.equals("Erdgeschoss"))
            return "EG";
        if (name.equals("1.Obergeschoss"))
            return "OG1";
        if (name.equals("Zwischengeschoss"))
            return "ZG";
        if (name.equals("2.Obergeschoss"))
            return "OG2";
        if (name.equals("1.Zwischengeschoss"))
            return "ZG1";
        if (name.equals("3.Obergeschoss"))
            return "OG3";
        if (name.equals("2.Zwischengeschoss"))
            return "ZG2";
        if (name.equals("4.Obergeschoss"))
            return "OG4";
        if (name.equals("5.Obergeschoss"))
            return "OG5";
        if (name.equals("6.Obergeschoss"))
            return "OG6";
        return level;
    }

    @Override
    public int compareTo(Floor another) {
        int i = FLOOR_ORDER.indexOf(name);
        int i2 = FLOOR_ORDER.indexOf(another.getName());

        if (i == i2)
            return 0;
        if (i < i2)
            return -1;

        return 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.buildingPartCode);
        dest.writeString(this.name);
        dest.writeString(this.level);
        dest.writeString(this.mapUri);
        dest.writeInt(this.mapSizeX);
        dest.writeInt(this.mapSizeY);
    }

    private Floor(Parcel in) {
        this.code = in.readString();
        this.buildingPartCode = in.readString();
        this.name = in.readString();
        this.level = in.readString();
        this.mapUri = in.readString();
        this.mapSizeX = in.readInt();
        this.mapSizeY = in.readInt();
    }

    public static final Parcelable.Creator<Floor> CREATOR = new Parcelable.Creator<Floor>() {
        public Floor createFromParcel(Parcel source) {
            return new Floor(source);
        }

        public Floor[] newArray(int size) {
            return new Floor[size];
        }
    };
}
