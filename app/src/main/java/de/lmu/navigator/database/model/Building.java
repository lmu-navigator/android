package de.lmu.navigator.database.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Building extends RealmObject {

    @PrimaryKey
    private String code;

    private Street street;

    private String displayName;

    @SerializedName("lat")
    private double coordLat;

    @SerializedName("lng")
    private double coordLong;

    private boolean starred = false;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getCoordLat() {
        return coordLat;
    }

    public void setCoordLat(double coordLat) {
        this.coordLat = coordLat;
    }

    public double getCoordLong() {
        return coordLong;
    }

    public void setCoordLong(double coordLong) {
        this.coordLong = coordLong;
    }

    public LatLng getLatLng() {
        return new LatLng(getCoordLat(), getCoordLong());
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }
}
