package de.lmu.navigator.search;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.database.DatabaseManager;
import de.lmu.navigator.database.RealmDatabaseManager;
import de.lmu.navigator.database.model.Building;

public class SearchBuildingActivity extends AbsSearchActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchBuildingActivity.class);
    }

    @Override
    public List<Searchable> getItems() {
        List<Searchable> items = new ArrayList<>();
        DatabaseManager databaseManager = new RealmDatabaseManager(this);
        for (Building b : databaseManager.getAllBuildings(true)) {
            items.add(SearchableWrapper.wrap(b));
        }
        databaseManager.close();
        return items;
    }

    @Override
    public int getSearchHintResId() {
        return R.string.search_hint_buildings;
    }
}
