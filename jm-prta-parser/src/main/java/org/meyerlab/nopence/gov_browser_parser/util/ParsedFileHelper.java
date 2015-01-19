package org.meyerlab.nopence.gov_browser_parser.util;

import com.google.common.base.Stopwatch;
import moa.recommender.rc.utils.Hash;
import org.meyerlab.nopence.utils.Helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Meyer
 */
public class ParsedFileHelper {

    private HashMap<String, File> _parsedFiles;
    private HashMap<String, List<String>> _buffer;

    private long _bufferSize;
    private long _bufferedInstances;

    public ParsedFileHelper() {
        _parsedFiles = new HashMap<>();
        _buffer = new HashMap<>();
        _bufferSize = GovOption.MaxBufferSize;
        _bufferedInstances = 0;
    }

    public void addInstance(String inst, String id) {
        if (_bufferedInstances < _bufferSize) {
            if(_buffer.containsKey(id)) {
                _buffer.get(id).add(inst);
            } else {
                ArrayList<String> history = new ArrayList<>();
                history.add(inst);

                _buffer.put(id, history);
            }
        }

        if (++_bufferedInstances >= _bufferSize) {
            emptyBuffer();
        }
    }


    public void emptyBuffer() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        System.out.println("Buffer will be emptied");

        try {
            for (String key : _buffer.keySet()) {
                if (!_parsedFiles.containsKey(key)) {
                    String filePath = GovOption.OutputDir
                            + File.separator + key + ".txt";

                    File newFile = new File(filePath);
                    Helper.createFile(newFile);

                    _parsedFiles.put(key, newFile);
                }

                FileWriter fileWriter =
                        new FileWriter(_parsedFiles.get(key), true);

                try {
                    StringBuffer instBuffer = new StringBuffer();
                    _buffer.get(key)
                            .forEach(inst -> {
                                instBuffer.append(inst);
                                instBuffer.append("\n");
                            });

                    fileWriter.write(instBuffer.toString());

                } finally {
                    fileWriter.flush();
                    fileWriter.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            _buffer.clear();
            _bufferedInstances = 0;
        }

        System.out.println("Buffer emptied");
        System.out.println("Time elapsed: "
                + stopwatch.stop().elapsed(TimeUnit.SECONDS));
    }
}
