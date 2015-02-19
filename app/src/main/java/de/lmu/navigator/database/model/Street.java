package de.lmu.navigator.database.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Street extends RealmObject {

    @PrimaryKey
    private String code;

    private City city;

    @Ignore
    private String cityCode;

    private String name;

    private RealmList<Building> buildings = new RealmList<>();

    public RealmList<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(RealmList<Building> buildings) {
        this.buildings = buildings;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }
}
