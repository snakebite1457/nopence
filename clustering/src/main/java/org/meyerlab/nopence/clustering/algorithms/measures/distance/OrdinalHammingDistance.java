package org.meyerlab.nopence.clustering.algorithms.measures.distance;

import net.openhft.koloboke.collect.set.hash.HashIntSets;
import org.meyerlab.nopence.clustering.DimensionInformation;
import org.meyerlab.nopence.clustering.DimensionType;
import org.meyerlab.nopence.clustering.algorithms.Points.Point;

import java.util.List;
import java.util.Set;

/**
 * @author Dennis Meyer
 */
public class OrdinalHammingDistance implements IDistanceMeasure {

    private DimensionInformation _dimensionInformation;
    private Set<Integer> _checkedDimensions;

    public OrdinalHammingDistance(DimensionInformation dimensionInformation) {
        _dimensionInformation = dimensionInformation.copy();
        _checkedDimensions = HashIntSets.newMutableSet();
    }

    @Override
    public double computeDistance(Point first, Point second) {
        double distance = 0;

        // Check if attr in v1 also contains in v2
        for (Integer dimId : first.Values.keySet()) {
            distance += buildDistance(first, second, dimId);
        }
        // Check if attr in v2 also contains in v1
        for (Integer dimId : second.Values.keySet()) {
            distance += buildDistance(first, second, dimId);
        }

        return distance;
    }

    @Override
    public IDistanceMeasure copy() {
        return new OrdinalHammingDistance(_dimensionInformation);
    }

    private double buildDistance(Point first, Point second, int curDimension) {

        double distance = 0;

        if (_checkedDimensions.contains(curDimension)) {
            return distance;
        }

        // TODO: Maybe catch if dimension can not be found
        if (_dimensionInformation.DimensionTypeMapping.get(curDimension) ==
                DimensionType.ORDINAL.getNumVal()) {

            List<Integer> curOrdinalDims =
                    _dimensionInformation.OrdinalTypeMapping.get(curDimension);

            // Get all vars with the current cluster id from event 1
            long ordinalDimsFirst = first.Values.keySet()
                    .stream()
                    .filter(curOrdinalDims::contains)
                    .count();

            // Get all vars with the current cluster id from event 2
            long ordinalDimsSecond = second.Values.keySet()
                    .stream()
                    .filter(curOrdinalDims::contains)
                    .count();

            // If one event have more vars from the original ordinal
            // attribute then the distance increase
            if (ordinalDimsFirst != ordinalDimsSecond) {
                // Example: 5 ordinal values, event 1 has 2 and event 2 has 3.
                // Then calc 3 - 2 / 5 = 1/5; Means the distance between
                // these two events for this attribute is 1/5;

                distance += (double) Math.abs(ordinalDimsFirst - ordinalDimsSecond)
                        / (double) curOrdinalDims.size();
            }

            curOrdinalDims.forEach(_checkedDimensions::add);

        } else if (first.Values.containsKey(curDimension)
                && !second.Values.containsKey(curDimension)
                || !first.Values.containsKey(curDimension)
                && second.Values.containsKey(curDimension)) {
            // Variable occur either in event 1 or in event 2
            distance++;
        }

        _checkedDimensions.add(curDimension);
        return distance;
    }
}
