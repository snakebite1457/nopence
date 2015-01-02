package org.meyerlab.nopence.jm_prta_parser.util;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.FileCanNotCreatedException;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.FileCanNotDeletedException;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.FileNotValidException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class Helper {

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isFileValid(File file) {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isDirValid(File dir) {
        return dir != null && dir.exists() && dir.isDirectory();
    }

    public static boolean containsDirAttrInfoFiles(File dir) {
        if (!isDirValid(dir)) {
            return false;
        }

        List<File> dirContent = Arrays.asList(dir.listFiles());

        return dirContent
                .stream()
                .filter(file ->
                        Files.getFileExtension(file.getAbsolutePath()).equals
                                (Constants.FILE_EXTENSION_XML) &&
                                Files.getNameWithoutExtension(file.getAbsolutePath())
                                        .startsWith(Constants.ATTR_INFO_FILE_PREFIX))
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
                                        .startsWith(Constants.ATTR_INFO_FILE_PREFIX))
                .collect(Collectors.toList());
    }

    public static String generateOutputFileName(File attrInfoFile, int maxSeq) {
        String attrFileName = Files.getNameWithoutExtension(
                attrInfoFile.getAbsolutePath());

        String maxSeqString = maxSeq != Integer.MAX_VALUE
                ? String.valueOf(maxSeq) : "INF";

        StringBuilder outFileNameBuilder = new StringBuilder()
                .append("JM-Data-")
                .append(attrFileName.replace(Constants.ATTR_INFO_FILE_PREFIX, ""))
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
                .append(attrFileName.replace(Constants.ATTR_INFO_FILE_PREFIX, ""))
                .append(".xml");

        return outFileNameBuilder.toString();
    }

    public static void createFile(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new FileCanNotDeletedException(file.getAbsolutePath());
            }
        }

        if (!file.createNewFile()) {
            throw new FileCanNotCreatedException(file.getAbsolutePath());
        }
    }

    public static int countLines(File file)
            throws FileNotValidException, IOException {
        if (!isFileValid(file)) {
            throw new FileNotValidException(file.getAbsolutePath());
        }

        return  Files.readLines(file,
                Charset.forName(Constants.FILE_ENCODING),
                new LineProcessor<Integer>() {
                    int count = 0;

                    @Override
                    public boolean processLine(String s) throws IOException {
                        count++;
                        return true;
                    }

                    @Override
                    public Integer getResult() {
                        return count;
                    }
                });
    }

    public static int calcPercentage(double max, double value) {
        return (int)Math.ceil((value * 100f) / max);
    }

    public static void printConsoleProgressBar(int percent){
        StringBuilder bar = new StringBuilder("[");

        for(int i = 0; i < 50; i++){
            if( i < (percent/2)){
                bar.append("=");
            }else if( i == (percent/2)){
                bar.append(">");
            }else{
                bar.append(" ");
            }
        }

        bar.append("]   ").append(percent).append("%");
        System.out.print("\r" + bar.toString());
    }
}
