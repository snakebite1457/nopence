package org.meyerlab.nopence.gov_browser_parser;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.meyerlab.nopence.gov_browser_parser.util.*;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author Dennis Meyer
 */
public class ParseRaw {

    private JsonParser _jsonParser;

    public void startParser()
            throws FileNotFoundException, DirNotValidException {

        _jsonParser = new JsonParser();

        File dir = new File(GovOption.InputDir);

        if (!Helper.isDirValid(dir)) {
            throw new DirNotValidException(String.format(Constants
                    .EX_DIR_NOT_VALID, GovOption.InputDir));
        }

        FileHelper fileHelper = new FileHelper();
        ParsedFileHelper parsedFileHelper = new ParsedFileHelper();

        try {
            for (File dataFile : dir.listFiles()) {
                fileHelper.reset(dataFile, true);

                String line = fileHelper.nextLine();
                while (line != null) {
                    try {
                        if (_jsonParser.parse(line).isJsonObject()) {
                            JsonObject jsonGovObj =
                                    _jsonParser.parse(line).getAsJsonObject();

                            String id = JsonGovParser.getId(jsonGovObj);
                            if (!Helper.isNullOrEmpty(id)) {
                                parsedFileHelper.addInstance(line, id);
                            }
                        }
                    } catch (JsonSyntaxException ex) { }

                    if (!fileHelper.hasNextLine()) {
                        break;
                    }

                    line = fileHelper.nextLine();
                }
            }
        } catch (FileNotValidException | IOException e) {
            e.printStackTrace();
        } finally {
            parsedFileHelper.emptyBuffer();
        }

        System.out.print("ready");
    }
}
