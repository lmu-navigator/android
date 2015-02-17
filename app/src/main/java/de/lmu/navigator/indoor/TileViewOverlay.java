package de.lmu.navigator.indoor;

import android.app.Activity;

import com.qozix.tileview.TileView;
import com.qozix.tileview.TileView.TileViewEventListener;

import de.lmu.navigator.model.FloorOld;

public abstract class TileViewOverlay implements TileViewFragment.OnFloorChangedListener, TileViewEventListener {
    private FloorViewActivity mActivity;
    private TileView mTileView;
    private OnFloorChangeCompleteListener mFloorChangeListener;

    public interface OnFloorChangeCompleteListener {
        public void onFloorChangeComplete(FloorOld f);
    }

    public TileViewOverlay(Activity context, TileView tileView) {
        mActivity = (FloorViewActivity) context;
        mTileView = tileView;
    }
    
    public FloorViewActivity getActivity() {
        return mActivity;
    }
    
    public TileView getTileView() {
        return mTileView;
    }
    
    public void activate(FloorOld f) {
        show(f);
        mTileView.addTileViewEventListener(this);
        onActivate(f);
    }
    
    public void deactivate() {
        hide();
        mTileView.removeTileViewEventListener(this);
        onDeactivate();
    }
    
    public void show(FloorOld f) {
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
    
    public void onFloorChanged(FloorOld f, TileView tv) {
        mTileView = tv;
        onFloorChange(f);
        if (mFloorChangeListener != null)
            mFloorChangeListener.onFloorChangeComplete(f);
    }
    
    public abstract void onActivate(FloorOld f);
    
    public abstract void onDeactivate();
    
    public abstract void onFloorChange(FloorOld f);
    
    public abstract void onHide();
    
    public abstract void onShow(FloorOld f);
    
    /* To respond to TileView events, override methods */

    @Override
    public void onDetailLevelChanged() {}

    @Override
    public void onDoubleTap(int arg0, int arg1) {}

    @Override
    public void onDrag(int arg0, int arg1) {}

    @Override
    public void onFingerDown(int arg0, int arg1) {}

    @Override
    public void onFingerUp(int arg0, int arg1) {}

    @Override
    public void onFling(int arg0, int arg1, int arg2, int arg3) {}

    @Override
    public void onFlingComplete(int arg0, int arg1) {}

    @Override
    public void onPinch(int arg0, int arg1) {}

    @Override
    public void onPinchComplete(int arg0, int arg1) {}

    @Override
    public void onPinchStart(int arg0, int arg1) {}

    @Override
    public void onRenderComplete() {}

    @Override
    public void onRenderStart() {}

    @Override
    public void onScaleChanged(double arg0) {}

    @Override
    public void onScrollChanged(int arg0, int arg1) {}

    @Override
    public void onTap(int arg0, int arg1) {}

    @Override
    public void onZoomComplete(double arg0) {}

    @Override
    public void onZoomStart(double arg0) {}
}
