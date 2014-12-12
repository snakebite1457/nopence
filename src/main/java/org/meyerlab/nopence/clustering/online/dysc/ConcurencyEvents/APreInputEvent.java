package org.meyerlab.nopence.clustering.online.dysc.ConcurencyEvents;

import org.meyerlab.nopence.clustering.Points.Point;

/**
 * @author Dennis Meyer
 */
public class APreInputEvent {

    public Point Point;

    public APreInputEvent(Point point) {
        Point = point;
    }
}
