package org.meyerlab.nopence.clustering.util;

import net.openhft.koloboke.collect.map.hash.HashIntDoubleMaps;

import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class Instance {

    public Map<Integer, Double> Values;

    public Instance(Map<Integer, Double> values) {
        Values = HashIntDoubleMaps.newImmutableMap(values);
    }
}
