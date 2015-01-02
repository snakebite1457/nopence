package org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyEvents;

import org.meyerlab.nopence.clustering.algorithms.Points.Point;

import java.util.List;

/**
 * @author Dennis Meyer
 */
public class APreCallbackEvent {

    public double MinDistance;
    public long MinDistanceClusterId;
    public int WorkerId;

    public List<Point> PointsWithoutSeed;

    public boolean pointAdded;
}
