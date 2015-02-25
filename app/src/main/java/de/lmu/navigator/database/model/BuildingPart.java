package de.lmu.navigator.database.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class BuildingPart extends RealmObject {

    @PrimaryKey
    private String code;

    private Building building;

    @Ignore
    private String buildingCode;

    @SerializedName("address")
    private String name;

    private RealmList<Floor> floors = new RealmList<>();

    public RealmList<Floor> getFloors() {
        return floors;
    }

    public void setFloors(RealmList<Floor> floors) {
        this.floors = floors;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }
}
