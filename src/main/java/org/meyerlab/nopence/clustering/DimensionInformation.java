package org.meyerlab.nopence.clustering;

import net.openhft.koloboke.collect.map.hash.HashIntIntMaps;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

import java.util.List;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class DimensionInformation {

    public enum DimensionType {
        NOMINAL(1), ORDINAL(2), NUMERIC(3);

        private int numVal;

        DimensionType(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }
    }

    public Map<Integer, Integer> DimensionType;

    public Map<Integer, List<Integer>> OrdinalTypeMapping;

    public void init(Map<Integer, Integer> dimensionType,
                     Map<Integer, List<Integer>> ordinalTypeMapping) {
        DimensionType = HashIntIntMaps.newImmutableMap(dimensionType);
        OrdinalTypeMapping = HashIntObjMaps.newImmutableMap(ordinalTypeMapping);
    }

    public DimensionInformation copy() {
        DimensionInformation copy = new DimensionInformation();
        copy.init(DimensionType, OrdinalTypeMapping);

        return copy;
    }

}
