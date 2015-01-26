package org.meyerlab.nopence.clustering.util.clustering;

/**
 * @author Dennis Meyer
 */
public class ClusterPairItem implements Comparable<ClusterPairItem> {

    private final ClusterPair _clusterPair;
    private final String _hash;
    private boolean _removed = false;

    public ClusterPairItem(ClusterPair clusterPair) {
        _clusterPair = clusterPair;
        _hash = ClusterPair.getHashCode(clusterPair);
    }

    @Override
    public int compareTo(ClusterPairItem o) {
        return _clusterPair.compareTo(o._clusterPair);
    }

    @Override
    public String toString() {
        return String.valueOf(_hash);
    }

    public ClusterPair getClusterPair() {
        return _clusterPair;
    }

    public String getHash() {
        return _hash;
    }

    public void setRemoved(boolean value) {
        _removed = value;
    }

    public boolean getRemoved() {
        return _removed;
    }
}
