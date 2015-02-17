package de.lmu.navigator.database;

import android.content.Context;

import de.lmu.navigator.database.model.Building;
import io.realm.Realm;

public class RealmDatabaseManager implements DatabaseManager {

    private Context mContext;

    public RealmDatabaseManager(Context context) {
        mContext = context;
    }

    private Realm getRealm() {
        return Realm.getInstance(mContext);
    }

    @Override
    public void setBuildingStarred(Building building, boolean starred) {
        Realm realm = getRealm();
        realm.beginTransaction();
        building.setStarred(starred);
        realm.commitTransaction();
    }

}
