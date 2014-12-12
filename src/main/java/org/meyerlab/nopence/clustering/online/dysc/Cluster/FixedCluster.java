package org.meyerlab.nopence.clustering.online.dysc.Cluster;

import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;
import org.meyerlab.nopence.clustering.Points.Point;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author Dennis Meyer
 */
public class FixedCluster extends Cluster {

    public FixedCluster(Point first) {
        super(first);
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
    public void removePoint(long pointId,
                            IDistanceMeasure distanceFunction) {
        throw new NotImplementedException();
    }
}
