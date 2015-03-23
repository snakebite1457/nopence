package org.meyerlab.nopence.attr_converter_prta.util.model;



import org.meyerlab.nopence.attr_converter_prta.attributes.Attribute;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dennis Meyer
 */
public class Instance implements Comparable<Instance> {

    private Date _observationDate;

    private Set<Integer> _events;

    public Instance(Date observationDate) {
        _observationDate = (Date) observationDate.clone();
        _events = new HashSet<>();
    }

    public void addEvents(Set<Attribute> attributeList) {
        if (attributeList == null) {
            return;
        }

        attributeList.forEach(attr -> _events.add(attr.getId()));
    }

    public Date getObservationDate() {
        return (Date) _observationDate.clone();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Instance)) {
            return false;
        }

        Instance other = (Instance) obj;
        return _observationDate.equals(other._observationDate);
    }

    @Override
    public int hashCode() {
        return 31 * _observationDate.hashCode();
    }

    @Override
    public int compareTo(Instance other) {
        return _observationDate.compareTo(other._observationDate);
    }

    public void appendText(long time, StringBuilder sb) {
        _events.forEach(event -> sb.append(time).append(" ")
                .append(event).append("\n"));
    }
}
