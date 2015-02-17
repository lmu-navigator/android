package de.lmu.navigator.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

import de.lmu.navigator.search.Searchable;

@Deprecated
@Table(name = "room")
public class RoomOld extends Model implements Parcelable, Searchable, Comparable<RoomOld> {
    public static final String COLUMN_CODE = "Code";
    public static final String COLUMN_FLOOR_CODE = "FloorCode";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_POS_X = "PosX";
    public static final String COLUMN_POS_Y = "PosY";

    @Column(name = COLUMN_CODE, unique = true)
    private String code;

    @Column(name = COLUMN_FLOOR_CODE)
    private String floorCode;

    @Column(name = COLUMN_NAME)
    private String name;

    @Column(name = COLUMN_POS_X)
    private int posX;

    @Column(name = COLUMN_POS_Y)
    private int posY;

    public RoomOld() {
    }

    public String getCode() {
        return code;
    }

    public String getFloorCode() {
        return floorCode;
    }

    public String getName() {
        return name;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public FloorOld getFloor() {
        return new Select().from(FloorOld.class).where(FloorOld.COLUMN_CODE + "=?", getFloorCode()).executeSingle();
    }

    public static List<RoomOld> getAll() {
        return new Select().from(RoomOld.class).execute();
    }

    @Override
    public String getPrimaryText() {
        return getName();
    }

    @Override
    public String getSecondaryText() {
        return getFloor().getDisplayName();
    }

    @Override
    public int compareTo(RoomOld another) {
        return getPrimaryText().compareTo(another.getPrimaryText());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.floorCode);
        dest.writeString(this.name);
        dest.writeInt(this.posX);
        dest.writeInt(this.posY);
    }

    private RoomOld(Parcel in) {
        this.code = in.readString();
        this.floorCode = in.readString();
        this.name = in.readString();
        this.posX = in.readInt();
        this.posY = in.readInt();
    }

    public static final Parcelable.Creator<RoomOld> CREATOR = new Parcelable.Creator<RoomOld>() {
        public RoomOld createFromParcel(Parcel source) {
            return new RoomOld(source);
        }

        public RoomOld[] newArray(int size) {
            return new RoomOld[size];
        }
    };
}
