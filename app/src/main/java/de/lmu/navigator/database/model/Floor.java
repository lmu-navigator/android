package de.lmu.navigator.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Floor extends RealmObject {

    @PrimaryKey
    private String code;

    private BuildingPart buildingPart;

    private String name;

    private String level;

    private String mapUri;

    private int mapSizeX;

    private int mapSizeY;

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
}
