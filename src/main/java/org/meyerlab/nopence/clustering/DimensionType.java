package org.meyerlab.nopence.clustering;

/**
 * @author Dennis Meyer
 */
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
