package org.meyerlab.nopence.jm_prta_parser.util.exceptions;

import org.meyerlab.nopence.jm_prta_parser.util.Constants;

import java.io.IOException;

/**
 * @author Dennis Meyer
 */
public class FileCanNotDeletedException extends IOException {

    public FileCanNotDeletedException(String dirPath) {
        super(String.format(Constants.EX_FILE_DEL, dirPath));
    }
}
