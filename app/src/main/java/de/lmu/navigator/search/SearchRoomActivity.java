package de.lmu.navigator.search;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.database.RealmDatabaseManager;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.Room;

public class SearchRoomActivity extends AbsSearchActivity {

    private static final String EXTRA_BUILDING_CODE = "EXTRA_BUILDING_CODE";

    public static Intent newIntent(Context context, String buildingCode) {
        return new Intent(context, SearchRoomActivity.class)
                .putExtra(EXTRA_BUILDING_CODE, buildingCode);
    }

    @Override
    public List<Searchable> getItems() {
        RealmDatabaseManager databaseManager = new RealmDatabaseManager(this);

        String buildingCode = getIntent().getStringExtra(EXTRA_BUILDING_CODE);
        Building building = databaseManager.getBuilding(buildingCode);

        List<Searchable> items = new ArrayList<>();
        for (Room r : databaseManager.getRoomsForBuilding(building, true, true)) {
            items.add(SearchableWrapper.wrap(r));
        }
        databaseManager.close();
        return items;
    }

    @Override
    public int getSearchHintResId() {
        return R.string.search_hint_rooms;
    }
}
