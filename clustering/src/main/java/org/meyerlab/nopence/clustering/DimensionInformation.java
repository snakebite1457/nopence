package org.meyerlab.nopence.clustering;

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

    public DimensionInformation(Map<Integer, Integer> dimensionTypeMapping,
                                Map<Integer, List<Integer>> ordinalTypeMapping) {
        DimensionTypeMapping = HashIntIntMaps.newImmutableMap(dimensionTypeMapping);
        OrdinalTypeMapping = HashIntObjMaps.newImmutableMap(ordinalTypeMapping);
    }

    public DimensionInformation(Map<Integer, Integer> dimensionTypeMapping) {
        DimensionTypeMapping = HashIntIntMaps.newImmutableMap(dimensionTypeMapping);
    }

    public DimensionInformation copy() {

        return OrdinalTypeMapping != null
            ? new DimensionInformation(DimensionTypeMapping, OrdinalTypeMapping)
            : new DimensionInformation(DimensionTypeMapping);
    }
}