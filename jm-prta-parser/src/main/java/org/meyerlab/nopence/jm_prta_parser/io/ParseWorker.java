package org.meyerlab.nopence.jm_prta_parser.io;

import java.util.concurrent.CountDownLatch;

/**
 * @author Dennis Meyer
 */
public class ParseWorker implements Runnable {

    private CountDownLatch _doneSignal;
    private final DataParser _parser;
    private String[] _splittedLine;

    public ParseWorker(DataParser parser) {
        _parser = parser;
    }

    public void reset(CountDownLatch doneSignal, String[] splittedLine) {
        _splittedLine = splittedLine;
        _doneSignal = doneSignal;
    }

    @Override
    public void run() {
        _parser.parse(_splittedLine);
        _doneSignal.countDown();
    }
}
