package org.meyerlab.nopence.gov_browser_parser;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableUserAgent;
import org.meyerlab.nopence.gov_browser_parser.util.*;
import org.meyerlab.nopence.util.CsvFileWriteHelper;
import org.meyerlab.nopence.util.FileHelper;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Dennis Meyer
 */
public class ParseCsv {

    private static JsonParser _jsonParser;

    public static void parse() throws DirNotValidException {
        _jsonParser = new JsonParser();

        File dir = new File(GovOption.InputDir);

        if (!Helper.isDirValid(dir)) {
            throw new DirNotValidException(String.format(Constants
                    .EX_DIR_NOT_VALID, GovOption.InputDir));
        }

        FileHelper fileHelper = new FileHelper();
        CsvFileWriteHelper csvFileWriteHelper = new CsvFileWriteHelper(GovOption
                .MaxBufferSize, GovOption.OutputFile);
        csvFileWriteHelper.addHeader(createOsHeader());

        try {
            for (File dataFile : dir.listFiles()) {
                fileHelper.reset(dataFile, true);

                String line = fileHelper.nextLine();
                TreeMap<Long, JsonObject> container = new TreeMap<>();

                while (line != null) {
                    try {
                        if (_jsonParser.parse(line).isJsonObject()) {
                            JsonObject jsonGovObj =
                                    _jsonParser.parse(line).getAsJsonObject();

                            if (!jsonGovObj.has("t") || !jsonGovObj.has("a")) {
                                continue;
                            }

                            long timestamp = jsonGovObj.get("t").getAsLong();
                            container.put(timestamp, jsonGovObj);
                        }
                    } catch (JsonSyntaxException ex) { }

                    if (!fileHelper.hasNextLine()) {
                        break;
                    }

                    line = fileHelper.nextLine();
                }

                StringBuffer csvOsHistory = new StringBuffer();

                long firstTimestamp = 0;
                for (Long timestamp : container.keySet()) {
                    if (firstTimestamp == 0) {
                        String inst = parseOsJsonObj(container.get(timestamp), 0);

                        if (Helper.isNullOrEmpty(inst)) {
                            continue;
                        }

                        csvOsHistory.append(inst);
                        firstTimestamp = container.get(timestamp).get("t").getAsLong();

                        continue;
                    }

                    long hoursDif = (container.get(timestamp)
                            .get("t").getAsLong() - firstTimestamp) / 3600;

                    String inst = parseOsJsonObj(container.get(timestamp), hoursDif);

                    if (Helper.isNullOrEmpty(inst)) {
                        continue;
                    }

                    csvOsHistory.append(inst);
                }

                csvFileWriteHelper.addInstance(csvOsHistory.toString());
            }
        } catch (FileNotValidException | IOException e) {
            e.printStackTrace();
        } finally {
            csvFileWriteHelper.emptyBuffer();
        }
    }

    private static String parseOsJsonObj(JsonObject govJsonObj, long timeSpan) {
        ReadableUserAgent userAgent = BrowserAgentParser.parse(
                govJsonObj.get("a").toString());

        OperatingSystem operatingSystem = userAgent.getOperatingSystem();

        if (operatingSystem.getFamily() == OperatingSystemFamily.UNKNOWN
                || Helper.isNullOrEmpty(operatingSystem.getFamilyName())
                || Helper.isNullOrEmpty(operatingSystem.getVersionNumber()
                .toVersionString())) {
            return null;
        }

        StringBuilder instanceBuilder = new StringBuilder();
        instanceBuilder.append(JsonGovParser.getId(govJsonObj)); // Add Id
        instanceBuilder.append(";");
        instanceBuilder.append(timeSpan); // Add timeSpan since firs observation
        instanceBuilder.append(";");
        instanceBuilder.append(userAgent.getOperatingSystem().getFamilyName());
        instanceBuilder.append(";");
        instanceBuilder.append(userAgent.getOperatingSystem().getName());
        instanceBuilder.append(";");
        instanceBuilder.append(userAgent.getOperatingSystem().getVersionNumber().toVersionString());
        instanceBuilder.append("\n");

        return instanceBuilder.toString();
    }

    private static String createOsHeader() {
        StringBuilder osHeaderBuilder = new StringBuilder();
        osHeaderBuilder.append("Id");
        osHeaderBuilder.append(";");
        osHeaderBuilder.append("Timespan");
        osHeaderBuilder.append(";");
        osHeaderBuilder.append("OsFamily");
        osHeaderBuilder.append(";");
        osHeaderBuilder.append("OsName");
        osHeaderBuilder.append(";");
        osHeaderBuilder.append("OsVersion");
        osHeaderBuilder.append("\n");

        return osHeaderBuilder.toString();

    }
}
