package de.lmu.navigator.outdoor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.manuelpeinado.fadingactionbar.extras.actionbarcompat.FadingActionBarHelper;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import de.lmu.navigator.R;
import de.lmu.navigator.app.MainActivity_;
import de.lmu.navigator.indoor.FloorViewActivity_;
import de.lmu.navigator.model.BuildingOld;
import de.lmu.navigator.model.BuildingPartOld;
import de.lmu.navigator.model.RoomOld;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchRoomActivity_;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;

@EActivity
@OptionsMenu(R.menu.building)
public class BuildingDetailActivityOld extends ActionBarActivity {

    private static final String LOG_TAG = BuildingDetailActivityOld.class.getSimpleName();

    private final static String STATIC_MAPS_API_KEY = "AIzaSyDec1V3Khk496ISjifxOtFtvUGMMWJpPpE";

    public static final int REQUEST_CODE_SEARCH_ROOM = 1;

    TextView mAddress1;

    TextView mAddress2;

    ImageView mImageFloorView;

    HListView mListBuildingParts;

    View mLayoutBuildingParts;

    ImageView mImageMap;

    ImageView mImageStar;

    @Extra
    BuildingOld mBuilding;

    private BuildingPartOld mSelectedBuildingPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FadingActionBarHelper helper = new FadingActionBarHelper()
                .actionBarBackground(R.color.green)
                .headerLayout(R.layout.activity_buildingdetail_header)
                .contentLayout(R.layout.activity_buildingdetail_content);
        setContentView(helper.createView(this));
        helper.initActionBar(this);
    }

    @AfterViews
    void init() {
        setTitle(mBuilding.getDisplayNameFixed());
        setUpBuildingInfo();
        setUpFloorViews();
        setUpMapViews();
    }

    private void setUpBuildingInfo() {
        mAddress1.setText(mBuilding.getDisplayNameFixed());
        mAddress2.setText(mBuilding.getCityName());
        mImageStar.setSelected(mBuilding.isStar());
    }

    private void setUpFloorViews() {
        final BuildingPartAdapter adapter = new BuildingPartAdapter(this, mBuilding.getBuildingParts());
        mListBuildingParts.setAdapter(adapter);

        if (mBuilding.getBuildingParts().size() < 2) {
            mLayoutBuildingParts.setVisibility(View.GONE);
        }

        setSelectedBuildingPart(0);

        mListBuildingParts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                adapter.setCurrentSelection(position);
                setSelectedBuildingPart(position);
            }
        });
    }

    private void setSelectedBuildingPart(int position) {
        mSelectedBuildingPart = (BuildingPartOld) mListBuildingParts.getItemAtPosition(position);
        Picasso.with(this)
                .load("file:///android_asset/" + mSelectedBuildingPart.getStartFloor().getSamplePath())
                .into(mImageFloorView);
    }

    private void setUpMapViews() {
        Picasso.with(this)
                .load(getStaticMapUrl(mBuilding))
                .into(mImageMap);
    }

    private String getStaticMapUrl(BuildingOld building) {
        // TODO: custom marker url
        return "http://maps.googleapis.com/maps/api/staticmap?"
                + "center=" + building.getCoordLat() + "," + building.getCoordLong()
                + "&zoom=16"
                + "&size=640x640"
                + "&scale=2"
                + "&markers=scale:2|" + building.getCoordLat() + "," + building.getCoordLong()
                + "&sensor=false"
                + "&key=" + STATIC_MAPS_API_KEY;
    }

    void showFloorView() {
        FloorViewActivity_.intent(BuildingDetailActivityOld.this)
                .mBuildingPart(mSelectedBuildingPart)
                .start();
    }

    void showMap() {
        MainActivity_.intent(this).mBuildingForMap(mBuilding).start();
    }

    void toggleStar() {
        boolean newState = !mBuilding.isStar();
        mBuilding.setFavorite(newState);
        mImageStar.setSelected(newState);
    }

    void startDirections() {
        String url = String.format("http://maps.google.com/maps?daddr=%s,%s",
                mBuilding.getCoordLat(), mBuilding.getCoordLong());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @OptionsItem(R.id.search)
    void startSearch() {
        SearchRoomActivity_.intent(this)
                .mBuilding(mBuilding)
                .startForResult(REQUEST_CODE_SEARCH_ROOM);
    }

    @OnActivityResult(REQUEST_CODE_SEARCH_ROOM)
    void onSearchResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            RoomOld room = data.getParcelableExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
            FloorViewActivity_.intent(this)
                    .mBuildingPart(room.getFloor().getBuildingPart())
                    .mRoomForSelection(room)
                    .start();
        } else {
            Log.w(LOG_TAG, "Search returned with unknown result code");
        }
    }

    @OptionsItem(android.R.id.home)
    void onUpPressed() {
        onBackPressed();
    }
}
