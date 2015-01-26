package org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions;

import org.meyerlab.nopence.clustering.algorithms.hierarchical.HierarchicalClusterer;

/**
 * @author Dennis Meyer
 */
public interface ITerminateOption {

    public boolean checkTerminated(HierarchicalClusterer clusterer);
}
