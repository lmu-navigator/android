package de.lmu.navigator.indoor;

import android.app.Activity;
import android.view.MotionEvent;

import com.qozix.tileview.TileView;

import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.indoor.view.ListenableTileView;
import de.lmu.navigator.indoor.view.TileViewEventListener;

public abstract class TileViewOverlay implements TileViewFragment.OnFloorChangedListener, TileViewEventListener {
    private FloorViewActivity mActivity;
    private ListenableTileView mTileView;
    private OnFloorChangeCompleteListener mFloorChangeListener;

    public interface OnFloorChangeCompleteListener {
        public void onFloorChangeComplete(Floor f);
    }

    public TileViewOverlay(Activity context, ListenableTileView tileView) {
        mActivity = (FloorViewActivity) context;
        mTileView = tileView;
    }
    
    public FloorViewActivity getActivity() {
        return mActivity;
    }
    
    public TileView getTileView() {
        return mTileView;
    }
    
    public void activate(Floor f) {
        show(f);
        mTileView.addEventListener(this);
        onActivate(f);
    }
    
    public void deactivate() {
        hide();
        mTileView.removeEventListener(this);
        onDeactivate();
    }
    
    public void show(Floor f) {
        mActivity.getTileViewFragment().addOnFloorChangedListener(this);
        onShow(f);
    }
    
    public void hide() {
        mActivity.getTileViewFragment().removeOnFloorChangedListener(this);
        onHide();
    }
    
    public void setOnFloorChangeCompleteListener(OnFloorChangeCompleteListener listener) {
        mFloorChangeListener = listener;
    }

    @Override
    public void onFloorChanged(Floor f, ListenableTileView tv) {
        mTileView = tv;
        onFloorChange(f);
        if (mFloorChangeListener != null)
            mFloorChangeListener.onFloorChangeComplete(f);
    }
    
    public abstract void onActivate(Floor f);
    
    public abstract void onDeactivate();
    
    public abstract void onFloorChange(Floor f);
    
    public abstract void onHide();
    
    public abstract void onShow(Floor f);
    
    /* To respond to TileView events, override methods */

    @Override
    public void onScaleChanged(float newScale, float oldScale) {}

    @Override
    public void onSingleTap(MotionEvent event) {}

    @Override
    public void onFingerDown(MotionEvent event) {}
}
