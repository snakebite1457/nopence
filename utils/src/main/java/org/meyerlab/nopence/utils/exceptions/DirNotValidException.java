package org.meyerlab.nopence.utils.exceptions;


import org.meyerlab.nopence.utils.Constants;

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
