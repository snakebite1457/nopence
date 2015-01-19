package org.meyerlab.nopence.gov_browser_parser.util;


import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.*;

/**
 * @author Dennis Meyer
 */
public class FileHelper {

    private File _currentFile;
    private BufferedReader _bufferedReader;
    private boolean _notEmptyLinesOnly;

    private int _numberOfLines;
    private int _alreadyReadLines;

    public FileHelper() {}

    public FileHelper(File currentFile, boolean notEmptyLinesOnly)
            throws IOException, FileNotValidException {

        init(currentFile, notEmptyLinesOnly);
    }

    public void init(File currentFile, boolean notEmptyLinesOnly)
            throws FileNotValidException, IOException {
        if (!Helper.isFileValid(currentFile)) {
            throw new FileNotValidException(Constants.EX_FILE_NOT_VALID);
        }
        _currentFile = currentFile;
        _numberOfLines = Helper.countLines(_currentFile);

        _notEmptyLinesOnly = notEmptyLinesOnly;

        if (_bufferedReader != null) {
            _bufferedReader.close();
        }
        _bufferedReader = new BufferedReader(
                new FileReader(_currentFile.getAbsoluteFile()));
    }

    public boolean hasNextLine() {
        return getNumberOfLines() > _alreadyReadLines;
    }

    public String nextLine() throws IOException {
        _alreadyReadLines++;

        if (_notEmptyLinesOnly) {
            String line = _bufferedReader.readLine();
            while (Helper.isNullOrEmpty(line) && hasNextLine()) {
                line = _bufferedReader.readLine();
                _alreadyReadLines++;
            }

            return line;
        } else {
            return _bufferedReader.readLine();
        }
    }

    public void reset(File currentFile, boolean notEmptyLinesOnly)
            throws IOException, FileNotValidException {
        _alreadyReadLines = 0;

        init(currentFile, notEmptyLinesOnly);
    }

    public int getNumberOfLines() {
        return _numberOfLines;
    }

}
