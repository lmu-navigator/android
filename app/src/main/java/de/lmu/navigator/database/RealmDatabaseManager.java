package de.lmu.navigator.database;

import java.util.List;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RealmDatabaseManager {

    private Realm mRealm;

    public RealmDatabaseManager() {
        mRealm = Realm.getDefaultInstance();
    }

    public void close() {
        mRealm.close();
    }

    public RealmResults<Building> getAllBuildings(boolean sorted) {
        RealmResults<Building> buildings = mRealm.where(Building.class).findAll();
        if (sorted) {
            buildings.sort(ModelHelper.BUILDING_NAME);
        }
        return buildings;
    }

    public List<Building> getStarredBuildings(boolean sorted) {
        RealmResults<Building> buildings = mRealm.where(Building.class)
                .equalTo(ModelHelper.BUILDING_STARRED, true)
                .findAll();
        if (sorted) {
            buildings.sort(ModelHelper.BUILDING_NAME);
        }
        return buildings;
    }

    public Building getBuilding(String code) {
        return mRealm.where(Building.class)
                .equalTo(ModelHelper.BUILDING_CODE, code)
                .findFirst();
    }

    public void setBuildingStarred(Building building, boolean starred) {
        mRealm.beginTransaction();
        building.setStarred(starred);
        mRealm.commitTransaction();
    }

    public BuildingPart getBuildingPart(String code) {
        return mRealm.where(BuildingPart.class)
                .equalTo(ModelHelper.BUILDING_PART_CODE, code)
                .findFirst();
    }

    public Room getRoom(String code) {
        return mRealm.where(Room.class)
                .equalTo(ModelHelper.ROOM_CODE, code)
                .findFirst();
    }

    public List<Room> getRoomsForFloor(Floor f, boolean includeSameMap, boolean sorted) {
        RealmResults<Room> rooms;
        if (includeSameMap) {
            rooms = mRealm.where(Room.class)
                    .equalTo(ModelHelper.ROOM_FLOOR_MAP_URI, f.getMapUri())
                    .findAll();
        } else {
            rooms = mRealm.where(Room.class)
                    .equalTo(ModelHelper.ROOM_FLOOR_CODE, f.getCode())
                    .findAll();
        }
        if (sorted) {
            rooms.sort(ModelHelper.ROOM_NAME);
        }
        return rooms;
    }

    public List<Room> getRoomsForBuilding(Building b, boolean includeSameMap, boolean sorted) {
        RealmResults<Room> rooms;
        if (includeSameMap) {
            RealmQuery<Room> query = mRealm.where(Room.class);
            List<Floor> floors = getFloorsForBuilding(b);
            for (int i = 0; i < floors.size(); i++) {
                if (i != 0) {
                    query = query.or();
                }
                query = query.equalTo(ModelHelper.ROOM_FLOOR_MAP_URI, floors.get(i).getMapUri());
            }
            rooms = query.findAll();
        } else {
            rooms = mRealm.where(Room.class)
                    .equalTo(ModelHelper.ROOM_BUILDING_CODE, b.getCode())
                    .findAll();
        }
        if (sorted) {
            rooms.sort(ModelHelper.ROOM_NAME);
        }
        return rooms;
    }

    private List<Floor> getFloorsForBuilding(Building b) {
        return mRealm.where(Floor.class)
                .equalTo(ModelHelper.FLOOR_BUILDING_CODE, b.getCode())
                .findAll();
    }
}
