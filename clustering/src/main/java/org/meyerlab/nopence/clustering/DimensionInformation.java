package org.meyerlab.nopence.clustering;

import net.openhft.koloboke.collect.map.hash.HashIntDoubleMaps;
import net.openhft.koloboke.collect.map.hash.HashIntIntMaps;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class DimensionInformation {

    public Map<Integer, Integer> DimensionTypeMapping;
    public Map<Integer, List<Integer>> OrdinalTypeMapping;
    public Map<Integer, Double> DimensionWeight;


    public DimensionInformation(Map<Integer, Integer> dimensionTypeMapping,
                                Map<Integer, List<Integer>> ordinalTypeMapping,
                                Map<Integer, Double> dimensionWeight) {
        DimensionTypeMapping = HashIntIntMaps.newImmutableMap(dimensionTypeMapping);
        OrdinalTypeMapping = HashIntObjMaps.newImmutableMap(ordinalTypeMapping);
        DimensionWeight = HashIntDoubleMaps.newImmutableMap(dimensionWeight);
    }

    public DimensionInformation(Map<Integer, Integer> dimensionTypeMapping) {
        DimensionTypeMapping = HashIntIntMaps.newImmutableMap(dimensionTypeMapping);
    }

    public DimensionInformation copy() {

        return OrdinalTypeMapping != null
            ? new DimensionInformation(DimensionTypeMapping,
                OrdinalTypeMapping, DimensionWeight)
            : new DimensionInformation(DimensionTypeMapping);
    }
}