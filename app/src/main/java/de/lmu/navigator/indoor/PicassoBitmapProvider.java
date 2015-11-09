package de.lmu.navigator.indoor;

import android.content.Context;
import android.graphics.Bitmap;

import com.qozix.tileview.graphics.BitmapProvider;
import com.qozix.tileview.tiles.Tile;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PicassoBitmapProvider implements BitmapProvider {

    @Override
    public Bitmap getBitmap(Tile tile, Context context) {
        final String pattern = (String) tile.getData();
        final String url = String.format(pattern, tile.getColumn(), tile.getRow());
        try {
            return Picasso.with(context).load(url).get();
        } catch (IOException e) {
            return null;
        }
    }
}
