package de.lmu.navigator.app;

import android.app.Application;

import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class DebugApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Crash reporting
        Fabric.with(this, new CrashlyticsCore());
    }
}
