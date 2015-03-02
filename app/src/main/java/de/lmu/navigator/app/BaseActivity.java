package de.lmu.navigator.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import de.lmu.navigator.database.RealmDatabaseManager;

public class BaseActivity extends ActionBarActivity {

    protected RealmDatabaseManager mDatabaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseManager = new RealmDatabaseManager(this);
    }

    @Override
    protected void onDestroy() {
        mDatabaseManager.close();
        super.onDestroy();
    }
}
