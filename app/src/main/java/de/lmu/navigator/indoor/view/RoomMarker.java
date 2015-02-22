package de.lmu.navigator.indoor.view;

import android.content.Context;

import com.qozix.tileview.TileView;

import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Room;

public class RoomMarker extends TileViewMarker {
    public static final int ROOM_MARKER_DRAWABLE = R.drawable.marker_room;
    private Room room;

    public RoomMarker(Room room, Context context, TileView tileView) {
        super(context, ROOM_MARKER_DRAWABLE, tileView, room.getPosX(), room.getPosY());
        this.room = room;
    }
    
    public Room getRoom() {
        return room;
    }
}
