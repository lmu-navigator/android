package de.lmu.navigator.app;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface PreferencesHelper {

    @DefaultInt(Constants.SHIPPED_DATA_VERSION)
    int dataVersion();

    @DefaultBoolean(false)
    boolean updatePending();
}
