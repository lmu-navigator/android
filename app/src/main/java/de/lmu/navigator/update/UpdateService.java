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
    public void onCreate() {
        super.onCreate();


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getInstance(this);
        RestClient restClient = new RetrofitRestClient();

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
            Log.d(LOG_TAG, "Download cities...");
            List<City> cities = restClient.getCities();
            Log.d(LOG_TAG, "Download streets...");
            List<Street> streets = restClient.getStreets();
            Log.d(LOG_TAG, "Download buildings...");
            List<Building> buildings = restClient.getBuildings();
            Log.d(LOG_TAG, "Download building parts...");
            List<BuildingPart> buildingParts = restClient.getBuildingParts();
            Log.d(LOG_TAG, "Download floors...");
            List<Floor> floors = restClient.getFloors();
            Log.d(LOG_TAG, "Download rooms...");
            List<Room> rooms = restClient.getRooms();
            Version version = restClient.getVersion();

            // Setup relationships
            // TODO: performance implications? (-> sort first?)
            Log.d(LOG_TAG, "Link cities and streets...");
            for (Street s : streets) {
                for (City c : cities) {
                    if (c.getCode().equals(s.getCityCode())) {
                        c.getStreets().add(s);
                        s.setCity(c);
                        break;
                    }
                }
            }
            Log.d(LOG_TAG, "Link streets and buildings...");
            for (Building b : buildings) {
                b.setStarred(mFavorites.contains(b.getCode()));
                for (Street s : streets) {
                    if (s.getCode().equals(b.getStreetCode())) {
                        s.getBuildings().add(b);
                        b.setStreet(s);
                        break;
                    }
                }
            }
            Log.d(LOG_TAG, "Link buildings and building parts...");
            for (BuildingPart p : buildingParts) {
                for (Building b : buildings) {
                    if (b.getCode().equals(p.getBuildingCode())) {
                        b.getBuildingParts().add(p);
                        p.setBuilding(b);
                        break;
                    }
                }
            }
            Log.d(LOG_TAG, "Link building parts and floors...");
            for (Floor f : floors) {
                for (BuildingPart p : buildingParts) {
                    if (p.getCode().equals(f.getBuildingPartCode())) {
                        p.getFloors().add(f);
                        f.setBuildingPart(p);
                        break;
                    }
                }
            }
            Log.d(LOG_TAG, "Link floors and rooms...");
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
                Log.d(LOG_TAG, "Save cities...");
                realm.copyToRealm(cities);
                Log.d(LOG_TAG, "Save streets...");
                realm.copyToRealm(streets);
                Log.d(LOG_TAG, "Save buildings...");
                realm.copyToRealm(buildings);
                Log.d(LOG_TAG, "Save building parts...");
                realm.copyToRealm(buildingParts);
                Log.d(LOG_TAG, "Save floors...");
                realm.copyToRealm(floors);
                Log.d(LOG_TAG, "Save rooms...");
                realm.copyToRealm(rooms);

                realm.commitTransaction();

                Prefs.with(this).save(Preferences.DATA_VERSION, version.version);
                Log.d(LOG_TAG, "Update successful! New version: " + version.version);
                EventBus.getDefault().post(new UpdateSuccessEvent());

            } catch (Exception e) {
                realm.cancelTransaction();
                Log.e(LOG_TAG, "Error saving update to database! Update cancelled!", e);
                EventBus.getDefault().post(new UpdateFailureEvent());
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error downloading update from server! Update cancelled!", e);
            EventBus.getDefault().post(new UpdateFailureEvent());
        } finally {
            realm.close();
        }
    }

    public static class UpdateSuccessEvent {}

    public static class UpdateFailureEvent {}

    public static class UpdateCancelEvent {}

}
