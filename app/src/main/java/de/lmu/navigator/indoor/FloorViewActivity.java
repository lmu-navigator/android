package de.lmu.navigator.indoor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingPartOld;
import de.lmu.navigator.model.RoomOld;
import de.lmu.navigator.search.AbsSearchActivity;

@EActivity(R.layout.activity_floorview)
@OptionsMenu(R.menu.floorview)
public class FloorViewActivity extends ActionBarActivity {
    private static final String LOG_TAG = FloorViewActivity.class
            .getSimpleName();

    public static final int REQUEST_CODE_SEARCH_ROOM = 1;

    @Extra
    BuildingPartOld mBuildingPart;

    @Extra
    RoomOld mRoomForSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void init() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container_tileview, TileViewFragment_.builder()
                        .buildingPart(mBuildingPart).mSelectedRoom(mRoomForSelection).build())
                .commit();

        setTitle(mBuildingPart.getBuilding().getDisplayNameFixed());
    }
    
    public TileViewFragment getTileViewFragment() {
        return (TileViewFragment) getFragmentManager().findFragmentById(R.id.container_tileview);
    }

    @OptionsItem(R.id.search)
    void startSearch() {
        // TODO
//        SearchRoomActivity_.intent(this)
//                .mBuilding(mBuildingPart.getBuilding())
//                .startForResult(REQUEST_CODE_SEARCH_ROOM);
    }

    @OnActivityResult(REQUEST_CODE_SEARCH_ROOM)
    void onSearchResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            RoomOld room = data.getParcelableExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
            getTileViewFragment().onRoomSelected(room);
        } else {
            Log.w(LOG_TAG, "Search returned with unknown result code");
        }
    }

    @Override
    public void onBackPressed() {
        TileViewFragment tileView = getTileViewFragment();
        if (tileView != null && tileView.isAdded()) {
            if (tileView.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @OptionsItem(android.R.id.home)
    void onUpPressed() {
        super.onBackPressed();
    }
}
