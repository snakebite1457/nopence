package org.meyerlab.nopence.clustering.util.Clustering;

import net.openhft.koloboke.collect.map.hash.HashLongDoubleMaps;
import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.Cluster.Cluster;
import org.meyerlab.nopence.clustering.util.Cluster.SimpleCluster;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;
import org.meyerlab.nopence.clustering.util.ClusteringMethod;
import org.meyerlab.nopence.utils.Helper;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Dennis Meyer
 */
public class DistanceMatrix {

    private ClusterHashMap<SimpleCluster> _currentCluster;
    private IDistanceMeasure _distanceMeasure;
    private ClusteringMethod _clusteringMethod;

    private PriorityQueue<ClusterPairItem> _clusterDistances;
    private Map<Long, ClusterPairItem> _clusterPairItems;

    private Map<Long, Double> _pointDistances;

    public DistanceMatrix(List<Point> points,
                          IDistanceMeasure distanceMeasure,
                          ClusteringMethod clusteringMethod) {

        _currentCluster = new ClusterHashMap<>();
        _distanceMeasure = distanceMeasure;
        _clusteringMethod = clusteringMethod;

        _clusterDistances = new PriorityQueue<>();
        _clusterPairItems = HashLongObjMaps.newMutableMap();
        _pointDistances = HashLongDoubleMaps.newMutableMap();

        initCluster(points);
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

        first.merge(second, _distanceMeasure);
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
                    switch (_clusteringMethod) {
                        case averageLink:
                            updateDistAverageLink(item.getClusterPair());
                            break;
                        case completeLink:
                            updateDistCompleteLink(item.getClusterPair());
                            break;
                        case singleLink:
                            updateDistSingleLink(item.getClusterPair());
                            break;
                    }
                });
    }

    public int getClusterSize() {
        return _currentCluster.size();
    }

    private void updateDistAverageLink(ClusterPair clusterPair) {

        Cluster first = _currentCluster.get(clusterPair.getFirstClusterId());
        Cluster second = _currentCluster.get(clusterPair.getSecondClusterId());

        final double[] distance = {0};
        first.getClusterPoints()
                .forEach(p1 -> second.getClusterPoints()
                        .forEach(p2 -> {
                            int hashCode = (p1.Id + p2.Id + "").hashCode();
                            distance[0] += _pointDistances.get(hashCode);
                        }));

        distance[0] /= (first.numPoints() * second.numPoints());
        clusterPair.setDistance(distance[0]);
    }

    private void updateDistCompleteLink(ClusterPair clusterPair) {

        Cluster first = _currentCluster.get(clusterPair.getFirstClusterId());
        Cluster second = _currentCluster.get(clusterPair.getSecondClusterId());

        final double[] maxDistance = {0};

        first.getClusterPoints()
                .forEach(p1 -> second.getClusterPoints()
                        .forEach(p2 -> {
                            int hashCode = (p1.Id + p2.Id + "").hashCode();

                            double distance = _pointDistances.get(hashCode);
                            if (maxDistance[0] < distance) {
                                maxDistance[0] = distance;
                            }
                        }));

        clusterPair.setDistance(maxDistance[0]);
    }

    private void updateDistSingleLink(ClusterPair clusterPair) {

        Cluster first = _currentCluster.get(clusterPair.getFirstClusterId());
        Cluster second = _currentCluster.get(clusterPair.getSecondClusterId());

        final double[] minDistance = {Double.MAX_VALUE};

        first.getClusterPoints()
                .forEach(p1 -> second.getClusterPoints()
                        .forEach(p2 -> {
                            long hashCode = Helper.createHash(p1.Id, p2.Id);
                            Double distance = _pointDistances.get(hashCode);

                            if (distance == null) {
                                hashCode = Helper.createHash(p2.Id, p1.Id);
                                distance = _pointDistances.get(hashCode);
                            }

                            if (minDistance[0] > distance) {
                                minDistance[0] = distance;
                            }
                        }));

        clusterPair.setDistance(minDistance[0]);
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
                long hashCode = Helper.createHash(first.getClusterSeed().Id,
                        second.getClusterSeed().Id);
                _pointDistances.put(hashCode, distance);
            }
        }
    }

}