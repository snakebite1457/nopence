package org.meyerlab.nopence.clustering.Points;

import net.openhft.koloboke.collect.map.hash.HashIntDoubleMaps;

import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class Point {

    public long Id;
    public Map<Integer, Double> Values;

    public Point(Map<Integer, Double> values) {
        Values = HashIntDoubleMaps.newMutableMap(values);
    }

    public Point copy() {
        Point newPoint = new Point(Values);
        newPoint.Id = Id;

        return newPoint;
    }
}
