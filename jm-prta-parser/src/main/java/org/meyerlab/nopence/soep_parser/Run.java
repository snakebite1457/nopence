package org.meyerlab.nopence.soep_parser;

import org.apache.commons.cli.*;
import org.meyerlab.nopence.soep_parser.util.SoepOption;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.IOException;

/**
 * Parser for the German Socio-Economic Panel (SOEP) data. Makes it possible
 * to find and combine all observations for one person from different files
 * into one file. The observations are now ordered by the person/object id.
 *
 * There are different command line parameters required.
 *
 * The option 'i': The directory, that contains the observation files.
 *
 * The option 'f': The output file (csv-format).
 *
 * The option 'p': All the observation file must have the same postfix. This
 * postfix must be specified here.
 *
 * The option 'm': For the SOEP data more information is required. Look at
 * the file 'meta-pgen'. If not needed feel free to change it.
 *
 *
 * @author Dennis Meyer
 */
public class Run {

    public static void main(String[] args) {

        System.out.println("Parser process has been started!\n");

        try {
            readCLI(args);
        } catch (ParseException | DirNotValidException | FileNotValidException | IOException e) {
            e.printStackTrace();
        }

        try {
            Parser.parse();
        } catch (DirNotValidException | FileNotValidException e) {
            e.printStackTrace();
        } catch (IOException ignored) { }
    }


    private static void readCLI(String[] args)
            throws ParseException,
            DirNotValidException,
            FileNotValidException, IOException {

        Options options = new Options();

        options.addOption("i", "inputDir", true, "Input Directory");
        options.addOption("f", "outputFile", true, "Output CSV File");
        options.addOption("p", "filePostfix", true, "File Postfix");
        options.addOption("m", "metaInputFile", true, "Meta Input File");


        // create the command line parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("inputDir")) {
            SoepOption.RawDataDirectory = line.getOptionValue("inputDir");
        }

        if (line.hasOption("outputFile")) {
            SoepOption.OutputFile = line.getOptionValue("outputFile");
        }

        if (line.hasOption("filePostfix")) {
            SoepOption.FilePostfix = line.getOptionValue("filePostfix");
        }

        if (line.hasOption("metaInputFile")) {
            SoepOption.MetaInputFile = line.getOptionValue("metaInputFile");
        }
    }
}
