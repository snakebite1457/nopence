package org.meyerlab.nopence.utils.exceptions;




import org.meyerlab.nopence.utils.Constants;

import java.io.IOException;

/**
 * @author Dennis Meyer
 */
public class FileCanNotCreatedException extends IOException {

    public FileCanNotCreatedException(String filePath) {
        super(String.format(Constants.EX_FILE_CREATE, filePath));
    }
}
