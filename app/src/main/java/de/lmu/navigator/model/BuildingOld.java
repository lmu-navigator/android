package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.lmu.navigator.search.Searchable;

@Deprecated
@Table(name = "building")
public class BuildingOld extends Model implements Parcelable, Searchable, ClusterItem {
    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_STREET_CODE = "StreetCode";
    public static final String COLUMN_DISPLAY_NAME = "DisplayName";
    public static final String COLUMN_COORD_LAT = "CoordLat";
    public static final String COLUMN_COORD_LONG = "CoordLong";
    public static final String COLUMN_STAR = "Star";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_STREET_CODE)
    private String streetCode;

    @Column(name = COLUMN_DISPLAY_NAME)
    private String displayName;

    @SerializedName("lat")
    @Column(name = COLUMN_COORD_LAT)
    private double coordLat;

    @SerializedName("lng")
    @Column(name = COLUMN_COORD_LONG)
    private double coordLong;

    @Column(name = COLUMN_STAR)
    private boolean star = false;

    public BuildingOld() {
    }

    public String getCode() {
        return code;
    }

    public String getStreetCode() {
        return streetCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getCoordLat() {
        return coordLat;
    }

    public double getCoordLong() {
        return coordLong;
    }

    public LatLng getLatLng() {
        return new LatLng(getCoordLat(), getCoordLong());
    }

    public String getDisplayNameFixed() {
        String formattedName = displayName
                .replace("STR.", "STRASSE")
                .replace(" - ", "-");

        formattedName = WordUtils.capitalizeFully(formattedName, '-', ' ');
        formattedName = formattedName
                .replace("strasse", "straße")
                .replace("Strasse", "Straße");

        // TODO: Fix all errors in data?

        return formattedName;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public void setFavorite(boolean favorite) {
        this.star = favorite;
        // unfortunately we cannot use save() here, because parcelable cannot pass the id
        new Update(BuildingOld.class)
                .set(COLUMN_STAR + "=?", star ? "1" : "0")
                .where(COLUMN_CODE + "=?", getCode())
                .execute();
    }

    public String getCityName() {
        return getStreet().getCity().getName();
    }

    public StreetOld getStreet() {
        return new Select().from(StreetOld.class).where(StreetOld.COLUMN_CODE + "=?", getStreetCode()).executeSingle();
    }

    public List<BuildingPartOld> getBuildingParts() {
        return new Select().from(BuildingPartOld.class).where(BuildingPartOld.COLUMN_BUILDING_CODE + "=?", getCode()).execute();
    }

    public static List<BuildingOld> getAll() {
        return new Select().from(BuildingOld.class).orderBy(COLUMN_DISPLAY_NAME + " ASC").execute();
    }

    public static List<BuildingOld> getFavorites() {
        return new Select().from(BuildingOld.class).where(COLUMN_STAR).orderBy(COLUMN_DISPLAY_NAME + " ASC").execute();
    }

    public List<FloorOld> getFloors() {
        return new Select().from(FloorOld.class)
                .join(BuildingPartOld.class).on("floor." + FloorOld.COLUMN_BUILDINGPART_CODE + "=building_part." + BuildingPartOld.COLUMN_CODE)
                .where(BuildingPartOld.COLUMN_BUILDING_CODE + "=?", getCode())
                .execute();
    }

    public List<RoomOld> getRooms() {
        return new Select().from(RoomOld.class)
                .join(FloorOld.class).on("room." + RoomOld.COLUMN_FLOOR_CODE + "=floor." + FloorOld.COLUMN_CODE)
                .join(BuildingPartOld.class).on("floor." + FloorOld.COLUMN_BUILDINGPART_CODE + "=building_part." + BuildingPartOld.COLUMN_CODE)
                .where(BuildingPartOld.COLUMN_BUILDING_CODE + "=?", getCode())
                .orderBy(RoomOld.COLUMN_NAME + " ASC")
                .execute();
    }

    public List<RoomOld> getRoomsIncludeAdjacent() {
        List<FloorOld> floors = getFloors();
        List<RoomOld> rooms = new ArrayList<RoomOld>();

        for (FloorOld f : floors) {
            rooms.addAll(f.getRoomsIncludeAdjacent());
        }

        Collections.sort(rooms);
        return rooms;
    }

    @Override
    public String getPrimaryText() {
        return getDisplayNameFixed();
    }

    @Override
    public String getSecondaryText() {
        return getCityName();
    }

    @Override
    public LatLng getPosition() {
        return getLatLng();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BuildingOld) {
            return ((BuildingOld) obj).getCode().equals(code);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.streetCode);
        dest.writeString(this.displayName);
        dest.writeDouble(this.coordLat);
        dest.writeDouble(this.coordLong);
        dest.writeByte(star ? (byte) 1 : (byte) 0);
    }

    private BuildingOld(Parcel in) {
        this.code = in.readString();
        this.streetCode = in.readString();
        this.displayName = in.readString();
        this.coordLat = in.readDouble();
        this.coordLong = in.readDouble();
        this.star = in.readByte() != 0;
    }

    public static final Creator<BuildingOld> CREATOR = new Creator<BuildingOld>() {
        public BuildingOld createFromParcel(Parcel source) {
            return new BuildingOld(source);
        }

        public BuildingOld[] newArray(int size) {
            return new BuildingOld[size];
        }
    };
}
