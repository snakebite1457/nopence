package org.meyerlab.nopence.util;

import com.google.common.base.Stopwatch;
import org.meyerlab.nopence.gov_browser_parser.util.GovOption;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Meyer
 */
public class CsvFileWriteHelper {

    private List<String> _listBuffer;

    private long _bufferSize;
    private long _bufferedInstances;
    private File _outputFile;

    public CsvFileWriteHelper(long bufferSize, String outputFile) {
        _listBuffer = new ArrayList<>();
        _bufferSize = bufferSize;
        _outputFile = new File(outputFile);
        _bufferedInstances = 0;
    }

    public void addInstance(String inst) {
        if (_bufferedInstances < _bufferSize) {
            _listBuffer.add(inst);
        }

        if (++_bufferedInstances >= _bufferSize) {
            emptyBuffer();
        }
    }

    public void emptyBuffer() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        System.out.println("Buffer will be emptied");

        try {
            FileWriter fileWriter =
                    new FileWriter(_outputFile, true);

            try {
                StringBuffer instBuffer = new StringBuffer();
                _listBuffer
                        .forEach(inst -> {
                            instBuffer.append(inst);
                            instBuffer.append("\n");
                        });

                fileWriter.write(instBuffer.toString());

            } finally {
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            _listBuffer.clear();
            _listBuffer = null;
            _listBuffer = new ArrayList<>();
            _bufferedInstances = 0;
        }

        System.out.println("Buffer emptied");
        System.out.println("Time elapsed: "
                + stopwatch.stop().elapsed(TimeUnit.SECONDS));
    }

    public void addHeader(String header) {
        _listBuffer.add(0, header);
    }
}
