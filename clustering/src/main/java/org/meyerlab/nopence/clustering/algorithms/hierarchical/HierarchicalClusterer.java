package org.meyerlab.nopence.clustering.algorithms.hierarchical;

import org.meyerlab.nopence.clustering.IClusterer;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.clusteringMethods.IClusteringMethod;
import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions.ITerminateOption;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;
import org.meyerlab.nopence.clustering.util.clustering.DistanceMatrix;

import java.util.List;

/**
 * @author Dennis Meyer
 */
public class HierarchicalClusterer implements IClusterer {

    private DistanceMatrix _distanceMatrix;
    private final ITerminateOption _terminateOption;
    private final IClusteringMethod _clusteringMethod;

    public HierarchicalClusterer(ITerminateOption terminateOption,
                                 IClusteringMethod clusteringMethod) {
        _terminateOption = terminateOption;
        _clusteringMethod = clusteringMethod;
    }

    public long getCurrentClusterSize() {
        return _distanceMatrix.getClusterSize();
    }

    public double getCurrentMinDistance() {
        return _distanceMatrix.getMinDistance();
    }

    @Override
    public void buildClusterer(List<Point> points,
                               IDistanceMeasure distanceMeasure) {

        _distanceMatrix = new DistanceMatrix(
                points, distanceMeasure, _clusteringMethod);
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
        while (!_terminateOption.checkTerminated(this)) {
            _distanceMatrix.mergeAndUpdate(_distanceMatrix.pollClosest());
        }
    }
}
