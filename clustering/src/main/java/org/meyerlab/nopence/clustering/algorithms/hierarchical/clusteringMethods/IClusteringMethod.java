package org.meyerlab.nopence.clustering.algorithms.hierarchical.clusteringMethods;

import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.util.clustering.DistanceMatrix;

/**
 * @author Dennis Meyer
 */
public interface IClusteringMethod {

    public double calculateDistance(Cluster first,
                                    Cluster second,
                                    DistanceMatrix distanceMatrix);
}
