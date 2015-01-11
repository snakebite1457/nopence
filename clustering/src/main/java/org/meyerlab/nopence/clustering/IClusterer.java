package org.meyerlab.nopence.clustering;

import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.util.Cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;

import java.util.List;

/**
 * @author Dennis Meyer
 */
public interface IClusterer {

    public void buildClusterer(List<Point> points,
                               IDistanceMeasure distanceMeasure);

    public ClusterHashMap<Cluster> getCluster();

    public void start();
}
