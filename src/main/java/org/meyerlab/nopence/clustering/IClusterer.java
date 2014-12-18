package org.meyerlab.nopence.clustering;

import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.util.ClusterHashMap;

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
