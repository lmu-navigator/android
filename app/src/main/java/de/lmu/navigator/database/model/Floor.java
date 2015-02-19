package de.lmu.navigator.database.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Floor extends RealmObject {

    @PrimaryKey
    private String code;

    @SerializedName("ignore")
    private BuildingPart buildingPart;

    @Ignore
    @SerializedName("buildingPart")
    private String buildingPartCode;

    private String name;

    private String level;

    private String mapUri;

    private int mapSizeX;

    private int mapSizeY;

    private RealmList<Room> rooms = new RealmList<>();

    public RealmList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(RealmList<Room> rooms) {
        this.rooms = rooms;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BuildingPart getBuildingPart() {
        return buildingPart;
    }

    public void setBuildingPart(BuildingPart buildingPart) {
        this.buildingPart = buildingPart;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMapUri() {
        return mapUri;
    }

    public void setMapUri(String mapUri) {
        this.mapUri = mapUri;
    }

    public int getMapSizeX() {
        return mapSizeX;
    }

    public void setMapSizeX(int mapSizeX) {
        this.mapSizeX = mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
    }

    public void setMapSizeY(int mapSizeY) {
        this.mapSizeY = mapSizeY;
    }

    public String getBuildingPartCode() {
        return buildingPartCode;
    }

    public void setBuildingPartCode(String buildingPartCode) {
        this.buildingPartCode = buildingPartCode;
    }
}
