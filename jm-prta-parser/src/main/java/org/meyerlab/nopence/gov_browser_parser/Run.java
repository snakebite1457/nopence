package org.meyerlab.nopence.gov_browser_parser;

import org.apache.commons.cli.*;
import org.meyerlab.nopence.gov_browser_parser.util.*;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.IOException;

/**
 * @author Dennis Meyer
 */
public class Run {

    public static void main(String[] argv) {

        try {
            readCLI(argv);

            WriteToDB.write();

            /*if (!Helper.isNullOrEmpty(GovOption.OutputFile)) {
                if (Helper.isFileValid(new File(GovOption.OutputFile))) {
                    Helper.createFile(new File(GovOption.OutputFile));
                }

                ParseCsv.parse();
                return;
            }

            if (Helper.isNullOrEmpty(GovOption.InputDir)
                    || Helper.isNullOrEmpty(GovOption.OutputDir)) {
                System.out.println("Please give input and output dir!");
                return;
            }

            ParseRaw parseRaw = new ParseRaw();

            parseRaw.startParser();*/
        } catch (DirNotValidException |
                FileNotValidException |
                IOException |
                ParseException e) {
            e.printStackTrace();
        }
    }

    private static void readCLI(String[] args)
            throws ParseException,
            DirNotValidException,
            FileNotValidException, IOException {

        Options options = new Options();

        options.addOption("i", "inputDir", true, "Input Directory");
        options.addOption("o", "outputDir", true, "Output Directory");
        options.addOption("f", "outputFile", true, "Output CSV File");
        options.addOption("s", "maxBufferSize", true, "Max Buffer Size");

        // create the command line parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("inputDir")) {
            GovOption.InputDir = line.getOptionValue("inputDir");
        }

        if (line.hasOption("outputDir")) {
            GovOption.OutputDir = line.getOptionValue("outputDir");
        }

        if (line.hasOption("outputFile")) {
            GovOption.OutputFile = line.getOptionValue("outputFile");
        }

        if (line.hasOption("maxBufferSize")) {
            GovOption.MaxBufferSize = Long.parseLong(line.getOptionValue
                    ("maxBufferSize"));
        }
    }
}
