package de.lmu.navigator.indoor.view;

import android.view.MotionEvent;

public interface TileViewEventListener {

    void onScaleChanged(float newScale, float oldScale);

    void onSingleTap(MotionEvent event);

    void onFingerDown(MotionEvent event);
}
