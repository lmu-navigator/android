package de.lmu.navigator.database;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.lmu.navigator.app.Preferences;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.City;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.database.model.Street;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import me.alexrs.prefs.lib.Prefs;

public class UpdateService extends IntentService {

    private static final String LOG_TAG = UpdateService.class.getSimpleName();

    public static final String ACTION_SUCCESS = "SUCCESS";
    public static final String ACTION_ERROR = "ERROR";
    public static final String EXTRA_THROWABLE = "THROWABLE";

    private LocalBroadcastManager mBroadcastManager;
    private List<String> mFavorites = new ArrayList<>();
    private boolean mRunning = false;

    public UpdateService() {
        super(UpdateService.class.getName());
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mRunning) {
            return;
        }
        mRunning = true;

        final Realm realm = Realm.getInstance(this);
        final AssetManager assetManager = getAssets();
        final Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        // try to remember favorites and restore them later
        RealmResults<Building> favBuildings = realm.where(Building.class)
                .equalTo(ModelHelper.BUILDING_STARRED, true)
                .findAll();
        for (Building b : favBuildings) {
            mFavorites.add(b.getCode());
        }

        Log.d(LOG_TAG, "Starting database update. Old version: " + Prefs.with(this)
                .getInt(Preferences.DATA_VERSION, -1));

        try {
            // Load data
            Log.d(LOG_TAG, "Load data from asset files...");
            InputStream in;
            InputStreamReader reader;

            in = assetManager.open("data/1_city.json");
            reader = new InputStreamReader(in);
            List<City> cities = gson.fromJson(reader, new TypeToken<List<City>>() {}.getType());
            reader.close();

            in = assetManager.open("data/2_street.json");
            reader = new InputStreamReader(in);
            List<Street> streets = gson.fromJson(reader, new TypeToken<List<Street>>() {}.getType());
            reader.close();

            in = assetManager.open("data/3_building.json");
            reader = new InputStreamReader(in);
            List<Building> buildings = gson.fromJson(reader, new TypeToken<List<Building>>() {}.getType());
            reader.close();

            in = assetManager.open("data/4_building_part.json");
            reader = new InputStreamReader(in);
            List<BuildingPart> buildingParts = gson.fromJson(reader, new TypeToken<List<BuildingPart>>() {}.getType());
            reader.close();

            in = assetManager.open("data/5_floor.json");
            reader = new InputStreamReader(in);
            List<Floor> floors = gson.fromJson(reader, new TypeToken<List<Floor>>() {}.getType());
            reader.close();

            in = assetManager.open("data/6_room.json");
            reader = new InputStreamReader(in);
            List<Room> rooms = gson.fromJson(reader, new TypeToken<List<Room>>() {}.getType());
            reader.close();

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
                fixBuilding(b);
                for (Street s : streets) {
                    if (s.getCode().equals(b.getStreetCode())) {
                        s.getBuildings().add(b);
                        b.setStreet(s);
                        break;
                    }
                }
            }
            for (BuildingPart p : buildingParts) {
                fixBuildingPart(p);
                for (Building b : buildings) {
                    if (b.getCode().equals(p.getBuildingCode())) {
                        b.getBuildingParts().add(p);
                        p.setBuilding(b);
                        break;
                    }
                }
            }
            for (Floor f : floors) {
                fixFloor(f);
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

                Prefs.with(this).save(Preferences.DATA_VERSION, Preferences.SHIPPED_DATA_VERSION);
                Log.d(LOG_TAG, "Update successful! New version: " + Preferences.SHIPPED_DATA_VERSION);
                broadcastSuccess();
            } catch (Exception e) {
                realm.cancelTransaction();
                Log.e(LOG_TAG, "Error saving update to database! Update cancelled!", e);
                broadcastError(e);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error loading data from assets! Update cancelled!", e);
            broadcastError(e);
        } finally {
            realm.close();
        }
    }

    private void broadcastSuccess() {
        mBroadcastManager.sendBroadcast(new Intent(ACTION_SUCCESS));
    }

    private void broadcastError(Throwable e) {
        mBroadcastManager.sendBroadcast(new Intent(ACTION_ERROR).putExtra(EXTRA_THROWABLE, e));
    }

    private void fixBuilding(Building b) {
        // restore favorites
        b.setStarred(mFavorites.contains(b.getCode()));
        // if user has no favorites yet, add Geschwister-Scholl-Platz
        if (mFavorites.isEmpty() && b.getCode().equals("bw0000")) {
            b.setStarred(true);
        }
        b.setDisplayName(ModelHelper.getBuildingNameFixed(b.getDisplayName()));
    }

    private void fixBuildingPart(BuildingPart p) {
        p.setName(ModelHelper.getBuildingPartNameFixed(p.getName()));
    }

    private void fixFloor(Floor f) {
        f.setName(ModelHelper.getFloorNameFixed(f.getName()));
        f.setLevel(ModelHelper.getFloorLevelFixed(f.getLevel(), f.getName()));
    }
}
