package de.lmu.navigator.database.model;

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

    private String address;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }
}
