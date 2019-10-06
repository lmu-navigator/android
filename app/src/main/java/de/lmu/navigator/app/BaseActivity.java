package de.lmu.navigator.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import de.lmu.navigator.database.RealmDatabaseManager;

public class BaseActivity extends AppCompatActivity {

    protected RealmDatabaseManager mDatabaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseManager = new RealmDatabaseManager();
    }

    @Override
    protected void onDestroy() {
        mDatabaseManager.close();
        super.onDestroy();
    }
}
