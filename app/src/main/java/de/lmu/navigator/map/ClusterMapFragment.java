package de.lmu.navigator.map;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.app.BuildingDetailActivity;
import de.lmu.navigator.app.MainActivity;
import de.lmu.navigator.database.model.Building;
import io.realm.RealmResults;

public class ClusterMapFragment extends SupportMapFragment implements
        ClusterManager.OnClusterClickListener<BuildingItem>,
        ClusterManager.OnClusterItemClickListener<BuildingItem>,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapClickListener {

    private static final String LOG_TAG = SupportMapFragment.class.getSimpleName();

    private static final LatLng INITIAL_POSITION = new LatLng(48.150690, 11.580360);
    private static final int INITIAL_ZOOM = 13;
    private static final int SELECTION_ZOOM = 17;

    private GoogleMap mGoogleMap;

    private MyClusterManager mClusterManager;
    private MyClusterRenderer mClusterRenderer;
    private List<BuildingItem> mDialogClusterItems;
    private BuildingItem mSelectedItem;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int result = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (result == ConnectionResult.SUCCESS) {
            mGoogleMap = getMap();
            mGoogleMap.setMyLocationEnabled(true);
        } else {
            CrashlyticsCore.getInstance()
                    .logException(new GooglePlayServicesNotAvailableException(result));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mGoogleMap == null) {
            return;
        }

        if (mClusterManager == null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(INITIAL_POSITION, INITIAL_ZOOM));
            setUpClusterer();
        }

        if (mSelectedItem != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(mSelectedItem.getPosition(), SELECTION_ZOOM));
            Marker marker = mClusterRenderer.getMarker(mSelectedItem);
            if (marker != null) {
                marker.showInfoWindow();
            }
        }
    }

    private void setUpClusterer() {
        mClusterManager = new MyClusterManager(getActivity(), mGoogleMap);
        mClusterRenderer = new MyClusterRenderer(this, mGoogleMap, mClusterManager);
        mClusterManager.setRenderer(mClusterRenderer);
        mClusterManager.setAlgorithm(new MyClusterAlgorithm<BuildingItem>(40));
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);

        mGoogleMap.setOnCameraChangeListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMapClickListener(this);

        addItems();
    }

    private void addItems() {
        RealmResults<Building> buildings = ((MainActivity) getActivity()).getBuildings();
        mClusterManager.addItems(Lists.transform(buildings,
                new Function<Building, BuildingItem>() {
                    @Override
                    public BuildingItem apply(Building input) {
                        return BuildingItem.wrap(input);
                    }
                }));
        mClusterManager.cluster();
    }

    public BuildingItem getSelectedItem() {
        return mSelectedItem;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        startActivity(BuildingDetailActivity
                .newIntent(getActivity(), mSelectedItem.getBuilding().getCode()));
    }

    @Override
    public boolean onClusterItemClick(BuildingItem item) {
        mSelectedItem = item;
        return false;
    }

    @Override
    public boolean onClusterClick(Cluster<BuildingItem> cluster) {
        mSelectedItem = null;
        LatLngBounds.Builder builder = LatLngBounds.builder();
        boolean shouldZoom = false;

        if (mGoogleMap.getCameraPosition().zoom < 15) {
            LatLng lastPosition = null;
            for (BuildingItem item : cluster.getItems()) {
                if (lastPosition != null
                        && !item.getPosition().equals(lastPosition))
                    shouldZoom = true;

                lastPosition = item.getPosition();
                builder.include(item.getPosition());
            }
        }

        if (shouldZoom) {
            int padding = getResources().getDimensionPixelSize(R.dimen.map_cluster_padding);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(), padding));
        } else {
            mDialogClusterItems = Lists.newArrayList(cluster.getItems());
            String[] items = new String[cluster.getSize()];
            for (int i = 0; i < mDialogClusterItems.size(); i++) {
                items[i] = mDialogClusterItems.get(i).getBuilding().getDisplayName();
            }

            new MaterialDialog.Builder(getActivity())
                    .title(R.string.map_cluster_dialog_title)
                    .items(items)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i,
                                                CharSequence charSequence) {
                            mSelectedItem = mDialogClusterItems.get(i);
                            mDialogClusterItems = null;
                            onInfoWindowClick(null);
                        }
                    })
                    .show();
        }

        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mSelectedItem = null;
    }
}
