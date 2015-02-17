package de.lmu.navigator.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Room extends RealmObject {

    @PrimaryKey
    private String code;

    private Floor floor;

    private String name;

    private int posX;

    private int posY;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

}
