package de.lmu.navigator.update;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.common.collect.ImmutableList;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.lmu.navigator.app.Preferences;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.City;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.database.model.Street;
import de.lmu.navigator.database.model.Version;
import io.realm.Realm;
import io.realm.RealmResults;
import me.alexrs.prefs.lib.Prefs;

public class UpdateService extends IntentService {

    private static final String LOG_TAG = UpdateService.class.getSimpleName();

    private List<String> mFavorites = ImmutableList.of();

    public UpdateService() {
        super(UpdateService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getInstance(this);
        RestService restService = RetrofitRestClient.create();

        // try to remember favorites and restore them later
        RealmResults<Building> favBuildings = realm.where(Building.class)
                .equalTo(ModelHelper.BUILDING_STARRED, true)
                .findAll();
        for (Building b : favBuildings) {
            mFavorites.add(b.getCode());
        }

        Log.d(LOG_TAG, "Starting update. Old version: " + Prefs.with(this)
                .getInt(Preferences.DATA_VERSION, Preferences.SHIPPED_DATA_VERSION));

        try {
            // Download data
            Log.d(LOG_TAG, "Download data...");
            List<City> cities = restService.getCities();
            List<Street> streets = restService.getStreets();
            List<Building> buildings = restService.getBuildings();
            List<BuildingPart> buildingParts = restService.getBuildingParts();
            List<Floor> floors = restService.getFloors();
            List<Room> rooms = restService.getRooms();
            Version version = restService.getVersion();

            // Setup relationships
            Log.d(LOG_TAG, "Setup relationships...");
            for (Street s : streets) {
                for (City c : cities) {
                    if (c.getCode().equals(s.getCityCode())) {
                        c.getStreets().add(s);
                        s.setCity(c);
                        break;
                    }
                }
            }
            for (Building b : buildings) {
                // restore favorites
                b.setStarred(mFavorites.contains(b.getCode()));
                // fix building names
                b.setDisplayName(ModelHelper.getBuildingNameFixed(b.getDisplayName()));
                // TODO: testing only, remove!
                b.setStarred(Math.random() > 0.8);
                for (Street s : streets) {
                    if (s.getCode().equals(b.getStreetCode())) {
                        s.getBuildings().add(b);
                        b.setStreet(s);
                        break;
                    }
                }
            }
            for (BuildingPart p : buildingParts) {
                for (Building b : buildings) {
                    if (b.getCode().equals(p.getBuildingCode())) {
                        b.getBuildingParts().add(p);
                        p.setBuilding(b);
                        break;
                    }
                }
            }
            for (Floor f : floors) {
                for (BuildingPart p : buildingParts) {
                    if (p.getCode().equals(f.getBuildingPartCode())) {
                        p.getFloors().add(f);
                        f.setBuildingPart(p);
                        break;
                    }
                }
            }
            for (Room r : rooms) {
                for (Floor f : floors) {
                    if (f.getCode().equals(r.getFloorCode())) {
                        f.getRooms().add(r);
                        r.setFloor(f);
                        break;
                    }
                }
            }

            // Update database
            Log.d(LOG_TAG, "Update database...");
            realm.beginTransaction();
            try {
                // Delete old data
                realm.allObjects(City.class).clear();
                realm.allObjects(Street.class).clear();
                realm.allObjects(Building.class).clear();
                realm.allObjects(BuildingPart.class).clear();
                realm.allObjects(Floor.class).clear();
                realm.allObjects(Room.class).clear();

                // Save new data
                // Inserting cities will insert all data due to relationships
                realm.copyToRealmOrUpdate(cities);

                realm.commitTransaction();

                Prefs.with(this).save(Preferences.DATA_VERSION, version.version);
                Prefs.with(this).save(Preferences.UPDATE_PENDING, false);
                Log.d(LOG_TAG, "Update successful! New version: " + version.version);
                EventBus.getDefault().post(new UpdateSuccessEvent());

            } catch (Exception e) {
                realm.cancelTransaction();
                Log.e(LOG_TAG, "Error saving update to database! Update cancelled!", e);
                Prefs.with(this).save(Preferences.UPDATE_PENDING, true);
                EventBus.getDefault().post(new UpdateFailureEvent());
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error downloading update from server! Update cancelled!", e);
            Prefs.with(this).save(Preferences.UPDATE_PENDING, true);
            EventBus.getDefault().post(new UpdateFailureEvent());
        } finally {
            realm.close();
        }
    }

    public static class UpdateSuccessEvent {}

    public static class UpdateFailureEvent {}
}
