package org.meyerlab.nopence.prta_d3_parser;

import org.apache.commons.cli.*;
import org.meyerlab.nopence.prta_d3_parser.Model.D3ParserOption;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author Dennis Meyer
 */
public class Run {

    public static void main(String[] args) {

        try {
            readCLI(args);
        } catch (ParseException | DirNotValidException | FileNotValidException | IOException e) {
            e.printStackTrace();
        }
        Parser parser = new Parser();
        try {
            parser.parse();
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }
    }


    private static void readCLI(String[] args)
            throws ParseException,
            DirNotValidException,
            FileNotValidException, IOException {

        Options options = new Options();

        options.addOption("v", "varHist", true, "");
        options.addOption("a", "automata", true, "");
        options.addOption("o", "outputFile", true, "");


        // create the command line parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("varHist")) {
            D3ParserOption.VarHistogrammFilePath = line.getOptionValue("varHist");
        }

        if (line.hasOption("automata")) {
            D3ParserOption.AutomataFilePath = line.getOptionValue("automata");
        }

        if (line.hasOption("outputFile")) {
            D3ParserOption.OutJsonAutomataFilePath = line.getOptionValue("outputFile");
        }
    }
}
