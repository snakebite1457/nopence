package org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyEvents;

import org.meyerlab.nopence.clustering.algorithms.Points.Point;

import java.util.List;

/**
 * @author Dennis Meyer
 */
public class APreInputEvent {

    public Point Point;
    public List<Long> PointIdsToBeRemoved;
    public boolean removePoints;

    public APreInputEvent(Point point) {
        Point = point;
        removePoints = false;
    }

    public APreInputEvent(List<Long> pointIdsToBeRemoved) {
        PointIdsToBeRemoved = pointIdsToBeRemoved;
        removePoints = true;
    }
}
