package de.lmu.navigator.indoor;

import android.app.Activity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.lmu.navigator.database.RealmDatabaseManager;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.indoor.view.RoomMarker;

public class RoomOverlay extends TileViewOverlay implements View.OnClickListener {

    private List<RoomMarker> mMarkerList;
    private RealmDatabaseManager mDatabaseManager;

    public RoomOverlay(Activity context, RealmDatabaseManager databaseManager) {
        super(context);
        mDatabaseManager = databaseManager;
    }

    @Override
    public void onActivate(Floor f) {
        // ignore
    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public void onFloorChange(Floor f) {
        mMarkerList.clear();
        onShow(f);
    }

    @Override
    public void onHide() {
        if (mMarkerList != null) {
            for (RoomMarker roomMarker : mMarkerList) {
                roomMarker.remove();
            }
            mMarkerList.clear();
        }
    }

    @Override
    public void onShow(Floor f) {
        mMarkerList = new ArrayList<>(f.getRooms().size());
        for (Room r : mDatabaseManager.getRoomsForFloor(f, true, false)) {
            RoomMarker m = new RoomMarker(r, getActivity(), getTileView());
            mMarkerList.add(m);
            m.add();
            m.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        RoomMarker m = (RoomMarker) v;
        getActivity().getTileViewFragment().onRoomSelected(m.getRoom());
    }
}
