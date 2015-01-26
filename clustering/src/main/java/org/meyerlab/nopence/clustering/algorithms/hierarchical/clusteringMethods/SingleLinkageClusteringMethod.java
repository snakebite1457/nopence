package org.meyerlab.nopence.clustering.algorithms.hierarchical.clusteringMethods;

import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.util.clustering.DistanceMatrix;

/**
 * @author Dennis Meyer
 */
public class SingleLinkageClusteringMethod implements IClusteringMethod {


    @Override
    public double calculateDistance(Cluster first,
                                    Cluster second,
                                    DistanceMatrix distanceMatrix) {

        double minDistance = Double.MAX_VALUE;
        for (Point firstPoint : first.getClusterPoints()) {
            for (Point secondPoint : second.getClusterPoints()) {
                double distance =
                        distanceMatrix.getPointDistance(firstPoint.Id, secondPoint.Id);

                if (minDistance > distance) {
                    minDistance = distance;
                }
            }
        }

        return minDistance;
    }
}
