package de.lmu.navigator.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.StaticCluster;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

public class MyClusterAlgorithm<T extends ClusterItem> implements Algorithm<T>{
    public int maxDistanceAtZoom;

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final Collection<QuadItem<T>> mItems = new ArrayList<QuadItem<T>>();

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final PointQuadTree<QuadItem<T>> mQuadTree = new PointQuadTree<QuadItem<T>>(0, 1, 0, 1);

    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(1);
    
    public MyClusterAlgorithm(int maxDistance) {
        maxDistanceAtZoom = maxDistance;
    }

    @Override
    public void addItem(T item) {
        final QuadItem<T> quadItem = new QuadItem<T>(item);
        synchronized (mQuadTree) {
            mItems.add(quadItem);
            mQuadTree.add(quadItem);
        }
    }

    @Override
    public void addItems(Collection<T> items) {
        for (T item : items) {
            addItem(item);
        }
    }

    @Override
    public void clearItems() {
        synchronized (mQuadTree) {
            mItems.clear();
            mQuadTree.clear();
        }
    }

    @Override
    public void removeItem(T item) {
    	final QuadItem<T> quadItem = new QuadItem<T>(item);
        synchronized (mQuadTree) {
        	Iterator it = mItems.iterator();
        	while (it.hasNext())
        	{
        		QuadItem<T> tmp = (QuadItem<T>) it.next(); 
        		if(tmp.getHashCode()==item.hashCode())
        		{
        			mItems.remove(tmp);
                    mQuadTree.remove(tmp);
                    break;
        		}
        	}
        }
//        throw new UnsupportedOperationException("NonHierarchicalDistanceBasedAlgorithm.remove not implemented");
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        final int discreteZoom = (int) zoom;

        final double zoomSpecificSpan = maxDistanceAtZoom / Math.pow(2, discreteZoom) / 256;

        final Set<QuadItem<T>> visitedCandidates = new HashSet<QuadItem<T>>();
        final Set<Cluster<T>> results = new HashSet<Cluster<T>>();
        final Map<QuadItem<T>, Double> distanceToCluster = new HashMap<QuadItem<T>, Double>();
        final Map<QuadItem<T>, StaticCluster<T>> itemToCluster = new HashMap<QuadItem<T>, StaticCluster<T>>();

        synchronized (mQuadTree) {
            for (QuadItem<T> candidate : mItems) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue;
                }

                Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
                Collection<QuadItem<T>> clusterItems;
                clusterItems = mQuadTree.search(searchBounds);
                if (clusterItems.size() == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate);
                    visitedCandidates.add(candidate);
                    distanceToCluster.put(candidate, 0d);
                    continue;
                }
                StaticCluster<T> cluster = new StaticCluster<T>(candidate.mClusterItem.getPosition());
                results.add(cluster);

                for (QuadItem<T> clusterItem : clusterItems) {
                    Double existingDistance = distanceToCluster.get(clusterItem);
                    double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue;
                        }
                        // Move item to the closer cluster.
                        itemToCluster.get(clusterItem).remove(clusterItem.mClusterItem);
                    }
                    distanceToCluster.put(clusterItem, distance);
                    cluster.add(clusterItem.mClusterItem);
                    itemToCluster.put(clusterItem, cluster);
                }
                visitedCandidates.addAll(clusterItems);
            }
        }
        return results;
    }

    @Override
    public Collection<T> getItems() {
        final List<T> items = new ArrayList<T>();
        synchronized (mQuadTree) {
            for (QuadItem<T> quadItem : mItems) {
                items.add(quadItem.mClusterItem);
            }
        }
        return items;
    }

    private double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private Bounds createBoundsFromSpan(Point p, double span) {
        double halfSpan = span / 2;
        return new Bounds(
                p.x - halfSpan, p.x + halfSpan,
                p.y - halfSpan, p.y + halfSpan);
    }

    private static class QuadItem<T extends ClusterItem> implements PointQuadTree.Item, Cluster<T> {
        private final T mClusterItem;
        private final Point mPoint;
        private final LatLng mPosition;
        private Set<T> singletonSet;
        private int hashCode;

        private QuadItem(T item) {
            mClusterItem = item;
            mPosition = item.getPosition();
            mPoint = PROJECTION.toPoint(mPosition);
            singletonSet = Collections.singleton(mClusterItem);
            hashCode = item.hashCode();
        }

        public int getHashCode()
        {
        	return hashCode;
        }
        @Override
        public Point getPoint() {
            return mPoint;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public Set<T> getItems() {
            return singletonSet;
        }

        @Override
        public int getSize() {
            return 1;
        }
    }
}
