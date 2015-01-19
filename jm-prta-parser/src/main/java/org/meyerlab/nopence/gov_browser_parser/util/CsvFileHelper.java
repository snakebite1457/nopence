package org.meyerlab.nopence.gov_browser_parser.util;

import com.google.common.base.Stopwatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Meyer
 */
public class CsvFileHelper {

    private List<String> _listBuffer;

    private long _bufferSize;
    private long _bufferedInstances;
    private File _outputFile;

    public CsvFileHelper() {
        _listBuffer = new ArrayList<>();
        _bufferSize = GovOption.MaxBufferSize;
        _outputFile = new File(GovOption.OutputFile);
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
