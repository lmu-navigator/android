package de.lmu.navigator.indoor.view;

import android.content.Context;

import com.qozix.tileview.TileView;

import de.lmu.navigator.R;
import de.lmu.navigator.model.RoomOld;

public class RoomMarker extends TileViewMarker {
    public static final int ROOM_MARKER_DRAWABLE = R.drawable.marker_room;
    private RoomOld room;

    public RoomMarker(RoomOld room, Context context, TileView tileView) {
        super(context, ROOM_MARKER_DRAWABLE, tileView, room.getPosX(), room.getPosY());
        this.room = room;
    }
    
    public RoomOld getRoom() {
        return room;
    }
}
