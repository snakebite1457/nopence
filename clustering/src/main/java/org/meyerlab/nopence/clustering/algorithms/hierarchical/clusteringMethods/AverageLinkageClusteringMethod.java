package org.meyerlab.nopence.clustering.algorithms.hierarchical.clusteringMethods;

import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.util.ClusteringHelper;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.util.clustering.DistanceMatrix;

/**
 * @author Dennis Meyer
 */
public class AverageLinkageClusteringMethod implements ClusteringMethod {


    @Override
    public double calculateDistance(Cluster first,
                                    Cluster second,
                                    DistanceMatrix distanceMatrix) {

        double distance = 0;
        for (Point firstPoint : first.getClusterPoints()) {
            for (Point secondPoint : second.getClusterPoints()) {
                distance +=
                        distanceMatrix.getPointDistance(firstPoint.Id, secondPoint.Id);
            }
        }

        distance /= (first.numPoints() * second.numPoints());
        return distance;
    }
}
