package org.meyerlab.nopence.clustering.measures.distance;

import org.meyerlab.nopence.clustering.Points.Point;

/**
 * @author Dennis Meyer
 */
public interface IDistanceMeasure {

    public double computeDistance(Point first, Point second);

    public IDistanceMeasure copy();
}
