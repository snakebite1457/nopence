package org.meyerlab.nopence.attr_converter_prta.util;

import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;
import org.meyerlab.nopence.utils.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Singleton class. Stores general information about the paths.
 *
 * @author Dennis Meyer
 */
public class Option {

    private static Option _instance;

    private static String _pathAttrInfoDir;
    private static String _pathDataFile;
    private static String _pathVarHistDir;
    private static ArrayList<Integer> _maxSeqList;
    private static String _pathOutputDir;
    private static int _instancesFileLineCount;

    private static int _colPersonId;
    private static int _colObservationDate;
    private static String _dateFormat;

    private static boolean debugMode = true;

    private Option() { }

    public static void init(String pathInfoDir,
                            String pathDataFile,
                            String pathVarHistDir,
                            String pathOutputDir,
                            List<Integer> maxSeqList)
            throws FileNotValidException, DirNotValidException, IOException {

        if (!Helper.isFileValid(new File(pathDataFile))) {
            throw new FileNotValidException(pathDataFile);
        }

        if (!Helper.isFileValid(new File(pathDataFile))) {
            throw new DirNotValidException(pathInfoDir);
        }

        if (!Helper.isDirValid(new File(pathVarHistDir))) {
            throw new DirNotValidException(pathVarHistDir);
        }

        if (!Helper.isDirValid(new File(pathOutputDir))) {
            throw new DirNotValidException(pathOutputDir);
        }

        if (!PrtaParserHelper.containsDirAttrInfoFiles(new File(pathInfoDir))) {
            return;
        }

        _pathAttrInfoDir = pathInfoDir;
        _pathDataFile = pathDataFile;
        _pathVarHistDir = pathVarHistDir;
        _pathOutputDir = pathOutputDir;
        _maxSeqList = new ArrayList<>(maxSeqList);

        _instancesFileLineCount = Helper.countLines(new File(_pathDataFile));

        readConfigFile();
    }

    public static Option getInstance() {
        if (_instance == null) {
            _instance = new Option();
        }

        return _instance;
    }

    private static void readConfigFile() throws IOException {
        String currentLocation = new File(Option.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath()).getParent();

        Properties properties = new Properties();
        properties.loadFromXML(debugMode
                ? Option.class.getResourceAsStream("/attr_converter_prta_properties.xml")
                : new FileInputStream(currentLocation +
                File.separator + "attr_converter_prta_properties.xml"));

        _colPersonId = Integer.parseInt(properties.getProperty("columnPersonId"));
        _colObservationDate = Integer.parseInt(properties.getProperty("columnObservationDate"));
        _dateFormat = properties.getProperty("dateFormat");
    }

    public String getDateFormat() {
        return _dateFormat;
    }

    public int getColPersonId() {
        return _colPersonId;
    }

    public int getColObservationDate() {
        return _colObservationDate;
    }

    public String getPathAttrInfoDir() {
        return _pathAttrInfoDir;
    }

    public String getPathDataFile() {
        return _pathDataFile;
    }

    public String getPathVarHistDir() {
        return _pathVarHistDir;
    }

    public String getPathOutputDir() {
        return _pathOutputDir;
    }

    public List<Integer> getMaxSeqList() {
        return new ArrayList<>(_maxSeqList);
    }

    public boolean isMaxSeqListEmpty() {
        return _maxSeqList.isEmpty();
    }

    public int getInstFileLineCount() {
        return _instancesFileLineCount;
    }
}
