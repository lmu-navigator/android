package de.lmu.navigator.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyClusterManager extends ClusterManager<BuildingItem> {

    private List<BuildingItem> mItems = new ArrayList<>();

    public MyClusterManager(Context context, GoogleMap map) {
        super(context, map);
    }

    @Override
    public void addItem(BuildingItem item) {
        super.addItem(item);
        mItems.add(item);
    }

    @Override
    public void clearItems() {
        super.clearItems();
        mItems.clear();
    }

    @Override
    public void addItems(Collection<BuildingItem> items) {
        super.addItems(items);
        mItems.addAll(items);
    }

    @Override
    public void removeItem(BuildingItem item) {
        super.removeItem(item);
        mItems.remove(item);
    }

    public List<BuildingItem> getItems() {
        return mItems;
    }

    public BuildingItem findItem(String buildingCode) {
        for (BuildingItem item : mItems) {
            if (item.getBuilding().getCode().equals(buildingCode)) {
                return item;
            }
        }
        return null;
    }
}
