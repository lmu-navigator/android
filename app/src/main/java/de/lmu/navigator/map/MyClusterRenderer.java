package de.lmu.navigator.map;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import de.lmu.navigator.R;

public class MyClusterRenderer extends DefaultClusterRenderer<BuildingItem> {
    public final static int ITEM_ICON_RES_ID = R.drawable.marker_lmu;
    public final static int CLUSTER_ICON_RES_ID = R.drawable.marker_lmu_cluster;
    public final static int CLUSTER_TEXT_STYLE_ID = android.R.style.TextAppearance_Holo_Medium_Inverse;

    private final IconGenerator mClusterIconGenerator;
    private Context mContext;
    private ClusterMapFragment mMapFragment;

    public MyClusterRenderer(ClusterMapFragment mapFragment, GoogleMap googleMap,
                             ClusterManager<BuildingItem> clusterManager) {
        super(mapFragment.getActivity(), googleMap, clusterManager);
        mMapFragment = mapFragment;
        mContext = mapFragment.getActivity();
        mClusterIconGenerator = new IconGenerator(mContext);
    }

    @Override
    protected void onBeforeClusterItemRendered(BuildingItem item,
            MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(ITEM_ICON_RES_ID));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<BuildingItem> cluster,
            MarkerOptions markerOptions) {
        mClusterIconGenerator.setBackground(mContext.getResources().getDrawable(CLUSTER_ICON_RES_ID));
        mClusterIconGenerator.setTextAppearance(CLUSTER_TEXT_STYLE_ID);

        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster
                .getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
                .anchor(0.5f, 0.5f).position(cluster.getPosition());
    }

    @Override
    protected void onClusterRendered(Cluster<BuildingItem> cluster, Marker marker) {
        super.onClusterRendered(cluster, marker);
    }

    @Override
    protected void onClusterItemRendered(BuildingItem item, Marker marker) {
        super.onClusterItemRendered(item, marker);
        if (item.equals(mMapFragment.getSelectedItem())) {
            marker.showInfoWindow();
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<BuildingItem> cluster) {
        return cluster.getSize() > 1;
    }
}
