package org.meyerlab.nopence.clustering.algorithms.dysc.Cluster;

import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * @author Dennis Meyer
 */
public class FixedCluster extends Cluster {

    private List<Point> _remainingPendingPoints;

    public FixedCluster(Point first,
                        double epsilonDistance) {
        super(first, epsilonDistance);
    }

    @Override
    public boolean addPoint(IDistanceMeasure distanceMeasure,
                            Point point) {

        if (distanceMeasure.computeDistance(getClusterSeed(), point) <
                _epsilonDistance) {

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


    public List<Point> remainingPendingPoints() {
        return _remainingPendingPoints;
    }

    public void setRemainingPendingPoints(
            List<Point> remainingPendingPoints) {
        _remainingPendingPoints = remainingPendingPoints;
    }
}
