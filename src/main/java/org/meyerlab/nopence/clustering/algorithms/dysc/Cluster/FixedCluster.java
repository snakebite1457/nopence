package org.meyerlab.nopence.clustering.algorithms.dysc.Cluster;

import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.measures.distance.IDistanceMeasure;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author Dennis Meyer
 */
public class FixedCluster extends Cluster {

    private long _oldPendingClusterId;

    public FixedCluster(Point first,
                        long oldPendingClusterId) {
        super(first);

        _oldPendingClusterId = oldPendingClusterId;
    }

    @Override
    public boolean addPoint(IDistanceMeasure distanceMeasure,
                            double epsilonDistance,
                            Point point) {

        if (distanceMeasure.computeDistance(getClusterSeed(), point) <
                epsilonDistance) {

            _points.put(point.Id, point);
            return true;
        }

        return false;
    }

    @Override
    public void reassignClusterSeed(IDistanceMeasure distanceMeasure) {
        double minDistance = Double.MAX_VALUE;
        long seedId = 0;

        for (Point point : _points.values()) {
            double distance = _points.values()
                    .stream()
                    .filter(p -> p.Id != point.Id)
                    .mapToDouble(p -> distanceMeasure.computeDistance(point, p))
                    .sum();

            if (distance < minDistance) {
                minDistance = distance;
                seedId = point.Id;
            }
        }

        if (_seedStateId != seedId) {
            System.out.println("Fixed cluster seed changed");
        }

        _seedStateId = seedId;
    }

    @Override
    public void removePoint(long pointId,
                            IDistanceMeasure distanceFunction) {
        throw new NotImplementedException();
    }

    public long getOldPendingClusterId() {
        return _oldPendingClusterId;
    }
}
