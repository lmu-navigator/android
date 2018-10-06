package de.lmu.navigator.database.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class City extends RealmObject {

    @Required
    @PrimaryKey
    private String code;

    @Required
    private String name;

    private RealmList<Street> streets = new RealmList<>();

    public RealmList<Street> getStreets() {
        return streets;
    }

    public void setStreets(RealmList<Street> streets) {
        this.streets = streets;
    }

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
