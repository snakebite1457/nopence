package org.meyerlab.nopence.clustering.algorithms.hierarchical;

import org.meyerlab.nopence.clustering.IClusterer;
import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.IPerformanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.SilhouetteCoefficient;
import org.meyerlab.nopence.clustering.util.Cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;
import org.meyerlab.nopence.clustering.util.ClusteringMethod;
import org.meyerlab.nopence.clustering.util.DistanceMatrix;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class HierarchicalClusterer implements IClusterer {

    private DistanceMatrix _distanceMatrix;
    private int _terminateClusterSize;
    private ClusteringMethod _clusteringMethod;
    private IDistanceMeasure _distanceMeasure;

    private List<Point> _points;


    public HierarchicalClusterer(int terminateClusterSize,
                                 ClusteringMethod clusteringMethod) {
        _terminateClusterSize = terminateClusterSize;
        _clusteringMethod = clusteringMethod;
    }

    @Override
    public void buildClusterer(List<Point> points,
                               IDistanceMeasure distanceMeasure) {
        _distanceMatrix = new DistanceMatrix(
                points, distanceMeasure, _clusteringMethod);

        _distanceMeasure = distanceMeasure;
        _points = points;
    }

    @Override
    public ClusterHashMap<Cluster> getCluster() {
        ClusterHashMap<Cluster> clusters = new ClusterHashMap<>();

        _distanceMatrix.getCluster()
                .values()
                .forEach(clusters::addCluster);

        return clusters;
    }

    @Override
    public void start() {
        while (_distanceMatrix.getClusterSize() > _terminateClusterSize) {
            _distanceMatrix.mergeAndUpdate(_distanceMatrix.pollClosest());
        }
    }
}
