package de.lmu.navigator.search;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.RoomOld;

public class SearchRoomActivity extends AbsSearchActivity {

    private static final String EXTRA_BUILDING_CODE = "EXTRA_BUILDING_CODE";

    public static Intent newIntent(Context context, String buildingCode) {
        return new Intent(context, SearchRoomActivity.class)
                .putExtra(EXTRA_BUILDING_CODE, buildingCode);
    }

    @Override
    public List<RoomOld> getItems() {
        String buildingCode = getIntent().getStringExtra(EXTRA_BUILDING_CODE);
        return null;
        //return mBuilding.getRoomsIncludeAdjacent();
    }

    @Override
    public int getSearchHintResId() {
        return R.string.search_hint_rooms;
    }
}
