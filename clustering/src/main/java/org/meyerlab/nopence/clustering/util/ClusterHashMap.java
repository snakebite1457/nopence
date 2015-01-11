package org.meyerlab.nopence.clustering.util;

import org.meyerlab.nopence.clustering.util.Cluster.Cluster;

import java.util.HashMap;

/**
 * @author Dennis Meyer
 */
public class ClusterHashMap<V extends Cluster> extends HashMap<Long, V> {

    private int addedCluster = 0;

    public void addCluster(V cluster) {
        cluster.setClusterId(addedCluster++);
        this.put(cluster.getClusterId(), cluster);
    }

    public V getCluster() {
        if (this.isEmpty()) {
            return null;
        }

        return this.entrySet().iterator().next().getValue();
    }

    public int numPoints() {
        return this.values()
                .stream()
                .mapToInt(Cluster::numPoints)
                .sum();
    }

    public boolean containsPoint(long pointId) {
        return this.values()
                .stream()
                .filter(cluster -> cluster.containsPoint(pointId))
                .count() > 0;
    }
}