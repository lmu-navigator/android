package de.lmu.navigator.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BuildingPart extends RealmObject {

    @PrimaryKey
    private String code;

    private Building building;

    private String address;

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
}
