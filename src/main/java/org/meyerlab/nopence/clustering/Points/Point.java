package org.meyerlab.nopence.clustering.Points;

import net.openhft.koloboke.collect.map.hash.HashIntDoubleMaps;

import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class Point {

    public long Id;
    public Map<Integer, Double> Values;

    public Point(Map<Integer, Double> values, long id) {
        Values = HashIntDoubleMaps.newMutableMap(values);
        Id = id;
    }

    public Point copy() {
        return new Point(Values, Id);
    }
}
