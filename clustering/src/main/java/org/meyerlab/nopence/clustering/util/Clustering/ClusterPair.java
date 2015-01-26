package org.meyerlab.nopence.clustering.util.clustering;

/**
 * @author Dennis Meyer
 */
public class ClusterPair implements Comparable<ClusterPair> {

    private long _firstClusterId;
    private long _secondClusterId;
    private Double _distance;

    public ClusterPair() { }

    public ClusterPair(long firstClusterId, long secondClusterId, Double distance) {
        _firstClusterId = firstClusterId;
        _secondClusterId = secondClusterId;
        _distance = distance;
    }

    @Override
    public int compareTo(ClusterPair o) {
        int result;
        if (o == null || o._distance == null) {
            result = -1;
        } else if (_distance == null) {
            result = 1;
        } else {
            result = _distance.compareTo(o._distance);
        }

        return result;
    }

    public Double getDistance() {
        return _distance;
    }

    public void setDistance(Double _distance) {
        this._distance = _distance;
    }

    public long getFirstClusterId() {
        return _firstClusterId;
    }

    public void setFirstClusterId(int _firstClusterId) {
        this._firstClusterId = _firstClusterId;
    }

    public long getSecondClusterId() {
        return _secondClusterId;
    }

    public void setSecondClusterId(int _secondClusterId) {
        this._secondClusterId = _secondClusterId;
    }

    public static String getHashCode(ClusterPair clusterPair) {
        return clusterPair.getFirstClusterId()
                + ";;;;"
                + clusterPair.getSecondClusterId();
    }
}
