package org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions;

import org.meyerlab.nopence.clustering.algorithms.hierarchical.HierarchicalClusterer;

/**
 * @author Dennis Meyer
 */
public class MinDistanceTerminationOption implements TerminateOption {

    private final double _maxDistance;

    public MinDistanceTerminationOption(double maxDistance) {
        _maxDistance = maxDistance;
    }

    @Override
    public boolean checkTerminated(HierarchicalClusterer clusterer) {
        return clusterer.getCurrentClusterSize() > 1
                &&  clusterer.getCurrentMinDistance() > _maxDistance;
    }
}
