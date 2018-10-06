package de.lmu.navigator.app;

import android.app.Application;

import io.realm.Realm;

public class RoomfinderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
