package de.lmu.navigator.app;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;

import de.lmu.navigator.database.DatabaseManager;
import de.lmu.navigator.database.RealmDatabaseManager;

public class BaseActivity extends ActionBarActivity {

    protected DatabaseManager mDatabaseManager;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mDatabaseManager = new RealmDatabaseManager(this);
    }

    @Override
    protected void onDestroy() {
        mDatabaseManager.close();
        super.onDestroy();
    }
}
