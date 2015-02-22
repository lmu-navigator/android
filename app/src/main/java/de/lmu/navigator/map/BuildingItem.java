package de.lmu.navigator.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.Building;

public class BuildingItem implements ClusterItem {

    private Building mBuilding;
    private LatLng mPosition;

    public static BuildingItem wrap(Building building) {
        return new BuildingItem(building);
    }

    private BuildingItem(Building building) {
        mBuilding = building;
        mPosition = ModelHelper.getBuildingLatLng(mBuilding);
    }

    public Building getBuilding() {
        return mBuilding;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

}
