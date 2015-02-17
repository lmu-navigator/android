package de.lmu.navigator.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Street extends RealmObject {

    @PrimaryKey
    private String code;

    private City city;

    private String name;

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
}
