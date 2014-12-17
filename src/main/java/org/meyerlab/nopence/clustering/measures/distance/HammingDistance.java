package org.meyerlab.nopence.clustering.measures.distance;

import org.meyerlab.nopence.clustering.DimensionInformation;
import org.meyerlab.nopence.clustering.Points.Point;

/**
 * @author Dennis Meyer
 */
public class HammingDistance implements IDistanceMeasure {

    private DimensionInformation _dimensionInformation;

    public HammingDistance(DimensionInformation dimensionInformation) {
        _dimensionInformation = dimensionInformation.copy();
    }

    @Override
    public double computeDistance(Point first, Point second) {
        final double[] distance = {0};

        _dimensionInformation.DimensionTypeMapping
                .keySet()
                .forEach(dim -> {
                    if (first.Values.containsKey(dim)
                            && second.Values.containsKey(dim)) {
                        if (!first.Values.get(dim)
                                .equals(second.Values.get(dim))) {
                            distance[0]++;
                        }
                    } else {
                        distance[0]++;
                    }
                });

        return distance[0];
    }

    @Override
    public IDistanceMeasure copy() {
        return new HammingDistance(_dimensionInformation);
    }
}
