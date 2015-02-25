package de.lmu.navigator.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.Building;

public class BuildingItem implements ClusterItem {

    private Building mBuilding;
    private LatLng mPosition;
    private String mCode;

    public static BuildingItem wrap(Building building) {
        return new BuildingItem(building);
    }

    private BuildingItem(Building building) {
        mBuilding = building;
        mCode = building.getCode();
        mPosition = ModelHelper.getBuildingLatLng(mBuilding);
    }

    public Building getBuilding() {
        return mBuilding;
    }

    public String getCode() {
        return mCode;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BuildingItem)) {
            return false;
        }

        return ((BuildingItem) o).mCode.equals(mCode);
    }
}
