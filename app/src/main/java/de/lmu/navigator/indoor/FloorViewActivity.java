package de.lmu.navigator.indoor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import de.lmu.navigator.R;
import de.lmu.navigator.app.BaseActivity;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.model.RoomOld;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchRoomActivity;

public class FloorViewActivity extends BaseActivity {
    private static final String LOG_TAG = FloorViewActivity.class.getSimpleName();

    public static final int REQUEST_CODE_SEARCH_ROOM = 1;

    private static final String EXTRA_BUILDING_PART_CODE = "EXTRA_BUILDING_PART_CODE";
    private static final String EXTRA_ROOM_CODE = "EXTRA_ROOM_CODE";

    private BuildingPart mBuildingPart;
    private Room mRoomForSelection;

    public static Intent newIntent(Context context, BuildingPart buildingPart) {
        return new Intent(context, FloorViewActivity.class)
                .putExtra(EXTRA_BUILDING_PART_CODE, buildingPart.getCode());
    }

    public static Intent newIntent(Context context, String roomCode) {
        return new Intent(context, FloorViewActivity.class)
                .putExtra(EXTRA_ROOM_CODE, roomCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floorview);
        ButterKnife.inject(this);

        String roomCode = getIntent().getStringExtra(EXTRA_ROOM_CODE);
        if (roomCode != null) {
            mRoomForSelection = mDatabaseManager.getRoom(roomCode);
            mBuildingPart = mRoomForSelection.getFloor().getBuildingPart();
        } else {
            String buildingPartCode = getIntent().getStringExtra(EXTRA_BUILDING_PART_CODE);
            mBuildingPart = mDatabaseManager.getBuildingPart(buildingPartCode);
        }

//        getFragmentManager().beginTransaction()
//                .replace(R.id.container_tileview, TileViewFragment_.builder()
//                        .buildingPart(mBuildingPart).mSelectedRoom(mRoomForSelection).build())
//                .commit();

        setTitle(mBuildingPart.getBuilding().getDisplayName());
    }

    public TileViewFragment getTileViewFragment() {
        return (TileViewFragment) getFragmentManager().findFragmentById(R.id.container_tileview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.floorview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;

            case R.id.search:
                startActivityForResult(SearchRoomActivity
                                .newIntent(this, mBuildingPart.getBuilding().getCode()),
                        REQUEST_CODE_SEARCH_ROOM);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SEARCH_ROOM) {
            if (resultCode == RESULT_OK) {
                RoomOld room = data.getParcelableExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
                getTileViewFragment().onRoomSelected(room);
            } else {
                Log.w(LOG_TAG, "Search returned with unknown result code");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
}
