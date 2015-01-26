package org.meyerlab.nopence.clustering.util;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import net.openhft.koloboke.collect.map.hash.HashIntIntMaps;
import org.meyerlab.nopence.clustering.DimensionInformation;
import org.meyerlab.nopence.clustering.DimensionType;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Dennis Meyer
 */
public class DataStream {

    private URL _resourceFile;
    private long _numInstances;
    private long _currentPosition;
    private BufferedReader _bufferedReader;
    private DimensionInformation _dimensionInformation;


    public DataStream(String resourceName)
            throws IOException {
        _resourceFile = DataStream.class.getResource("/" + resourceName);
        try {

            _numInstances = countLines(new File(_resourceFile.getFile()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        init();
    }

    public DataStream(String resourceName, int numInstances)
            throws IOException {
        _resourceFile = DataStream.class.getResource("/" + resourceName);
        _numInstances = numInstances;
        init();
    }

    private int countLines(File file) throws IOException {

        return Files.readLines(file,
                Charset.forName("UTF-8"),
                new LineProcessor<Integer>() {
                    int count = 0;

                    @Override
                    public boolean processLine(String s) throws IOException {
                        count++;
                        return true;
                    }

                    @Override
                    public Integer getResult() {
                        return count;
                    }
                });
    }

    private void init()
            throws IOException {
        // Read instances file and create automata input
        _bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(_resourceFile.getFile()),
                "UTF-8"));

        String[] header = _bufferedReader.readLine().split(",");

        Map<Integer, Integer> typeMapping = HashIntIntMaps.newMutableMap();
        IntStream.range(0, header.length)
                .forEachOrdered(a
                        -> typeMapping.put(a, DimensionType.NOMINAL.getNumVal()));

        _dimensionInformation = new DimensionInformation(typeMapping);
    }

    public DimensionInformation getDimInformation() {
        return _dimensionInformation;
    }

    public boolean hasNext() {
        return _currentPosition < _numInstances;
    }

    public Instance next() {
        try {
            String line = _bufferedReader.readLine();

            if (line == null) {
                return null;
            }

            _currentPosition++;

            Map<Integer, Double> values = new HashMap<>();
            String[] splittedLine = line.split(",");
            IntStream.range(0, splittedLine.length)
                    .forEachOrdered(a
                            -> values.put(a, Double.valueOf(splittedLine[a])));

            return new Instance(values);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void dispose() {
        try {
            _bufferedReader.close();
            _dimensionInformation = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
