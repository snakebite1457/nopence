package org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyEvents;

import org.meyerlab.nopence.clustering.algorithms.points.Point;

import java.util.List;

/**
 * @author Dennis Meyer
 */
public class APreInputEvent {

    public Point Point;
    public List<Long> PointIdsToBeRemoved;
    public boolean removePoints;
    public boolean reassignClusterSeeds;

    public APreInputEvent(Point point) {
        Point = point;
        removePoints = false;
        reassignClusterSeeds = false;
    }

    public APreInputEvent(List<Long> pointIdsToBeRemoved) {
        PointIdsToBeRemoved = pointIdsToBeRemoved;
        removePoints = true;
        reassignClusterSeeds = false;
    }

    public APreInputEvent() {
        reassignClusterSeeds = true;
    }
}
