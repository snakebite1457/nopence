package org.meyerlab.nopence.gov_browser_parser.util;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

/**
 * @author Dennis Meyer
 */
public class BrowserAgentParser {

    private static UserAgentStringParser _userAgentStringParser =
            UADetectorServiceFactory.getResourceModuleParser();

    public static ReadableUserAgent parse(String browserAgent) {
        return _userAgentStringParser.parse(browserAgent);
    }
}
