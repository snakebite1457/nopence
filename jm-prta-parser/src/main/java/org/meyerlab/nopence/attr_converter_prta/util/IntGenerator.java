package org.meyerlab.nopence.attr_converter_prta.util;

/**
 * @author Dennis Meyer
 */
public class IntGenerator {

    private int _lastGeneratedNumber;

    public IntGenerator() {
        _lastGeneratedNumber = 0;
    }

    public int getNext() {
        return _lastGeneratedNumber++;
    }

    public int getLastNumber() {
        return _lastGeneratedNumber;
    }
}
