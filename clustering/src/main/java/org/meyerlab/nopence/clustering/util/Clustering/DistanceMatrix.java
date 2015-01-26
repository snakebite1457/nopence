package org.meyerlab.nopence.clustering.util.clustering;

import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;
import net.openhft.koloboke.collect.map.hash.HashObjObjMaps;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.clusteringMethods.IClusteringMethod;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;
import org.meyerlab.nopence.clustering.util.ClusteringHelper;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.util.cluster.SimpleCluster;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Dennis Meyer
 */
public class DistanceMatrix {

    private ClusterHashMap<SimpleCluster> _currentCluster;
    private IDistanceMeasure _distanceMeasure;
    private IClusteringMethod _clusteringMethod;

    private PriorityQueue<ClusterPairItem> _clusterDistances;
    private Map<String, ClusterPairItem> _clusterPairItems;

    private Map<String, Double> _pointDistances;

    public DistanceMatrix(List<Point> points,
                          IDistanceMeasure distanceMeasure,
                          IClusteringMethod clusteringMethod) {

        _currentCluster = new ClusterHashMap<>();
        _distanceMeasure = distanceMeasure;
        _clusteringMethod = clusteringMethod;

        _clusterDistances = new PriorityQueue<>();
        _clusterPairItems = HashObjObjMaps.newMutableMap();
        _pointDistances = HashObjDoubleMaps.newMutableMap();

        initCluster(points);
    }

    public double getMinDistance() {
        return _clusterDistances.peek().getClusterPair().getDistance();
    }

    public ClusterPair pollClosest() {
        ClusterPairItem clusterPairItem = _clusterDistances.poll();

        while (clusterPairItem != null && clusterPairItem.getRemoved()) {
            clusterPairItem = _clusterDistances.poll();
        }

        if (clusterPairItem == null) {
            return null;
        }

        ClusterPair clusterPair = clusterPairItem.getClusterPair();
        _clusterPairItems.remove(clusterPairItem.getHash());
        return clusterPair;
    }

    public void mergeAndUpdate(ClusterPair clusterPair) {

        Cluster first = _currentCluster.get(clusterPair.getFirstClusterId());
        Cluster second = _currentCluster.get(clusterPair.getSecondClusterId());

        // set all cluster pairs as removed which have an arc to second cluster
        removeClusterPairs(second);

        first.merge(second);
        _currentCluster.remove(second.getClusterId());

        // Update all distances that points to the new merged cluster
        updateClusterPairDistances(first);
    }

    public ClusterHashMap<SimpleCluster> getCluster() {
        return _currentCluster;
    }

    private void removeClusterPairs(Cluster removedCluster) {
        _clusterPairItems.values()
                .stream()
                .filter(item ->
                        item.getClusterPair().getFirstClusterId() == removedCluster.getClusterId()
                                || item.getClusterPair().getSecondClusterId() == removedCluster.getClusterId())
                .forEach(item -> item.setRemoved(true));
    }

    private void updateClusterPairDistances(Cluster mergedCluster) {
        _clusterPairItems.values()
                .stream()
                .filter(item ->
                        (item.getClusterPair().getFirstClusterId() == mergedCluster.getClusterId()
                                || item.getClusterPair().getSecondClusterId() == mergedCluster.getClusterId())
                                && !item.getRemoved())
                .forEach(item -> {
                    Cluster first =
                            _currentCluster.get(item.getClusterPair().getFirstClusterId());
                    Cluster second =
                            _currentCluster.get(item.getClusterPair().getSecondClusterId());

                    double distance = _clusteringMethod.calculateDistance
                            (first, second, this);

                    item.getClusterPair().setDistance(distance);
                });
    }

    public int getClusterSize() {
        return _currentCluster.size();
    }

    public double getPointDistance(long firstId, long secondId) {

        String hash = ClusteringHelper.buildPointHash(firstId, secondId);
        String hashReverse = ClusteringHelper.buildPointHash(secondId, firstId);

        if (_pointDistances.containsKey(hash)) {
            return _pointDistances.get(hash);
        }

        return _pointDistances.get(hashReverse);
    }

    private void initCluster(List<Point> points) {
        points
            .forEach(point ->
                    _currentCluster.addCluster(new SimpleCluster(point)));

        for (long i = 0; i < _currentCluster.size() - 1; i++) {
            for (long j = i + 1; j < _currentCluster.size(); j++) {
                Cluster first = _currentCluster.get(i);
                Cluster second = _currentCluster.get(j);

                double distance = _distanceMeasure.computeDistance
                        (first.getClusterSeed(), second.getClusterSeed());

                ClusterPair clusterPair = new ClusterPair(
                        first.getClusterId(), second.getClusterId(), distance);

                ClusterPairItem clusterPairItem = new ClusterPairItem
                        (clusterPair);

                _clusterDistances.add(clusterPairItem);
                _clusterPairItems.put(clusterPairItem.getHash(), clusterPairItem);

                // Store point distance information
                String hashCode = ClusteringHelper.buildPointHash(first
                                .getClusterSeed(), second.getClusterSeed());
                _pointDistances.put(hashCode, distance);
            }
        }
    }

}