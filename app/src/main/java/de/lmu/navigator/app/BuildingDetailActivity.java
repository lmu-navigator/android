package de.lmu.navigator.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.melnykov.fab.FloatingActionButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.indoor.FloorViewActivity;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchRoomActivity;

public class BuildingDetailActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private static final String LOG_TAG = BuildingDetailActivity.class.getSimpleName();

    private static final String EXTRA_BUILDING_CODE = "EXTRA_BUILDING_CODE";

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    public static final int REQUEST_CODE_SEARCH_ROOM = 1;

    @InjectView(R.id.image)
    ImageView mBuildingImage;

    @InjectView(R.id.scroll)
    ObservableScrollView mScrollView;

    @InjectView(R.id.overlay)
    View mImageOverlay;

    @InjectView(R.id.fab)
    FloatingActionButton mActionButton;

    @InjectView(R.id.title)
    TextView mTitleView;

    @InjectView(R.id.text_address1)
    TextView mAddress1;

    @InjectView(R.id.text_address2)
    TextView mAddress2;

    private Building mBuilding;

    private BuildingPart mSelectedBuildingPart;

    private ColorDrawable mActionBarBackgroundColor;
    private int mActionBarSize;
    private int mStatusBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mActionBarTitlePadding;
    private int mTitlePadding;
    private boolean mFabIsShown = false;

    public static Intent newIntent(Context context, String buildingCode) {
        return new Intent(context, BuildingDetailActivity.class)
                .putExtra(EXTRA_BUILDING_CODE, buildingCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildingdetail);
        ButterKnife.inject(this);

        String extraBuildingCode = getIntent().getStringExtra(EXTRA_BUILDING_CODE);
        mBuilding = mDatabaseManager.getBuilding(extraBuildingCode);

        init();
    }

    private void init() {
        setTitle(null);
        mTitleView.setText(mBuilding.getDisplayName());

        mActionBarBackgroundColor = new ColorDrawable(getResources().getColor(R.color.green_primary));
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);
        mActionBarTitlePadding = getResources().getDimensionPixelSize(R.dimen.action_bar_title_margin);
        mTitlePadding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        mActionBarSize = getActionBarSize();
        mStatusBarSize = getResources().getDimensionPixelSize(R.dimen.status_bar_height);

        setActionBarBackgroundAlpha(0);
        mScrollView.setScrollViewCallbacks(this);
        mActionButton.setScaleX(0);
        mActionButton.setScaleY(0);

        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0, false, false);
            }
        });

        setUpBuildingInfo();
        setSelectedBuildingPart(0);
    }

    private void setUpBuildingInfo() {
        mAddress1.setText(mBuilding.getDisplayName());
        mAddress2.setText(mBuilding.getStreet().getCity().getName());
        mActionButton.setImageResource(mBuilding.isStarred()
                ? R.drawable.ic_star_checked : R.drawable.ic_star);
        //mImageStar.setSelected(mBuilding.isStar());
    }

    private void setSelectedBuildingPart(int position) {
        mSelectedBuildingPart = mBuilding.getBuildingParts().get(position);
//        mSelectedBuildingPart = (BuildingPart) mListBuildingParts.getItemAtPosition(position);
//        Picasso.with(this)
//                .load("file:///android_asset/" + mSelectedBuildingPart.getStartFloor().getSamplePath())
//                .into(mImageFloorView);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize - mStatusBarSize;
        int minOverlayTransitionY = mActionBarSize + mStatusBarSize - mImageOverlay.getHeight();
        mImageOverlay.setTranslationY(Math.max(minOverlayTransitionY, Math.min(0, -scrollY)));
        mBuildingImage.setTranslationY(Math.max(minOverlayTransitionY, Math.min(0, -scrollY / 2)));

        // Change alpha of overlay
        mImageOverlay.setAlpha(Math.max(0, Math.min(1, (float) scrollY / flexibleRange)));

        // Scale title text
        float scale = 1 + Math.max(0, Math.min(MAX_TEXT_SCALE_DELTA, (flexibleRange - scrollY) / flexibleRange));
        mTitleView.setPivotX(0);
        mTitleView.setPivotY(0);
        mTitleView.setScaleX(scale);
        mTitleView.setScaleY(scale);

        // Change alpha of toolbar background
        if (-scrollY + mFlexibleSpaceImageHeight <= mActionBarSize + mStatusBarSize) {
            setTitle(mBuilding.getDisplayName());
            setActionBarBackgroundAlpha(1);
        } else {
            setTitle(null);
            setActionBarBackgroundAlpha(0);
        }

        // Translate title text
        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale) - mTitlePadding;
        int titleTranslationY = maxTitleTranslationY - scrollY;
        int maxTitleTranslationX = mActionBarTitlePadding - mTitlePadding;
        int titleTranslationX = (int) (maxTitleTranslationX * (1 - ((scale - 1) / MAX_TEXT_SCALE_DELTA)));
        mTitleView.setTranslationX(titleTranslationX);
        mTitleView.setTranslationY(titleTranslationY);
        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceImageHeight - mActionButton.getHeight() / 2;
        int fabTranslationY = Math.max(mActionBarSize - mActionButton.getHeight() / 2,
                Math.min(maxFabTranslationY, -scrollY + mFlexibleSpaceImageHeight - mActionButton.getHeight() / 2));
        mActionButton.setTranslationY(fabTranslationY);

        // Show/hide FAB
        if (mActionButton.getTranslationY() < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private void showFab() {
        if (!mFabIsShown) {
            mActionButton.animate().cancel();
            mActionButton.animate().scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            mActionButton.animate().cancel();
            mActionButton.animate().scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShown = false;
        }
    }

    private void setActionBarBackgroundAlpha(float alpha) {
        mActionBarBackgroundColor.setAlpha((int) alpha * 255);
        getSupportActionBar().setBackgroundDrawable(mActionBarBackgroundColor);
    }

    private int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    @OnClick(R.id.image_directions)
    void startDirections() {
        String url = String.format("http://maps.google.com/maps?daddr=%s,%s",
                mBuilding.getCoordLat(), mBuilding.getCoordLong());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @OnClick(R.id.layout_floorview)
    void showFloorView() {
        startActivity(FloorViewActivity.newIntent(this, mSelectedBuildingPart));
    }

    @OnClick(R.id.layout_map)
    void showMap() {
        startActivity(MainActivity.newIntent(BuildingDetailActivity.this, mBuilding));
    }

    @OnClick(R.id.fab)
    void toggleStar() {
        boolean newState = !mBuilding.isStarred();
        mDatabaseManager.setBuildingStarred(mBuilding, newState);
        mActionButton.setImageResource(newState ? R.drawable.ic_star_checked : R.drawable.ic_star);
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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
