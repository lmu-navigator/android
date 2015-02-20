package de.lmu.navigator.outdoor;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.melnykov.fab.FloatingActionButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import de.lmu.navigator.R;
import de.lmu.navigator.app.MainActivity;
import de.lmu.navigator.indoor.FloorViewActivity_;
import de.lmu.navigator.model.BuildingOld;
import de.lmu.navigator.model.BuildingPartOld;
import de.lmu.navigator.model.RoomOld;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchRoomActivity_;

@EActivity(R.layout.activity_buildingdetail)
@OptionsMenu(R.menu.building)
public class BuildingDetailActivity extends ActionBarActivity implements ObservableScrollViewCallbacks {

    private static final String LOG_TAG = BuildingDetailActivity.class.getSimpleName();

    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    public static final int REQUEST_CODE_SEARCH_ROOM = 1;

    public static final int RIPPLE_DELAY = 750;

    @ViewById(R.id.image)
    ImageView mBuildingImage;

    @ViewById(R.id.scroll)
    ObservableScrollView mScrollView;

    @ViewById(R.id.overlay)
    View mImageOverlay;

    @ViewById(R.id.fab)
    FloatingActionButton mActionButton;

    @ViewById(R.id.title)
    TextView mTitleView;

    @ViewById(R.id.text_address1)
    TextView mAddress1;

    @ViewById(R.id.text_address2)
    TextView mAddress2;

    @Extra
    BuildingOld mBuilding;

    private BuildingPartOld mSelectedBuildingPart;

    private Handler mHandler = new Handler();

    private ColorDrawable mActionBarBackgroundColor;
    private int mActionBarSize;
    private int mStatusBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mActionBarTitlePadding;
    private int mTitlePadding;
    private boolean mFabIsShown = false;

    @AfterViews
    void init() {
        setTitle(null);
        mTitleView.setText(mBuilding.getDisplayNameFixed());

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
        mAddress1.setText(mBuilding.getDisplayNameFixed());
        mAddress2.setText(mBuilding.getCityName());
        mActionButton.setImageResource(mBuilding.isStar()
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
            setTitle(mBuilding.getDisplayNameFixed());
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

    @Click(R.id.image_directions)
    void startDirections() {
        String url = String.format("http://maps.google.com/maps?daddr=%s,%s",
                mBuilding.getCoordLat(), mBuilding.getCoordLong());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Click(R.id.layout_floorview)
    void showFloorView() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FloorViewActivity_.intent(BuildingDetailActivity.this)
                        .mBuildingPart(mSelectedBuildingPart)
                        .start();
            }
        }, RIPPLE_DELAY);
    }

    @Click(R.id.layout_map)
    void showMap() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(MainActivity.newIntent(BuildingDetailActivity.this, mBuilding));
            }
        }, RIPPLE_DELAY);

    }

    @Click(R.id.fab)
    void toggleStar() {
        boolean newState = !mBuilding.isStar();
        mBuilding.setFavorite(newState);
        mActionButton.setImageResource(newState ? R.drawable.ic_star_checked : R.drawable.ic_star);
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
