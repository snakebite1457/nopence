package org.meyerlab.nopence.jm_prta_parser.util.exceptions;

import org.meyerlab.nopence.jm_prta_parser.util.Constants;

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
