package org.meyerlab.nopence.gov_browser_parser.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.meyerlab.nopence.utils.Helper;

/**
 * @author Dennis Meyer
 */
public class JsonGovParser {

    public static String getId(JsonObject govObj) {
        if (idAvailable(govObj)) {
            return parseId(govObj);
        }

        return null;
    }

    private static boolean idAvailable(JsonObject govObj) {
        if (!govObj.has(GovBrowserConstants.JSON_GOV_LOCATION)) {
            return false;
        }

        if (!govObj.has("h")) {
            return false;
        }

        if (!govObj.has("l")) {
            return false;
        }

        return !Helper.isNullOrEmpty(govObj.get(GovBrowserConstants
                .JSON_GOV_LOCATION).toString());
    }

    private static String parseId(JsonObject govObj) {
        JsonArray locationArray = govObj
                .get(GovBrowserConstants.JSON_GOV_LOCATION).getAsJsonArray();

        return govObj.get("h").getAsString();
    }
}
