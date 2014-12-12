package org.meyerlab.nopence.clustering.Points;

import net.openhft.koloboke.collect.map.hash.HashIntIntMaps;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class OrdinalPoint extends Point {

    private Map<Integer,Integer> _dependencies;

    public OrdinalPoint(Map<Integer, Double> values,
                        Map<Integer, Integer> dependencies) {
        super(values);

        _dependencies = HashIntIntMaps.newImmutableMap(dependencies);
    }

    public List<Integer> getDependentValueIds(int valueId) {
        return null;
    }


}
