package de.lmu.navigator.outdoor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingOld;

@EFragment
public class LMUMapFragment extends SupportMapFragment implements
        ClusterManager.OnClusterClickListener<BuildingOld>,
        ClusterManager.OnClusterItemClickListener<BuildingOld>,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapClickListener {

    private static final String LOG_TAG = SupportMapFragment.class.getSimpleName();
    private static final LatLng INITIAL_POSITION = new LatLng(48.150690, 11.580360);
    private static final int INITIAL_ZOOM = 13;
    private static final int SELECTION_ZOOM = 17;

    @FragmentArg
    BuildingOld mSelectedBuilding;

    private GoogleMap mGoogleMap;

    private boolean mIsRestoredState = false;

    private ClusterManager<BuildingOld> mClusterManager;
    private LMUClusterRenderer mClusterRenderer;
    private List<BuildingOld> mDialogClusterItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mGoogleMap = getMap();
        mGoogleMap.setMyLocationEnabled(true);

        if (mClusterManager == null) {
            mIsRestoredState = false;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    INITIAL_POSITION, mSelectedBuilding == null ? INITIAL_ZOOM : SELECTION_ZOOM));
            setUpClusterer();
        } else {
            mIsRestoredState = true;
        }

        return v;
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<BuildingOld>(getActivity(), mGoogleMap);
        mClusterRenderer = new LMUClusterRenderer(this, mGoogleMap, mClusterManager);
        mClusterManager.setRenderer(mClusterRenderer);
        mClusterManager
                .setAlgorithm(new MyNonHierarchicalDistanceBasedAlgorithm<BuildingOld>(
                        40));
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);

        mGoogleMap.setOnCameraChangeListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMapClickListener(this);

        mClusterManager.addItems(BuildingOld.getAll());
    }

    @AfterViews
    void init() {
        if (!mIsRestoredState && mSelectedBuilding != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory
                    .newLatLng(mSelectedBuilding.getPosition()), 250, null);
            Marker marker = mClusterRenderer.getMarker(mSelectedBuilding);
            if (marker != null) {
                marker.showInfoWindow();
            }
        }
    }

    public BuildingOld getSelectedBuilding() {
        return mSelectedBuilding;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        BuildingDetailActivity_.intent(this)
                .mBuilding(mSelectedBuilding)
                .start();
    }

    @Override
    public boolean onClusterItemClick(BuildingOld item) {
        mSelectedBuilding = item;
        return false;
    }

    @Override
    public boolean onClusterClick(Cluster<BuildingOld> cluster) {
        mSelectedBuilding = null;
        LatLngBounds.Builder builder = LatLngBounds.builder();
        boolean shouldZoom = false;

        if (mGoogleMap.getCameraPosition().zoom < 15) {
            LatLng lastPosition = null;
            for (BuildingOld item : cluster.getItems()) {
                if (lastPosition != null
                        && !item.getPosition().equals(lastPosition))
                    shouldZoom = true;

                lastPosition = item.getPosition();
                builder.include(item.getPosition());
            }
        }

        if (shouldZoom) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(), 300));
        } else {
            String[] items = new String[cluster.getSize()];
            mDialogClusterItems = new ArrayList<BuildingOld>(cluster.getSize());
            Iterator<BuildingOld> iterator = cluster.getItems().iterator();
            for (int i = 0; i < cluster.getSize(); i++) {
                BuildingOld item = iterator.next();
                items[i] = item.getPrimaryText();
                mDialogClusterItems.add(item);
            }

            new MaterialDialog.Builder(getActivity())
                    .title(R.string.map_cluster_dialog_title)
                    .items(items)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int i,
                                                CharSequence charSequence) {
                            mSelectedBuilding = mDialogClusterItems.get(i);
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
        mSelectedBuilding = null;
    }
}
