package de.lmu.navigator.indoor.view;

import android.content.Context;
import android.widget.ImageView;

import com.qozix.tileview.TileView;

public class TileViewMarker extends ImageView {
    private final static float ANCHOR_X = -0.5f;
    private final static float ANCHOR_Y = -0.5f;
    private double posX;
    private double posY;
    private TileView tileView;

    public TileViewMarker(Context context, int resId, TileView tileView, double posX, double posY) {
        super(context);
        setImageResource(resId);
        this.posX = posX;
        this.posY = posY;
        this.tileView = tileView;
    }
    
    public void add() {
        tileView.addMarker(this, posX, posY, ANCHOR_X, ANCHOR_Y);
    }
    
    public void remove() {
        tileView.removeMarker(this);
    }
    
}
