package org.meyerlab.nopence.attr_converter_prta.util;

import com.google.common.io.Files;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class PrtaParserHelper {

    public static boolean containsDirAttrInfoFiles(File dir) {
        if (!Helper.isDirValid(dir)) {
            return false;
        }

        List<File> dirContent = Arrays.asList(dir.listFiles());

        return dirContent
                .stream()
                .filter(file ->
                        Files.getFileExtension(file.getAbsolutePath()).equals
                                (Constants.FILE_EXTENSION_XML) &&
                                Files.getNameWithoutExtension(file.getAbsolutePath())
                                        .startsWith(PrtaParserConstants.ATTR_INFO_FILE_PREFIX))
                .count() > 0;
    }

    public static List<File> getAllAttrInfoFiles(File dir) {
        if (!containsDirAttrInfoFiles(dir)) {
            return new ArrayList<>();
        }

        List<File> dirContent = Arrays.asList(dir.listFiles());
        return dirContent
                .stream()
                .filter(file ->
                        Files.getFileExtension(file.getAbsolutePath()).equals
                                (Constants.FILE_EXTENSION_XML) &&
                                Files.getNameWithoutExtension(file.getAbsolutePath())
                                        .startsWith(PrtaParserConstants.ATTR_INFO_FILE_PREFIX))
                .collect(Collectors.toList());
    }

    public static String generateOutputFileName(File attrInfoFile, int maxSeq) {
        String attrFileName = Files.getNameWithoutExtension(
                attrInfoFile.getAbsolutePath());

        String maxSeqString = maxSeq != Integer.MAX_VALUE
                ? String.valueOf(maxSeq) : "INF";

        StringBuilder outFileNameBuilder = new StringBuilder()
                .append("JM-Data-")
                .append(attrFileName.replace(PrtaParserConstants.ATTR_INFO_FILE_PREFIX, ""))
                .append("-S")
                .append(maxSeqString)
                .append(".txt");

        return outFileNameBuilder.toString();

    }

    public static String generateVarHistFileName(File attrInfoFile) {
        String attrFileName = Files.getNameWithoutExtension(
                attrInfoFile.getAbsolutePath());

        StringBuilder outFileNameBuilder = new StringBuilder()
                .append("Variables-Histogramm-")
                .append(attrFileName.replace(PrtaParserConstants.ATTR_INFO_FILE_PREFIX, ""))
                .append(".xml");

        return outFileNameBuilder.toString();
    }

}
