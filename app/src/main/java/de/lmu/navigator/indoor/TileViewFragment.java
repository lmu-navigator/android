package de.lmu.navigator.indoor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.qozix.tileview.TileView;
import com.qozix.tileview.graphics.BitmapProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.lmu.navigator.BuildConfig;
import de.lmu.navigator.R;
import de.lmu.navigator.app.BaseFragment;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.RealmDatabaseManager;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.indoor.view.ListenableTileView;
import de.lmu.navigator.indoor.view.TileViewEventListener;

public class TileViewFragment extends BaseFragment implements TileViewEventListener {
    // TODO: refactor! e.g. replace all loops with bparts and floors by a map?

    private static final String LOG_TAG = TileViewFragment.class.getSimpleName();

    private static final String ARGS_BUILDING_CODE = "ARGS_BUILDING_CODE";
    private static final String ARGS_ROOM_CODE = "ARGS_ROOM_CODE";

    private final static int FLOOR_BUTTONS_AUTOCOLLAPSE_DELAY = 5000;
    private final static float TILEVIEW_MAX_SCALE = 4.0f;
    private final static float TILEVIEW_MIN_SCALE = 0.125f;
    private final static int FLOOR_CHANGE_CROSSFADE_DURATION = 250;

    @BindView(R.id.tileview_container)
    FrameLayout mTileViewContainer;

    @BindView(R.id.tileview_floor_button_layout)
    LinearLayout mFloorButtonLayout;

    @BindView(R.id.tileview_button_floor_up)
    ImageButton mButtonFloorUp;

    @BindView(R.id.tileview_button_floor_down)
    ImageButton mButtonFloorDown;

    @BindView(R.id.tileview_button_zoom_in)
    ImageButton mButtonZoomIn;

    @BindView(R.id.tileview_button_zoom_out)
    ImageButton mButtonZoomOut;

    @BindView(R.id.tileview_room_details)
    View mRoomDetailView;

    @BindView(R.id.room_detail_name)
    TextView mRoomDetailName;

    @BindView(R.id.room_detail_floor)
    TextView mRoomDetailFloor;

    @BindView(R.id.tileview_buildingpart_button_layout)
    LinearLayout mBuildingPartsButtonLayout;

    @BindView(R.id.tileview_zoom_layout)
    LinearLayout mZoomButtonLayout;

    @BindView(R.id.tileview_root)
    View mRootView;

    private Building mBuilding;
    private BuildingPart mCurrentBuildingPart;
    private Room mSelectedRoom;
    private Room mStartRoom;
    private List<BuildingPart> mBuildingParts;

    private RealmDatabaseManager mDatabaseManager;

    private ListenableTileView mTileView;
    private List<Floor> mFloorList;
    private List<FloorButton> mFloorButtons;
    private Floor mCurrentFloor;
    private ImageView mSelectedMarker;
    private List<BuildingPartButton> mBuildingPartButtons;

    private boolean mHasBuildingPartsWithDifferentMaps;

    private boolean mHideZoomButtonsWhenExpanded = false;
    private int mZoomButtonTranslation;

    private Handler mHandler = new Handler();
    private Runnable mAutoHideFloorButtonsRunnable = new Runnable() {
        @Override
        public void run() {
            collapseFloorButtons();
        }
    };

    private BitmapProvider mBitmapProvider = new PicassoBitmapProvider();

    private List<OnFloorChangedListener> mOnFloorChangedListeners = new ArrayList<>();

    // Only for debug
    private RoomOverlay roomOverlay;
    private boolean overlayShown = false;

    public static TileViewFragment newInstance(Building building) {
        TileViewFragment fragment = new TileViewFragment();
        Bundle args = new Bundle(1);
        args.putString(ARGS_BUILDING_CODE, building.getCode());
        fragment.setArguments(args);
        return fragment;
    }

    public static TileViewFragment newInstance(Building building, Room room) {
        TileViewFragment fragment = new TileViewFragment();
        Bundle args = new Bundle(1);
        args.putString(ARGS_BUILDING_CODE, building.getCode());
        args.putString(ARGS_ROOM_CODE, room.getCode());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseManager = new RealmDatabaseManager();

        String buildingCode = getArguments().getString(ARGS_BUILDING_CODE);
        mBuilding = mDatabaseManager.getBuilding(buildingCode);

        prepareBuildingParts();

        String roomCode = getArguments().getString(ARGS_ROOM_CODE);
        if (roomCode != null && savedInstanceState == null) {
            mStartRoom = mDatabaseManager.getRoom(roomCode);
            mCurrentBuildingPart = getBuildingPartForRoom(mStartRoom);
        } else {
            mCurrentBuildingPart = mBuildingParts.get(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tileview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mHasBuildingPartsWithDifferentMaps) {
            addBuildingPartButtons();
        }

        mFloorList = getFloorList();
        addFloorButtons();
        setFloor(getStartFloor());

        // For testing
        if (BuildConfig.DEBUG) {
            roomOverlay = new RoomOverlay(getActivity(), mDatabaseManager);
            toggleRoomOverlay();
        }

        mSelectedMarker = new ImageView(getActivity());
        mSelectedMarker.setImageResource(R.drawable.marker_lmu);

        if (mStartRoom != null) {
            onRoomSelected(mStartRoom);
            mStartRoom = null;
        }
    }

    private void prepareBuildingParts() {
        mBuildingParts = new ArrayList<>(Collections2.filter(mBuilding.getBuildingParts(),
                new Predicate<BuildingPart>() {
                    @Override
                    public boolean apply(BuildingPart input) {
                        return input.getFloors() != null && input.getFloors().size() > 0;
                    }
                }));

        Collections.sort(mBuildingParts, ModelHelper.buildingPartComparator);

        mHasBuildingPartsWithDifferentMaps = false;
        if (mBuildingParts.size() <= 1) {
            return;
        }

        for (int i = 0; i < mBuildingParts.size() - 1; i++) {
            for (int k = i + 1; k < mBuildingParts.size(); k++) {
                if (!checkBuildingPartsHaveSameMaps(mBuildingParts.get(i), mBuildingParts.get(k))) {
                    mHasBuildingPartsWithDifferentMaps = true;
                    break;
                }
            }

            if (mHasBuildingPartsWithDifferentMaps) {
                break;
            }
        }
    }

    private boolean checkBuildingPartsHaveSameMaps(BuildingPart p1, BuildingPart p2) {
        for (Floor f1 : p1.getFloors()) {
            for (Floor f2 : p2.getFloors()) {
                if (f1.getLevel().equals(f2.getLevel())) {
                    if (f1.getMapUri().equals(f2.getMapUri())) {
                        break;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private BuildingPart getBuildingPartForRoom(Room room) {
        BuildingPart bp = room.getFloor().getBuildingPart();
        if (mBuildingParts.contains(bp)) {
            return bp;
        }

        for (BuildingPart p : mBuildingParts) {
            for (Floor f : p.getFloors()) {
                if (room.getFloor().getMapUri().equals(f.getMapUri())) {
                    return p;
                }
            }
        }

        return mBuildingParts.get(0);
    }


    private void addBuildingPartButtons() {
        mBuildingPartButtons = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        final Resources res = getResources();
        int buttonWidth = res.getDimensionPixelSize(R.dimen.tileview_button_size);
        int buttonMargin = res.getDimensionPixelSize(R.dimen.tileview_button_margin);
        int screenWidth = res.getDisplayMetrics().widthPixels;

        int availableSpace = screenWidth - 2 * buttonWidth - 4 * buttonMargin;
        int maxWidth = availableSpace / mBuildingParts.size();

        for (final BuildingPart part : mBuildingParts) {
            BuildingPartButton pb = new BuildingPartButton();
            pb.button = (Button) inflater.inflate(R.layout.tileview_buildingpart_button,
                    mBuildingPartsButtonLayout, false);
            if (part.getName().isEmpty()) {
                pb.button.setText("?");
            } else {
                pb.button.setText(part.getName());
            }
            pb.button.setEnabled(!part.equals(mCurrentBuildingPart));
            pb.part = part;

            pb.button.getLayoutParams().width = Math.min(maxWidth, buttonWidth);

            mBuildingPartsButtonLayout.addView(pb.button);
            mBuildingPartButtons.add(pb);

            pb.button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setBuildingPart(part);
                    setFloor(getStartFloor(), false);
                }
            });
        }

        mBuildingPartsButtonLayout.setVisibility(View.VISIBLE);
    }

    private void updateBuildingPartButtonSelection() {
        for (BuildingPartButton pb : mBuildingPartButtons) {
            pb.button.setEnabled(!pb.part.equals(mCurrentBuildingPart));
        }
    }

    private List<Floor> getFloorList() {
        List<Floor> floors = new ArrayList<>();

        if (mBuildingParts.size() <= 1 || mHasBuildingPartsWithDifferentMaps) {
            floors.addAll(mCurrentBuildingPart.getFloors());
        } else {
            for (BuildingPart p : mBuildingParts) {
                for (Floor f1 : p.getFloors()) {
                    boolean mustAdd = true;
                    for (Floor f2 : floors) {
                        if (f1.getLevel().equals(f2.getLevel())) {
                            mustAdd = false;
                            break;
                        }
                    }
                    if (mustAdd) {
                        floors.add(f1);
                    }
                }
            }
        }

        Collections.sort(floors, ModelHelper.floorComparator);
        return floors;
    }

    private Floor getStartFloor() {
        Floor start = mFloorList.get(0);
        for (Floor f : mFloorList) {
            if (f.getName().equals("Erdgeschoss"))
                return f;
            if (!f.getName().endsWith("Untergeschoss")
                    && ModelHelper.floorComparator.compare(f, start) < 0)
                start = f;
        }

        return start;
    }

    public void onRoomSelected(final Room room) {
        clearSelection();
        setBuildingPart(getBuildingPartForRoom(room));
        setFloorForRoom(room);

        mTileView.post(new Runnable() {
            @Override
            public void run() {
                selectRoom(room);
            }
        });
    }

    private void selectRoom(Room room) {
        mTileView.addMarker(mSelectedMarker, room.getPosX(), room.getPosY(), -0.5f, -1f);
        mTileView.setScale(1); // TODO: define better scale
        mTileView.scrollToAndCenter(room.getPosX(), room.getPosY());

        mRoomDetailView.setVisibility(View.VISIBLE);
        mRoomDetailName.setText(getString(R.string.floorview_selected_room, room.getName()));
        mRoomDetailFloor.setText(room.getFloor().getName());

        mSelectedRoom = room;
    }

    private void setFloorForRoom(Room room) {
        for (Floor f : mCurrentBuildingPart.getFloors()) {
            if (room.getFloor().getMapUri().equals(f.getMapUri())) {
                setFloor(f, false);
                return;
            }
        }
    }

    public void clearSelection() {
        if (mSelectedMarker != null) {
            mTileView.removeMarker(mSelectedMarker);
        }
        mRoomDetailView.setVisibility(View.GONE);
        mSelectedRoom = null;
    }

    public void addOnFloorChangedListener(OnFloorChangedListener listener) {
        mOnFloorChangedListeners.add(listener);
    }

    public void removeOnFloorChangedListener(OnFloorChangedListener listener) {
        mOnFloorChangedListeners.remove(listener);
    }

    private void addFloorButtons() {
        mFloorButtons = new ArrayList<>();
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        List<Floor> floors = new ArrayList<>(mFloorList);
        Collections.sort(floors, Collections.reverseOrder(ModelHelper.floorComparator));
        for (Floor f : floors) {
            FloorButton fb = new FloorButton();
            fb.button = (Button) inflater.inflate(R.layout.tileview_floor_button, mFloorButtonLayout, false);
            fb.button.setText(f.getLevel());
            fb.floor = f;
            mFloorButtonLayout.addView(fb.button, 2 + floors.indexOf(f));
            mFloorButtons.add(fb);
        }

        mRootView.post(new Runnable() {
            @Override
            public void run() {
                final Resources res = getResources();

                int rootHeight = mRootView.getHeight();
                int buttonHeight = res.getDimensionPixelSize(R.dimen.tileview_button_size);
                int buttonMargin = res.getDimensionPixelSize(R.dimen.tileview_button_margin);
                int dividerHeight = res.getDimensionPixelSize(R.dimen.tileview_divider_height);

                int availableExpandedSpace = rootHeight - 2 * buttonHeight - 2 * buttonMargin - dividerHeight;
                int expandedButtonLayoutHeight = (mFloorButtons.size() + 2) * buttonHeight + buttonMargin + 2 * dividerHeight;

                mHideZoomButtonsWhenExpanded = expandedButtonLayoutHeight > availableExpandedSpace;
                mZoomButtonTranslation = buttonHeight + buttonMargin;
            }
        });
    }

    private void clearFloorButtons() {
        for (FloorButton fb : mFloorButtons) {
            mFloorButtonLayout.removeView(fb.button);
        }
        mHideZoomButtonsWhenExpanded = false;
    }

    private void setBuildingPart(BuildingPart buildingPart) {
        if (!mHasBuildingPartsWithDifferentMaps || buildingPart.equals(mCurrentBuildingPart)) {
            return;
        }

        mCurrentBuildingPart = buildingPart;
        mCurrentFloor = null;

        mFloorList = getFloorList();
        clearFloorButtons();
        addFloorButtons();
        updateBuildingPartButtonSelection();
    }

    private void setFloor(Floor newFloor) {
        setFloor(newFloor, true);
    }

    private void setFloor(Floor newFloor, boolean preserveState) {
        if (newFloor.equals(mCurrentFloor)) {
            collapseFloorButtons();
            return;
        }

        clearSelection();

        // destroy old tileview, but preserve scale and position
        float scale = 0.125f;
        int xPos = 0;
        int yPos = 0;
        if (mTileView != null) {

            if (preserveState) {
                scale = mTileView.getScale();
                xPos = mTileView.getScrollX();
                yPos = mTileView.getScrollY();
            }

            final TileView removedTileView = mTileView;
            removedTileView.animate()
                    .alpha(0f)
                    .setDuration(FLOOR_CHANGE_CROSSFADE_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            onAnimationEnd(animation);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            removedTileView.setVisibility(View.GONE);
                            removedTileView.destroy();
                            removedTileView.removeView(mTileView);
                        }
                    })
                    .start();
        }

        // add new tileview
        mTileView = new ListenableTileView(getActivity());
        mTileView.setBitmapProvider(mBitmapProvider);
        mTileView.setTransitionsEnabled(true);
        mTileView.addEventListener(this);
        mTileView.setAlpha(0f);

        mTileView.animate().alpha(1f).setDuration(FLOOR_CHANGE_CROSSFADE_DURATION).start();

        mTileViewContainer.addView(mTileView);

        // original image size
        mTileView.setSize(newFloor.getMapSizeX(), newFloor.getMapSizeY());

        // tiles are loaded from server, samples are loaded from assets folder
        mTileView.addDetailLevel(1.0f, ModelHelper.getFloorTilesPath(newFloor, "1000"));
        mTileView.addDetailLevel(0.5f, ModelHelper.getFloorTilesPath(newFloor, "500"));
        mTileView.addDetailLevel(0.25f, ModelHelper.getFloorTilesPath(newFloor, "250"));
        mTileView.addDetailLevel(0.125f, ModelHelper.getFloorTilesPath(newFloor, "125"));

        final ImageView sampleImage = new ImageView(getActivity());
        Picasso.with(getActivity())
                .load(ModelHelper.getFloorSamplePath(newFloor))
                .noPlaceholder()
                .into(sampleImage);
        mTileView.addView(sampleImage, 0);

        // restore scale and position
        mTileView.setShouldScaleToFit(true);
        // TODO: compute scale limits from map size
        mTileView.setScaleLimits(TILEVIEW_MIN_SCALE, TILEVIEW_MAX_SCALE);
        mTileView.setScale(scale);
        mTileView.scrollTo(xPos, yPos);

        mCurrentFloor = newFloor;
        for (OnFloorChangedListener l : mOnFloorChangedListeners) {
            l.onFloorChanged(newFloor, mTileView);
        }

        Toast.makeText(getActivity(), newFloor.getName(), Toast.LENGTH_SHORT).show();
        collapseFloorButtons();
        checkFloorBounds();
    }

    private void expandFloorButtons() {
        if (mHideZoomButtonsWhenExpanded) {
            mZoomButtonLayout.animate().translationX(mZoomButtonTranslation).start();
        }

        for (final FloorButton b : mFloorButtons) {
            b.button.setVisibility(View.VISIBLE);
            b.button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFloor(b.floor);
                }
            });
        }

        mHandler.postDelayed(mAutoHideFloorButtonsRunnable, FLOOR_BUTTONS_AUTOCOLLAPSE_DELAY);
    }

    private void collapseFloorButtons() {
        mHandler.removeCallbacks(mAutoHideFloorButtonsRunnable);

        for (FloorButton b : mFloorButtons) {
            if (b.floor.equals(mCurrentFloor)) {
                b.button.setVisibility(View.VISIBLE);
                b.button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expandFloorButtons();
                    }
                });
            } else {
                b.button.setVisibility(View.GONE);
            }
        }

        if (mHideZoomButtonsWhenExpanded) {
            mZoomButtonLayout.animate().translationX(0).setStartDelay(250).start();
        }
    }

    private void checkFloorBounds() {
        if (mFloorList.indexOf(mCurrentFloor) == 0)
            mButtonFloorDown.setEnabled(false);
        else
            mButtonFloorDown.setEnabled(true);

        if (mFloorList.indexOf(mCurrentFloor) == mFloorList.size() - 1)
            mButtonFloorUp.setEnabled(false);
        else
            mButtonFloorUp.setEnabled(true);
    }

    public boolean onBackPressed() {
        if (mSelectedRoom != null) {
            clearSelection();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        mTileView.destroy();
        mDatabaseManager.close();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mTileView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mTileView.resume();
        super.onResume();
    }

    @OnClick(R.id.tileview_button_zoom_in)
    void zoomIn() {
        mTileView.smoothScaleFromCenter(mTileView.getScale() * 2);
    }

    @OnClick(R.id.tileview_button_zoom_out)
    void zoomOut() {
        mTileView.smoothScaleFromCenter(mTileView.getScale() / 2);
    }

    @OnClick(R.id.tileview_button_floor_up)
    void floorUp() {
        setFloor(mFloorList.get(mFloorList.indexOf(mCurrentFloor) + 1));
    }

    @OnClick(R.id.tileview_button_floor_down)
    void floorDown() {
        setFloor(mFloorList.get(mFloorList.indexOf(mCurrentFloor) - 1));
    }

    @OnClick(R.id.tileview_room_details)
    void centerSelectedRoom() {
        if (mSelectedMarker != null) {
            mTileView.moveToMarker(mSelectedMarker, true);
        }
    }

    @OnClick(R.id.tileview_button_buildingparts)
    void toogleBuildingPartButtons() {
        for (BuildingPartButton pb : mBuildingPartButtons) {
            if (pb.button.getVisibility() == View.VISIBLE) {
                pb.button.setVisibility(View.GONE);
            } else {
                pb.button.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onFingerDown(MotionEvent event) {
        collapseFloorButtons();
    }

    @Override
    public void onScaleChanged(float newScale, float oldScale) {
        collapseFloorButtons();

        if (newScale == TILEVIEW_MAX_SCALE)
            mButtonZoomIn.setEnabled(false);
        else
            mButtonZoomIn.setEnabled(true);

        if (newScale == TILEVIEW_MIN_SCALE)
            mButtonZoomOut.setEnabled(false);
        else
            mButtonZoomOut.setEnabled(true);
    }

    @Override
    public void onSingleTap(MotionEvent event) {
        collapseFloorButtons();
        if (mSelectedRoom != null) {
            clearSelection();
        }
    }

    @Override
    public void onLongPress(MotionEvent event) {
        if (BuildConfig.DEBUG) {
            toggleRoomOverlay();
        }
    }

    private void toggleRoomOverlay() {
        if (overlayShown) {
            roomOverlay.hide();
            overlayShown = false;
        } else if (mCurrentFloor != null) {
            roomOverlay.show(mCurrentFloor, mTileView);
            overlayShown = true;
        }
    }

    public interface OnFloorChangedListener {
        void onFloorChanged(Floor floor, ListenableTileView tileView);
    }

    private class FloorButton {
        Floor floor;
        Button button;
    }

    private class BuildingPartButton {
        BuildingPart part;
        Button button;
    }
}
