package org.meyerlab.nopence.jmdatagen;

import org.meyerlab.nopence.jm_prta_parser.util.model.Sequence;

/**
 * @author Dennis Meyer
 */
public class DataGenerator {

    private static long lastSeqId = -1;


    public static Sequence generateSequence() {
        lastSeqId++;

        Sequence sequence = new Sequence(lastSeqId, lastSeqId);

        return sequence;
    }
}
