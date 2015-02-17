package de.lmu.navigator.database;

import android.content.Context;

import java.util.List;

import de.lmu.navigator.database.model.Building;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmDatabaseManager implements DatabaseManager {

    private Context mContext;

    public RealmDatabaseManager(Context context) {
        mContext = context;
    }

    private Realm getRealm() {
        return Realm.getInstance(mContext);
    }

    @Override
    public List<Building> getAllBuildings(boolean sorted) {
        Realm realm = getRealm();
        RealmResults<Building> buildings = realm.allObjects(Building.class);
        if (sorted) {
            buildings.sort(ModelHelper.BUILDING_NAME);
        }
        realm.close();
        return buildings;
    }

    @Override
    public List<Building> getStarredBuildings(boolean sorted) {
        Realm realm = getRealm();
        RealmResults<Building> buildings = realm.where(Building.class)
                .equalTo(ModelHelper.BUILDING_STARRED, true)
                .findAll();
        if (sorted) {
            buildings.sort(ModelHelper.BUILDING_NAME);
        }
        realm.close();
        return buildings;
    }

    @Override
    public void setBuildingStarred(Building building, boolean starred) {
        Realm realm = getRealm();
        realm.beginTransaction();
        building.setStarred(starred);
        realm.commitTransaction();
        realm.close();
    }

}
