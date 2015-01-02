package org.meyerlab.nopence.jm_prta_parser.util.model;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;
import java.util.TreeSet;

/**
 * @author Dennis Meyer
 */
public class Sequence {

    private final long _id;
    private final long _personId;

    // key -> time; value -> list of events
    private TreeSet<Instance> _timeEventMap;

    public Sequence(long id, long personId) {
        _id = id;
        _personId = personId;
        _timeEventMap = new TreeSet<>();
    }

    public void addInstance(Instance inst) {
        _timeEventMap.add(inst);
    }

    public boolean equalPersonId(long personId) {
        return _personId == personId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("seqId ").append(_id).append("\n");

        Date firstBeginDate = _timeEventMap.first().getBeginDate();
        while (!_timeEventMap.isEmpty()) {
            Instance inst = _timeEventMap.pollFirst();
            long daysDiff = Days.daysBetween(new DateTime(firstBeginDate),
                    new DateTime((inst.getBeginDate()))).getDays();

            inst.appendText(daysDiff, builder);
        }

        return builder.append("\n").toString();
    }
}
