package org.meyerlab.nopence.jm_prta_parser.util.exceptions;


import org.meyerlab.nopence.jm_prta_parser.util.Constants;

import java.io.IOException;

/**
 * @author Dennis Meyer
 */
public class FileCanNotCreatedException extends IOException {

    public FileCanNotCreatedException(String filePath) {
        super(String.format(Constants.EX_FILE_CREATE, filePath));
    }
}
