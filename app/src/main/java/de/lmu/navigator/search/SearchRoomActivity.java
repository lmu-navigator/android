package de.lmu.navigator.search;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.Building;
import de.lmu.navigator.model.Room;

@EActivity(R.layout.activity_search)
public class SearchRoomActivity extends AbsSearchActivity {

    @Extra
    Building mBuilding;

    @Override
    public List<Room> getItems() {
        return mBuilding.getRoomsIncludeAdjacent();
    }

    @Override
    public int getSearchHintResId() {
        return R.string.search_hint_rooms;
    }
}
