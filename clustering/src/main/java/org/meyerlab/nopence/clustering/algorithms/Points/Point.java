package org.meyerlab.nopence.clustering.algorithms.points;

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

    @Override
    public String toString() {
        StringBuilder pointStringBuilder = new StringBuilder();
        pointStringBuilder.append("Point Id: " + Id);
        pointStringBuilder.append("\n");
        pointStringBuilder.append("Values");
        pointStringBuilder.append("\n");

        for (Map.Entry<Integer, Double> entry : Values.entrySet()) {
            pointStringBuilder.append("Value Id: " + entry.getKey());
            pointStringBuilder.append("; ");
            pointStringBuilder.append("Value: " + entry.getValue());
            pointStringBuilder.append("\n");
        }
        return pointStringBuilder.toString();
    }
}
