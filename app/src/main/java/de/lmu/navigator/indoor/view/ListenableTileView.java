package de.lmu.navigator.indoor.view;

import android.content.Context;
import android.view.MotionEvent;

import com.qozix.tileview.TileView;

import java.util.ArrayList;
import java.util.List;

public class ListenableTileView extends TileView {

    private List<TileViewEventListener> mListeners = new ArrayList<>();

    public ListenableTileView(Context context) {
        super(context);
    }

    public void addEventListener(TileViewEventListener listener) {
        mListeners.add(listener);
    }

    public void removeEventListener(TileViewEventListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onScaleChanged(float scale, float previous) {
        super.onScaleChanged(scale, previous);
        for (TileViewEventListener listener : mListeners) {
            if (listener != null) {
                listener.onScaleChanged(scale, previous);
            }
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        for (TileViewEventListener listener : mListeners) {
            if (listener != null) {
                listener.onSingleTap(event);
            }
        }
        return super.onSingleTapConfirmed(event);
    }

    @Override
    public void onShowPress(MotionEvent event) {
        super.onShowPress(event);
        for (TileViewEventListener listener : mListeners) {
            if (listener != null) {
                listener.onFingerDown(event);
            }
        }
    }

}
