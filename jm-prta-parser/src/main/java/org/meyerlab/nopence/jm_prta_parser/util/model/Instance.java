package org.meyerlab.nopence.jm_prta_parser.util.model;



import org.meyerlab.nopence.jm_prta_parser.attributes.Attribute;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dennis Meyer
 */
public class Instance implements Comparable<Instance> {

    private Date _beginDate;
    private Date _endDate;

    private Set<Integer> _events;

    public Instance(Date beginDate, Date endDate) {
        _beginDate = (Date) beginDate.clone();
        _endDate = (Date) endDate.clone();
        _events = new HashSet<>();
    }

    public void addEvents(Set<Attribute> attributeList) {
        if (attributeList == null) {
            return;
        }

        attributeList.forEach(attr -> _events.add(attr.getId()));
    }

    public Date getBeginDate() {
        return (Date) _beginDate.clone();
    }


    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Instance)) {
            return false;
        }

        Instance other = (Instance) obj;
        return _beginDate.equals(other._beginDate);
    }

    public int hashCode() {
        return 31 * _beginDate.hashCode();
    }

    @Override
    public int compareTo(Instance other) {
        return _beginDate.compareTo(other._beginDate);
    }

    public void appendText(long time, StringBuilder sb) {
        _events.forEach(event -> sb.append(time).append(" ")
                .append(event).append("\n"));
    }
}
