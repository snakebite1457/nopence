package org.meyerlab.nopence.utils.exceptions;


import org.meyerlab.nopence.utils.Constants;

/**
 * @author Dennis Meyer
 */
public class FileNotValidException extends Exception {

    /**
     * @param filePath the filePath
     */
    public FileNotValidException(String filePath) {
        super(String.format(Constants.EX_FILE_NOT_VALID, filePath));
    }
}
