package de.lmu.navigator.search;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingOld;
import de.lmu.navigator.model.RoomOld;

@EActivity(R.layout.activity_search)
public class SearchRoomActivity extends AbsSearchActivity {

    @Extra
    BuildingOld mBuilding;

    @Override
    public List<RoomOld> getItems() {
        return mBuilding.getRoomsIncludeAdjacent();
    }

    @Override
    public int getSearchHintResId() {
        return R.string.search_hint_rooms;
    }
}
