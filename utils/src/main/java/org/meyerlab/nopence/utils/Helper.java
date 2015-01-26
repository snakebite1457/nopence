package org.meyerlab.nopence.utils;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.meyerlab.nopence.utils.exceptions.FileCanNotCreatedException;
import org.meyerlab.nopence.utils.exceptions.FileCanNotDeletedException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Dennis Meyer
 */
public class Helper {

    private static final int intPrime = 179426549;
    private static final long longPrime = 32416190071l;

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isFileValid(File file) {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isDirValid(File dir) {
        return dir != null && dir.exists() && dir.isDirectory();
    }

    public static int countLines(File file)
            throws FileNotValidException, IOException {
        if (!Helper.isFileValid(file)) {
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


    public static boolean isNullOrEmpty(String str) {
        if (str == null || str.equals("")) {
            return true;
        }

        return false;
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
