package de.lmu.navigator.indoor;

import android.app.Activity;
import android.view.View;

import com.qozix.tileview.TileView;

import java.util.ArrayList;
import java.util.List;

import de.lmu.navigator.indoor.view.RoomMarker;
import de.lmu.navigator.model.FloorOld;
import de.lmu.navigator.model.RoomOld;

public class RoomOverlay extends TileViewOverlay implements View.OnClickListener {

    private List<RoomMarker> mMarkerList;

    public RoomOverlay(Activity context, TileView tileView) {
        super(context, tileView);
    }

    @Override
    public void onActivate(FloorOld f) {
        // ignore
    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public void onFloorChange(FloorOld f) {
        mMarkerList.clear();
        onShow(f);
    }

    @Override
    public void onHide() {

    }

    @Override
    public void onShow(FloorOld f) {
        mMarkerList = new ArrayList<RoomMarker>(f.getRooms().size());
        for (RoomOld r : f.getRoomsIncludeAdjacent()) {
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
