package org.meyerlab.nopence.jm_prta_parser.util.exceptions;

import org.meyerlab.nopence.jm_prta_parser.util.Constants;

/**
 * @author Dennis Meyer
 */
public class DirNotValidException extends Exception {

    /**
     * @param dirPath the filePath
     */
    public DirNotValidException(String dirPath) {
        super(String.format(Constants.EX_DIR_NOT_VALID, dirPath));
    }

}
