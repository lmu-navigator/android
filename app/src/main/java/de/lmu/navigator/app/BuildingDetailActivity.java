package de.lmu.navigator.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.indoor.FloorViewActivity;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchRoomActivity;

public class BuildingDetailActivity extends BaseActivity {

    private static final String LOG_TAG = BuildingDetailActivity.class.getSimpleName();

    private static final String EXTRA_BUILDING_CODE = "EXTRA_BUILDING_CODE";

    public static final int REQUEST_CODE_SEARCH_ROOM = 1;

    @Bind(R.id.image)
    ImageView mBuildingImage;

    @Bind(R.id.title)
    TextView mBuildingName;

    @Bind(R.id.city)
    TextView mBuildingCity;

    private Building mBuilding;

    public static Intent newIntent(Context context, String buildingCode) {
        return new Intent(context, BuildingDetailActivity.class)
                .putExtra(EXTRA_BUILDING_CODE, buildingCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);
        ButterKnife.bind(this);

        String extraBuildingCode = getIntent().getStringExtra(EXTRA_BUILDING_CODE);
        mBuilding = mDatabaseManager.getBuilding(extraBuildingCode);

        setTitle(null);
        mBuildingName.setText(mBuilding.getDisplayName());
        mBuildingCity.setText(mBuilding.getStreet().getCity().getName());

        final Drawable placeholder = TextDrawable.builder()
                .beginConfig()
                    .textColor(Color.WHITE)
                    .fontSize(getResources()
                            .getDimensionPixelSize(R.dimen.building_placeholder_font_size))
                    .toUpperCase()
                .endConfig()
                .buildRect(mBuilding.getDisplayName().substring(0, 1),
                        ColorGenerator.MATERIAL.getColor(mBuilding));

        Picasso.with(this)
               .load(ModelHelper.getPictureUrl(mBuilding))
               .placeholder(placeholder)
               .into(mBuildingImage);
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
        startActivity(FloorViewActivity.newIntent(this, mBuilding));
    }

    @OnClick(R.id.layout_map)
    void showMap() {
        String url = String.format("http://maps.google.com/maps?geo:%s%s&q=%s",
                mBuilding.getCoordLat(), mBuilding.getCoordLong(), mBuilding.getDisplayName());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @OnClick(R.id.layout_search)
    void startSearch() {
        startActivityForResult(SearchRoomActivity
                .newIntent(this, mBuilding.getCode()), REQUEST_CODE_SEARCH_ROOM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SEARCH_ROOM) {
            if (resultCode == RESULT_OK) {
                String roomCode = data.getStringExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
                startActivity(FloorViewActivity.newIntent(this, mBuilding, roomCode));
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

            //case R.id.search:
            //    startActivityForResult(SearchRoomActivity
            //            .newIntent(this, mBuilding.getCode()), REQUEST_CODE_SEARCH_ROOM);
            //    return true;

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
