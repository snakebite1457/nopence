package org.meyerlab.nopence.clustering;

import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public interface IClusterer {

    public void buildClusterer(List<Point> points,
                               IDistanceMeasure distanceMeasure);

    public Map<Long, List<Long>> start();
}
