package de.lmu.navigator.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.indoor.FloorViewActivity;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchRoomActivity;

public class BuildingDetailActivity extends BaseActivity {

    private static final String LOG_TAG = BuildingDetailActivity.class.getSimpleName();

    private static final String EXTRA_BUILDING_CODE = "EXTRA_BUILDING_CODE";

    public static final int REQUEST_CODE_SEARCH_ROOM = 1;

    @InjectView(R.id.image)
    ImageView mBuildingImage;

    @InjectView(R.id.title)
    TextView mBuildingName;

    @InjectView(R.id.city)
    TextView mBuildingCity;

    @InjectView(R.id.layout_buildingparts)
    View mBuildingPartsLayout;

    @InjectView(R.id.recycler)
    RecyclerView mBuildingPartRecycler;

    private Building mBuilding;
    private BuildingPartAdapter mAdapter;

    public static Intent newIntent(Context context, String buildingCode) {
        return new Intent(context, BuildingDetailActivity.class)
                .putExtra(EXTRA_BUILDING_CODE, buildingCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);
        ButterKnife.inject(this);

        String extraBuildingCode = getIntent().getStringExtra(EXTRA_BUILDING_CODE);
        mBuilding = mDatabaseManager.getBuilding(extraBuildingCode);

        setTitle(null);
        mBuildingName.setText(mBuilding.getDisplayName());
        mBuildingCity.setText(mBuilding.getStreet().getCity().getName());

        // TODO: only show building parts if maps are different!
        mBuildingPartRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        if (mBuilding.getBuildingParts().size() > 1) {
            mAdapter = new BuildingPartAdapter(this, mBuilding.getBuildingParts());
            mBuildingPartRecycler.setAdapter(mAdapter);
        } else {
            mBuildingPartsLayout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.layout_directions)
    void startDirections() {
        String url = String.format("http://maps.google.com/maps?daddr=%s,%s",
                mBuilding.getCoordLat(), mBuilding.getCoordLong());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @OnClick(R.id.layout_floorview)
    void showFloorView() {
        BuildingPart p;
        if (mAdapter != null) {
            p = mAdapter.getSelectedBuildingPart();
        } else {
            p = mBuilding.getBuildingParts().get(0);
        }
        startActivity(FloorViewActivity.newIntent(this, p));
    }

    @OnClick(R.id.layout_map)
    void showMap() {
        String url = String.format("http://maps.google.com/maps?geo:%s%s&q=%s",
                mBuilding.getCoordLat(), mBuilding.getCoordLong(), mBuilding.getDisplayName());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SEARCH_ROOM) {
            if (resultCode == RESULT_OK) {
                String roomCode = data.getStringExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
                startActivity(FloorViewActivity.newIntent(this, roomCode));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.building, menu);
        MenuItem starItem = menu.findItem(R.id.star);
        starItem.setIcon(mBuilding.isStarred() ? R.drawable.ic_action_star
                : R.drawable.ic_action_star_outline);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.search:
                startActivityForResult(SearchRoomActivity
                        .newIntent(this, mBuilding.getCode()), REQUEST_CODE_SEARCH_ROOM);
                return true;

            case R.id.star:
                boolean newState = !mBuilding.isStarred();
                mDatabaseManager.setBuildingStarred(mBuilding, newState);
                item.setIcon(newState ? R.drawable.ic_action_star
                        : R.drawable.ic_action_star_outline);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
