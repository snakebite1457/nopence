package org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions;

import org.meyerlab.nopence.clustering.algorithms.hierarchical.HierarchicalClusterer;

/**
 * @author Dennis Meyer
 */
public class ClusterSizeTerminateOption implements ITerminateOption {

    private final int _minClusterSize;

    public ClusterSizeTerminateOption(int minClusterSize) {
        _minClusterSize = minClusterSize;
    }

    @Override
    public boolean checkTerminated(HierarchicalClusterer clusterer) {
        return clusterer.getCurrentClusterSize() <= _minClusterSize;
    }
}
