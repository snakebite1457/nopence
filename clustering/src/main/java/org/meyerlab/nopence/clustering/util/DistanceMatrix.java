package org.meyerlab.nopence.clustering.util;

import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.Cluster.SimpleCluster;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class DistanceMatrix {

    private ClusterHashMap<SimpleCluster> _currentCluster;
    private IDistanceMeasure _distanceMeasure;
    private ClusteringMethod _clusteringMethod;

    private List<Distance> _distances;

    private HashMap<String, Double> _pointDistances;

    public DistanceMatrix(List<Point> points,
                          IDistanceMeasure distanceMeasure,
                          ClusteringMethod clusteringMethod) {

        _currentCluster = new ClusterHashMap<>();
        _distances = new ArrayList<>();
        _distanceMeasure = distanceMeasure;
        _clusteringMethod = clusteringMethod;
        _pointDistances = new HashMap<>();

        initCluster(points);
    }

    public Distance peekClosest() {
        _distances.sort(new DistanceComparator());
        return _distances.get(0);
    }

    public Distance pollClosest() {
        _distances.sort(new DistanceComparator());
        return _distances.remove(0);
    }

    public void mergeAndUpdate(Distance dist) {
        _currentCluster.get(dist.FirstIndex).merge(_currentCluster.get
                (dist.SecondIndex), _distanceMeasure);

        _currentCluster.remove(dist.SecondIndex);
        _distances.remove(dist);

        // Delete all distance which points to the removed cluster
        List<Distance> remainingDistances = _distances
                .stream()
                .filter(d -> d.SecondIndex != dist.SecondIndex
                        && d.FirstIndex != dist.SecondIndex)
                .collect(Collectors.toList());
        _distances = remainingDistances;

        // Update all distances that points to the new merged cluster
        _distances
                .stream()
                .filter(d -> d.FirstIndex == dist.FirstIndex
                        || d.SecondIndex == dist.FirstIndex)
                .forEach(d -> {
                    switch (_clusteringMethod) {

                        case averageLink:
                            updateDistAverageLink(d, dist.FirstIndex);
                            break;
                        case completeLink:
                            updateDistCompleteLink(d, dist.FirstIndex);
                            break;
                        case singleLink:
                            updateDistSingleLink(d, dist.FirstIndex);
                            break;
                    }
                });
    }

    public int getClusterSize() {
        return _currentCluster.size();
    }

    public ClusterHashMap<SimpleCluster> getCluster() {
        return _currentCluster;
    }

    private void updateDistAverageLink(Distance dist, long recentMerged) {
        long secondClusterId = dist.FirstIndex == recentMerged
                ? dist.SecondIndex
                : dist.FirstIndex;

        final double[] updatedDistance = {0};

        // Iterate over recent merged cluster
        _currentCluster.get(recentMerged).getClusterPoints()
                .forEach(p1 -> _currentCluster.get(secondClusterId).getClusterPoints()
                        .forEach(p2 -> {
                            String code = p1.Id + ";" + p2.Id;
                            if (!_pointDistances.containsKey(code)) {
                                double distance = _distanceMeasure
                                        .computeDistance(p1, p2);
                                _pointDistances.put(code, distance);
                            }

                            updatedDistance[0] += _pointDistances.get(code);
                        }));

        dist.Distance = updatedDistance[0] /
                (_currentCluster.get(recentMerged).numPoints()
                    * _currentCluster.get(secondClusterId).numPoints());
    }

    private void updateDistCompleteLink(Distance dist, long recentMerged) {
        long secondClusterId = dist.FirstIndex == recentMerged
                ? dist.SecondIndex
                : dist.FirstIndex;

        final double[] maxDistance = {0};

        // Iterate over recent merged cluster
        _currentCluster.get(recentMerged).getClusterPoints()
                .forEach(p1 -> _currentCluster.get(secondClusterId).getClusterPoints()
                        .forEach(p2 -> {
                            String code = p1.Id + ";" + p2.Id;
                            if (!_pointDistances.containsKey(code)) {
                                double distance = _distanceMeasure
                                        .computeDistance(p1, p2);
                                _pointDistances.put(code, distance);
                            }

                            double distance = _pointDistances.get(code);

                            if (maxDistance[0] < distance) {
                                maxDistance[0] = distance;
                            }
                        }));

        dist.Distance = maxDistance[0];
    }

    private void updateDistSingleLink(Distance dist, long recentMerged) {
        long secondClusterId = dist.FirstIndex == recentMerged
                ? dist.SecondIndex
                : dist.FirstIndex;

        final double[] minDistance = {Double.MAX_VALUE};

        // Iterate over recent merged cluster
        _currentCluster.get(recentMerged).getClusterPoints()
                .forEach(p1 -> _currentCluster.get(secondClusterId).getClusterPoints()
                        .forEach(p2 -> {
                            String code = p1.Id + ";" + p2.Id;
                            if (!_pointDistances.containsKey(code)) {
                                double distance = _distanceMeasure
                                        .computeDistance(p1, p2);
                                _pointDistances.put(code, distance);
                            }

                            double distance = _pointDistances.get(code);

                            if (minDistance[0] > distance) {
                                minDistance[0] = distance;
                            }
                        }));

        dist.Distance = minDistance[0];
    }

    private void initCluster(List<Point> points) {
        points
            .forEach(point ->
                    _currentCluster.addCluster(new SimpleCluster(point)));

        for (long i = 0; i < _currentCluster.size() - 1; i++) {
            for (long j = i + 1; j < _currentCluster.size(); j++) {
                double distance = _distanceMeasure.computeDistance
                        (_currentCluster.get(i).getClusterSeed(),
                                _currentCluster.get(j).getClusterSeed());

                Distance dist = new Distance(i, j, distance);
                _distances.add(dist);

                // Store point distance information
                String code = _currentCluster.get(i).getClusterSeed().Id + ";"
                        + _currentCluster.get(j).getClusterSeed().Id;
                _pointDistances.put(code, distance);
            }
        }
    }

}

class Distance {

    public long FirstIndex;
    public long SecondIndex;
    public double Distance;

    public Distance(long firstIndex, long secondIndex, double distance) {
        FirstIndex = firstIndex;
        SecondIndex = secondIndex;
        Distance = distance;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Distance) {
            Distance other = (Distance) obj;

            return other.SecondIndex == SecondIndex && other.FirstIndex ==
                    FirstIndex;
        }

        return false;
    }
}

class DistanceComparator implements  Comparator<Distance> {

    @Override
    public int compare(Distance o1, Distance o2) {
        return Double.compare(o1.Distance, o2.Distance);
    }
}