package de.lmu.navigator.app;

import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class DebugApplication extends RoomfinderApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // Crash reporting
        Fabric.with(this, new CrashlyticsCore());
    }
}
