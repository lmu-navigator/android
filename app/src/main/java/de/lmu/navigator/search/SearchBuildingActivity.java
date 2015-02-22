package de.lmu.navigator.search;

import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.database.DatabaseManager;
import de.lmu.navigator.database.RealmDatabaseManager;
import de.lmu.navigator.database.model.Building;

@EActivity(R.layout.activity_search)
public class SearchBuildingActivity extends AbsSearchActivity {

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
