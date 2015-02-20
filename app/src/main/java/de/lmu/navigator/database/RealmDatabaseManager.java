package de.lmu.navigator.database;

import android.content.Context;

import java.util.List;

import de.lmu.navigator.database.model.Building;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmDatabaseManager implements DatabaseManager {

    private Realm mRealm;

    public RealmDatabaseManager(Context context) {
        mRealm = Realm.getInstance(context);
    }

    public void close() {
        mRealm.close();
    }

    @Override
    public List<Building> getAllBuildings(boolean sorted) {
        RealmResults<Building> buildings = mRealm.allObjects(Building.class);
        if (sorted) {
            buildings.sort(ModelHelper.BUILDING_NAME);
        }
        return buildings;
    }

    @Override
    public List<Building> getStarredBuildings(boolean sorted) {
        RealmResults<Building> buildings = mRealm.where(Building.class)
                .equalTo(ModelHelper.BUILDING_STARRED, true)
                .findAll();
        if (sorted) {
            buildings.sort(ModelHelper.BUILDING_NAME);
        }
        return buildings;
    }

    @Override
    public void setBuildingStarred(Building building, boolean starred) {
        mRealm.beginTransaction();
        building.setStarred(starred);
        mRealm.commitTransaction();
    }

}
