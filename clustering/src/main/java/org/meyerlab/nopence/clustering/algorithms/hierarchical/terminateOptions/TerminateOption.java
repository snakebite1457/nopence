package org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions;

import org.meyerlab.nopence.clustering.algorithms.hierarchical.HierarchicalClusterer;

/**
 * @author Dennis Meyer
 */
public interface TerminateOption {

    public boolean checkTerminated(HierarchicalClusterer clusterer);
}
