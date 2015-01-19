package org.meyerlab.nopence.utils.exceptions;


import org.meyerlab.nopence.utils.Constants;

import java.io.IOException;

/**
 * @author Dennis Meyer
 */
public class FileCanNotDeletedException extends IOException {

    public FileCanNotDeletedException(String dirPath) {
        super(String.format(Constants.EX_FILE_DEL, dirPath));
    }
}
