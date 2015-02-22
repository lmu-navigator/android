package de.lmu.navigator.indoor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
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

import com.qozix.tileview.TileView;
import com.qozix.tileview.TileView.TileViewEventListener;
import com.qozix.tileview.graphics.BitmapDecoderHttp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.app.BaseFragment;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;

public class TileViewFragment extends BaseFragment implements
                TileViewEventListener {

    private static final String LOG_TAG = TileViewFragment.class.getSimpleName();

    private static final String ARGS_BUILDING_PART_CODE = "ARGS_BUILDING_PART_CODE";
    private static final String ARGS_ROOM_CODE = "ARGS_ROOM_CODE";

    private final static int ZOOM_ANIMATION_DURATION = 500;
    private final static int FLOOR_BUTTONS_AUTOCOLLAPSE_DELAY = 5000;
    private final static double TILEVIEW_MAX_SCALE = 4.0;
    private final static double TILEVIEW_MIN_SCALE = 0.125;
    private final static int FLOOR_CHANGE_CROSSFADE_DURATION = 250;
    
    @InjectView(R.id.tileview_container)
    FrameLayout mTileViewContainer;
    
    @InjectView(R.id.tileview_button_layout)
    LinearLayout mButtonLayout;
    
    @InjectView(R.id.tileview_button_floor_up)
    ImageButton mButtonFloorUp;
    
    @InjectView(R.id.tileview_button_floor_down)
    ImageButton mButtonFloorDown;
    
    @InjectView(R.id.tileview_button_zoom_in)
    ImageButton mButtonZoomIn;
    
    @InjectView(R.id.tileview_button_zoom_out)
    ImageButton mButtonZoomOut;

    @InjectView(R.id.tileview_room_details)
    View mRoomDetailView;

    @InjectView(R.id.room_detail_name)
    TextView mRoomDetailName;

    @InjectView(R.id.room_detail_floor)
    TextView mRoomDetailFloor;

    private BuildingPart mBuildingPart;
    private Room mSelectedRoom;

    private TileView mTileView;
    private List<Floor> mFloorList;
    private List<FloorButton> mFloorButtons;
    private Floor mCurrentFloor;
    private ImageView mSelectedMarker;

    private Handler mHandler = new Handler();
    private Runnable mAutoHideButtonsRunnable = new Runnable() {
        @Override
        public void run() {
            collapseFloorButtons();
        }
    };

    private List<OnFloorChangedListener> mOnFloorChangedListeners = new ArrayList<OnFloorChangedListener>();

    public interface OnFloorChangedListener {
        public void onFloorChanged(Floor floor, TileView tileView);
    }
    
    class FloorButton {
        Floor floor;
        Button button;
    }

    public static TileViewFragment newInstance(BuildingPart buildingPart) {
        TileViewFragment fragment = new TileViewFragment();
        Bundle args = new Bundle(1);
        args.putString(ARGS_BUILDING_PART_CODE, buildingPart.getCode());
        fragment.setArguments(args);
        return fragment;
    }

    public static TileViewFragment newInstance(Room room) {
        TileViewFragment fragment = new TileViewFragment();
        Bundle args = new Bundle(1);
        args.putString(ARGS_ROOM_CODE, room.getCode());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String roomCode = getArguments().getString(ARGS_ROOM_CODE);
        if (roomCode != null) {
            mSelectedRoom = mDatabaseManager.getRoom(roomCode);
            mBuildingPart = mSelectedRoom.getFloor().getBuildingPart();
        } else {
            String buildingPartCode = getArguments().getString(ARGS_BUILDING_PART_CODE);
            mBuildingPart = mDatabaseManager.getBuildingPart(buildingPartCode);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tileview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFloorList = mBuildingPart.getFloors();
        addFloorButtons();
        setFloor(getStartFloor());

        RoomOverlay roomOverlay = new RoomOverlay(getActivity(), mTileView);
        roomOverlay.show(mCurrentFloor);

        mSelectedMarker = new ImageView(getActivity());
        mSelectedMarker.setImageResource(R.drawable.marker_lmu);

        if (mSelectedRoom != null) {
            mTileView.post(new Runnable() {
                @Override
                public void run() {
                    onRoomSelected(mSelectedRoom);
                }
            });
        }
    }

    private Floor getStartFloor() {
        Collections.sort(mFloorList, ModelHelper.floorComparator);
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

    public void onBuildingPartChanged(BuildingPart buildingPart) {
        // TODO
    }

    public void onRoomSelected(Room room) {
        clearSelection();

        setFloor(room.getFloor());

        mTileView.addMarker(mSelectedMarker, room.getPosX(), room.getPosY(), -0.5f, -1f);
        mTileView.setScale(1); // TODO: define better scale
        mTileView.moveToMarker(mSelectedMarker, false);

        mRoomDetailView.setVisibility(View.VISIBLE);
        mRoomDetailName.setText("Raum " + room.getName());
        mRoomDetailFloor.setText(room.getFloor().getName());

        mSelectedRoom = room;
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
        mFloorButtons = new ArrayList<FloorButton>();
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        Collections.sort(mFloorList, Collections.reverseOrder(ModelHelper.floorComparator));
        for (Floor f : mFloorList) {
            FloorButton fb = new FloorButton();
            fb.button = (Button) inflater.inflate(R.layout.tileview_floor_button, mButtonLayout, false);
            fb.button.setText(f.getLevel());
            fb.floor = f;
            mButtonLayout.addView(fb.button, 2 + mFloorList.indexOf(f));
            mFloorButtons.add(fb);
        }
    }

    public void setFloor(Floor newFloor) {
        if (newFloor.equals(mCurrentFloor)) {
            collapseFloorButtons();
            return;
        }

        clearSelection();

        // destroy old tileview, but preserve scale and position
        double scale = 0.125;
        int xPos = 0;
        int yPos = 0;
        if (mTileView != null) {
            scale = mTileView.getScale();
            xPos = mTileView.getScrollX();
            yPos = mTileView.getScrollY();

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
                            removedTileView.removeView(mTileView);
                            removedTileView.destroy();
                        }
                    })
                    .start();
        }

        // add new tileview
        mTileView = new TileView(getActivity());
        mTileView.setTileDecoder(new BitmapDecoderHttp());
        mTileView.setCacheEnabled(false); // TODO: caching enabled leads to random crashes
        mTileView.setTransitionsEnabled(true);
        mTileView.addTileViewEventListener(this);
        mTileView.setAlpha(0f);

        mTileView.animate().alpha(1f).setDuration(FLOOR_CHANGE_CROSSFADE_DURATION).start();

        mTileViewContainer.addView(mTileView);
        
        // original image size
        mTileView.setSize(newFloor.getMapSizeX(), newFloor.getMapSizeY());

        // tiles are loaded from server, samples are loaded from assets folder
        String samplePath = ModelHelper.getFloorSamplePath(newFloor);
        mTileView.addDetailLevel(1.0f, ModelHelper.getFloorTilesPath(newFloor, "1000"), samplePath);
        mTileView.addDetailLevel(0.5f, ModelHelper.getFloorTilesPath(newFloor, "500"), samplePath);
        mTileView.addDetailLevel(0.25f, ModelHelper.getFloorTilesPath(newFloor, "250"), samplePath);
        mTileView.addDetailLevel(0.125f, ModelHelper.getFloorTilesPath(newFloor, "125"), samplePath);

        // restore scale and position
        mTileView.setScaleToFit(true);
        // TODO: compute scale limits from map size
        mTileView.setScaleLimits(TILEVIEW_MIN_SCALE, TILEVIEW_MAX_SCALE);
        mTileView.setScale(scale);
        mTileView.moveTo(xPos, yPos);

        mCurrentFloor = newFloor;
        for (OnFloorChangedListener l : mOnFloorChangedListeners) {
            l.onFloorChanged(newFloor, mTileView);
        }

        Toast.makeText(getActivity(), newFloor.getName(), Toast.LENGTH_SHORT).show();
        collapseFloorButtons();
        checkFloorBounds();
    }
    
    private void expandFloorButtons() {
        for (final FloorButton b : mFloorButtons) {
            b.button.setVisibility(View.VISIBLE);
            b.button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFloor(b.floor);
                }
            });
        }
        
        mHandler.postDelayed(mAutoHideButtonsRunnable, FLOOR_BUTTONS_AUTOCOLLAPSE_DELAY);
    }
    
    private void collapseFloorButtons() {
        mHandler.removeCallbacks(mAutoHideButtonsRunnable);
        
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
        mTileView.smoothScaleTo(mTileView.getScale() * 2, ZOOM_ANIMATION_DURATION);
    }
    
    @OnClick(R.id.tileview_button_zoom_out)
    void zoomOut() {
        mTileView.smoothScaleTo(mTileView.getScale() / 2, ZOOM_ANIMATION_DURATION);
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
    
    @Override
    public void onDetailLevelChanged() {
        // ignore
    }

    @Override
    public void onDoubleTap(int arg0, int arg1) {
        // ignore
    }

    @Override
    public void onDrag(int arg0, int arg1) {
        // ignore
    }

    @Override
    public void onFingerDown(int arg0, int arg1) {
        collapseFloorButtons();
    }

    @Override
    public void onFingerUp(int arg0, int arg1) {
        // ignore
    }

    @Override
    public void onFling(int arg0, int arg1, int arg2, int arg3) {
        // ignore
    }

    @Override
    public void onFlingComplete(int arg0, int arg1) {
        // ignore
    }

    @Override
    public void onPinch(int arg0, int arg1) {
        // ignore
        
    }

    @Override
    public void onPinchComplete(int arg0, int arg1) {
        // ignore
        
    }

    @Override
    public void onPinchStart(int arg0, int arg1) {
        // ignore
        
    }

    @Override
    public void onRenderComplete() {
        // ignore
    }

    @Override
    public void onRenderStart() {
        // ignore
    }

    @Override
    public void onScaleChanged(double scale) {
        collapseFloorButtons();
        
        if (scale == TILEVIEW_MAX_SCALE)
            mButtonZoomIn.setEnabled(false);
        else
            mButtonZoomIn.setEnabled(true);
        
        if (scale == TILEVIEW_MIN_SCALE)
            mButtonZoomOut.setEnabled(false);
        else
            mButtonZoomOut.setEnabled(true);
    }

    @Override
    public void onScrollChanged(int arg0, int arg1) {
        // ignore
    }

    @Override
    public void onTap(int arg0, int arg1) {
        collapseFloorButtons();
        if (mSelectedRoom != null) {
            clearSelection();
        }
    }

    @Override
    public void onZoomComplete(double arg0) {
        // ignore
    }

    @Override
    public void onZoomStart(double arg0) {
        // ignore
    }
}
