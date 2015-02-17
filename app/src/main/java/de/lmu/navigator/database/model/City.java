package de.lmu.navigator.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class City extends RealmObject {

    @PrimaryKey
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
