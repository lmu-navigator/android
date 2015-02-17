package de.lmu.navigator.indoor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingPartOld;
import de.lmu.navigator.model.FloorOld;
import de.lmu.navigator.model.RoomOld;

@EFragment(R.layout.fragment_tileview)
public class TileViewFragment extends Fragment implements
                TileViewEventListener {
    private final static int ZOOM_ANIMATION_DURATION = 500;
    private final static int FLOOR_BUTTONS_AUTOCOLLAPSE_DELAY = 5000;
    private final static double TILEVIEW_MAX_SCALE = 4.0;
    private final static double TILEVIEW_MIN_SCALE = 0.125;
    private final static int FLOOR_CHANGE_CROSSFADE_DURATION = 250;
    
    @SystemService
    LayoutInflater mLayoutInflater;
    
    @ViewById(R.id.tileview_container)
    FrameLayout mTileViewContainer;
    
    @ViewById(R.id.tileview_button_layout)
    LinearLayout mButtonLayout;
    
    @ViewById(R.id.tileview_button_floor_up)
    ImageButton mButtonFloorUp;
    
    @ViewById(R.id.tileview_button_floor_down)
    ImageButton mButtonFloorDown;
    
    @ViewById(R.id.tileview_button_zoom_in)
    ImageButton mButtonZoomIn;
    
    @ViewById(R.id.tileview_button_zoom_out)
    ImageButton mButtonZoomOut;

    @ViewById(R.id.tileview_room_details)
    View mRoomDetailView;

    @ViewById(R.id.room_detail_name)
    TextView mRoomDetailName;

    @ViewById(R.id.room_detail_floor)
    TextView mRoomDetailFloor;

    @FragmentArg
    BuildingPartOld buildingPart;

    @FragmentArg
    RoomOld mSelectedRoom;

    private TileView mTileView;
    private List<FloorOld> mFloorList;
    private List<FloorButton> mFloorButtons;
    private FloorOld mCurrentFloor;
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
        public void onFloorChanged(FloorOld floor, TileView tileView);
    }
    
    class FloorButton {
        FloorOld floor;
        Button button;
    }

    @AfterViews
    protected void init() {
        mFloorList = buildingPart.getFloors();

        addFloorButtons();
        setFloor(buildingPart.getStartFloor(mFloorList));

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

    public void onBuildingPartChanged(BuildingPartOld buildingPart) {
        // TODO
    }

    public void onRoomSelected(RoomOld room) {
        clearSelection();

        setFloor(room.getFloorCode());

        mTileView.addMarker(mSelectedMarker, room.getPosX(), room.getPosY(), -0.5f, -1f);
        mTileView.setScale(1); // TODO: define better scale
        mTileView.moveToMarker(mSelectedMarker, false);

        mRoomDetailView.setVisibility(View.VISIBLE);
        mRoomDetailName.setText("Raum " + room.getName());
        mRoomDetailFloor.setText(room.getFloor().getDisplayName());

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
        Collections.sort(mFloorList);
        Collections.reverse(mFloorList);
        for (FloorOld f : mFloorList) {
            FloorButton fb = new FloorButton();
            fb.button = (Button) mLayoutInflater.inflate(R.layout.tileview_floor_button, mButtonLayout, false);
            fb.button.setText(f.getShortName());
            fb.floor = f;
            mButtonLayout.addView(fb.button, 2 + mFloorList.indexOf(f));
            mFloorButtons.add(fb);
        }
    }

    public void setFloor(String floorCode) {
        for (FloorOld f : mFloorList) {
            if (f.getCode().equals(floorCode)) {
                setFloor(f);
                return;
            }
        }
    }

    public void setFloor(FloorOld newFloor) {
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
        mTileView.addDetailLevel(1.0f, newFloor.getDetailLevelTilesPath("1000"),
                newFloor.getSamplePath());
        mTileView.addDetailLevel(0.5f, newFloor.getDetailLevelTilesPath("500"),
                newFloor.getSamplePath());
        mTileView.addDetailLevel(0.25f, newFloor.getDetailLevelTilesPath("250"),
                newFloor.getSamplePath());
        mTileView.addDetailLevel(0.125f, newFloor.getDetailLevelTilesPath("125"),
                newFloor.getSamplePath());

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

        Toast.makeText(getActivity(), newFloor.getDisplayName(), Toast.LENGTH_SHORT).show();
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
            if (b.floor == mCurrentFloor) {
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
    
    @Click(R.id.tileview_button_zoom_in)
    void zoomIn() {
        mTileView.smoothScaleTo(mTileView.getScale() * 2, ZOOM_ANIMATION_DURATION);
    }
    
    @Click(R.id.tileview_button_zoom_out)
    void zoomOut() {
        mTileView.smoothScaleTo(mTileView.getScale() / 2, ZOOM_ANIMATION_DURATION);
    }

    @Click(R.id.tileview_button_floor_up)
    void floorUp() {
        setFloor(mFloorList.get(mFloorList.indexOf(mCurrentFloor) + 1));
    }
    
    @Click(R.id.tileview_button_floor_down)
    void floorDown() {
        setFloor(mFloorList.get(mFloorList.indexOf(mCurrentFloor) - 1));
    }

    @Click(R.id.tileview_room_details)
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
