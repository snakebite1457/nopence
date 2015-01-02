package org.meyerlab.nopence.jm_prta_parser.util;

import org.meyerlab.nopence.jm_prta_parser.util.exceptions.DirNotValidException;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.FileNotValidException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private Option() {
    }

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

        if (!Helper.containsDirAttrInfoFiles(new File(pathInfoDir))) {
            return;
        }

        _pathAttrInfoDir = pathInfoDir;
        _pathDataFile = pathDataFile;
        _pathVarHistDir = pathVarHistDir;
        _pathOutputDir = pathOutputDir;
        _maxSeqList = new ArrayList<>(maxSeqList);

        _instancesFileLineCount = Helper.countLines(new File(_pathDataFile));
    }

    public static Option getInstance() {
        if (_instance == null) {
            _instance = new Option();
        }

        return _instance;
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
