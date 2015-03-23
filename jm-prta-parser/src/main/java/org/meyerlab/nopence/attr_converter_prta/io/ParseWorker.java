package org.meyerlab.nopence.attr_converter_prta.io;

import java.util.concurrent.CountDownLatch;

/**
 * @author Dennis Meyer
 */
public class ParseWorker implements Runnable {

    private CountDownLatch _doneSignal;
    private final DataParser _parser;
    private String[] _splittedLine;
    private boolean _lastLine;

    public ParseWorker(DataParser parser) {
        _parser = parser;
    }

    public void reset(CountDownLatch doneSignal,
                      String[] splittedLine,
                      boolean lastLine) {
        _splittedLine = splittedLine;
        _doneSignal = doneSignal;
        _lastLine = lastLine;
    }

    @Override
    public void run() {
        _parser.parse(_splittedLine, _lastLine);
        _doneSignal.countDown();
    }
}
