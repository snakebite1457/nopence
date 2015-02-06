package org.meyerlab.nopence.soep_parser.util.model;

import java.util.*;

/**
 * @author Dennis Meyer
 */
public class Instance implements Comparable<Instance> {

    private int _personId;
    private int _year;
    private int _month;

    private Map<Integer, String> _values;

    public int getYear() {
        return _year;
    }

    public int getMonth() { return _month; }

    public int getPersonId() {
        return _personId;
    }

    public Map<Integer, String> getValues() {
        return _values;
    }

    public Instance(int year, int month,
                    Map<Integer, String> values, int personId) {
        _values = values;
        _month = month;
        _year = year;
        _personId = personId;
    }

    @Override
    public int compareTo(Instance o) {
        return Integer.compare(_year, o._year);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(_personId).append(",");

        // Add date of observation
        builder.append("01.").append(_month).append(".").append(_year);
        builder.append(",");

        Set<Integer> keys = _values.keySet();
        List<Integer> sortedKeys = new ArrayList<>(keys);
        Collections.sort(sortedKeys);

        for (Integer key : sortedKeys) {
            builder.append(_values.get(key)).append(",");
        }


        return builder.toString();
    }
}
