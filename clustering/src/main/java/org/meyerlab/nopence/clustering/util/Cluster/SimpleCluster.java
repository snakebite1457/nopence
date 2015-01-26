package org.meyerlab.nopence.clustering.util.cluster;

import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;

/**
 * @author Dennis Meyer
 */
public class SimpleCluster extends Cluster {

    public SimpleCluster(Point first) {
        super(first);
    }

    @Override
    public boolean addPoint(IDistanceMeasure distanceMeasure, Point point) {
        _points.put(point.Id, point);
        return true;
    }

    @Override
    public void removePoint(long pointId, IDistanceMeasure distanceMeasure) {
        if (_points.containsKey(pointId)) {
            _points.remove(pointId);
        }
    }

    @Override
    public void reassignClusterSeed(IDistanceMeasure distanceMeasure) {
        double minDistance = Double.MAX_VALUE;
        long seedId = 0;

        for (Point point : _points.values()) {
            double distance = _points.values()
                    .stream()
                    .mapToDouble(p -> distanceMeasure.computeDistance(point, p))
                    .sum();

            if (distance < minDistance) {
                minDistance = distance;
                seedId = point.Id;
            }
        }

        if (_seedStateId != seedId) {
            System.out.println("Fixed cluster seed changed");
            _seedStateId = seedId;
        }
    }
}
