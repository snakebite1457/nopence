package org.meyerlab.nopence.jm_prta_parser;

import com.google.common.base.Stopwatch;
import org.apache.commons.cli.*;
import org.meyerlab.nopence.jm_prta_parser.io.DataParser;
import org.meyerlab.nopence.jm_prta_parser.io.ParseWorker;
import org.meyerlab.nopence.jm_prta_parser.io.TypeConverter;
import org.meyerlab.nopence.jm_prta_parser.util.PrtaParserConstants;
import org.meyerlab.nopence.jm_prta_parser.util.PrtaParserHelper;
import org.meyerlab.nopence.jm_prta_parser.util.Option;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Meyer
 */
public class Run {

    private static ArrayList<DataParser> dataParserList;
    private static boolean useThreading;

    public static void main(String[] args) {

        System.out.println("Converter process has been started!\n");
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            // Read command lines
            readCLI(args);

            List<File> attrInfoFiles = PrtaParserHelper.getAllAttrInfoFiles(new File(
                    Option.getInstance().getPathAttrInfoDir()));

            generateDataParser(attrInfoFiles);


            // Write the variables histogram files
            System.out.println("Begin writing the " +
                    "variables histogramm files \n");

            for (DataParser dataParser : dataParserList) {
                dataParser.writeVarHistFile();
            }

            if (useThreading) {
                parseDataThreaded();
            } else {
                parseData();
            }

        } catch (SAXException |
                IOException |
                FileNotValidException |
                ParseException |
                DirNotValidException |
                InterruptedException e) {

            e.printStackTrace();
        }

        stopwatch.stop();
        System.out.println("Time elapsed: " +
                stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
    }

    private static void parseData() throws IOException {
        // Read instances file and create automata input
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(Option.getInstance().getPathDataFile()),
                Constants.FILE_ENCODING));

        // Skip header
        String line = br.readLine();

        System.out.println("Begin parse operation");
        int processCounter = 1; // Header
        int overallInstances = Option.getInstance().getInstFileLineCount();
        while ((line = br.readLine()) != null) {
            String[] splittedLine = line.split(Constants.FILE_CSV_SEPARATION);

            for (DataParser dataParser : dataParserList) {
                dataParser.parse(splittedLine, overallInstances == ++processCounter);
            }
            Helper.printConsoleProgressBar(Helper.calcPercentage
                    (overallInstances, processCounter));
        }
        System.out.println("\nEnd Parse parse operation");
    }

    private static void parseDataThreaded()
            throws IOException, InterruptedException {
        // Read instances file and create automata input
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(Option.getInstance().getPathDataFile()),
                Constants.FILE_ENCODING));

        // Skip header
        String line = br.readLine();

        int parserSize = dataParserList.size();
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Create the parser workers once and reset it for every line
        ArrayList<ParseWorker> parseWorkers = new ArrayList<>(parserSize);
        dataParserList.forEach(parser -> parseWorkers.add(new ParseWorker(parser)));


        System.out.println("Begin threaded parse operation");
        int processCounter = 1; // Header
        int overallInstances = Option.getInstance().getInstFileLineCount();
        while ((line = br.readLine()) != null) {
            String[] splittedLine = line.split(Constants.FILE_CSV_SEPARATION);
            CountDownLatch doneSignal = new CountDownLatch(parserSize);

            processCounter++;
            for (ParseWorker worker : parseWorkers) {
                worker.reset(doneSignal,
                        splittedLine.clone(), overallInstances == processCounter);
                executorService.submit(worker);
            }

            doneSignal.await();

            Helper.printConsoleProgressBar(Helper.calcPercentage
                    (overallInstances, processCounter));
        }
        executorService.shutdown();
        System.out.println("\nEnd Parse threaded parse operation");
    }

    private static void generateDataParser(List<File> attrInfoFiles)
            throws IOException, SAXException {

        for (File attrInfoFile : attrInfoFiles) {
            TypeConverter converter = new TypeConverter(attrInfoFile);
            converter.buildConverter();

            String varHistFileName = PrtaParserHelper.generateVarHistFileName
                    (attrInfoFile);
            File varHistFile = new File(Option.getInstance()
                    .getPathVarHistDir(), varHistFileName);

            dataParserList.add(new DataParser(attrInfoFile, varHistFile));
        }

        if (dataParserList.size() > 1) {
            useThreading = true;
        }
    }

    private static void readCLI(String[] args)
            throws ParseException,
            DirNotValidException,
            FileNotValidException, IOException {

        Options options = new Options();
        dataParserList = new ArrayList<>();

        options.addOption("d", "dataFile", true, PrtaParserConstants.CLI_DES_DATA_FILE);
        options.addOption("a", "infoDir", true, PrtaParserConstants.CLI_DES_ATTR_INFO_DIR);
        options.addOption(OptionBuilder
                .withLongOpt("numSeq")
                .hasArgs()
                .withDescription(PrtaParserConstants.CLI_DES_NUM_SEQ)
                .create("s"));
        options.addOption("o", "outDir", true, PrtaParserConstants.CLI_DES_OUT_DIR);
        options.addOption("v", "varHistDir", true, PrtaParserConstants.CLI_DES_VAR_HIST_DIR);

        // create the command line parser
        CommandLineParser parser = new BasicParser();

        CommandLine line = parser.parse(options, args);

        String dataFilePath = "", attrInfoDirPath = "", outputDirPath = "", varHistDirPath = "";
        List<Integer> maxSeqList = new ArrayList<>();

        if (line.hasOption("dataFile")) {
            dataFilePath = line.getOptionValue("dataFile");
        }

        if (line.hasOption("infoDir")) {
            attrInfoDirPath = line.getOptionValue("infoDir");
        }

        if (line.hasOption("outDir")) {
            outputDirPath = line.getOptionValue("outDir");
        }

        if (line.hasOption("varHistDir")) {
            varHistDirPath = line.getOptionValue("varHistDir");
        }

        if (line.hasOption("numSeq")) {
            Arrays.asList(line.getOptionValues("numSeq")).forEach(maxSeq
                    -> maxSeqList.add(Integer.valueOf(maxSeq)));
        } else {
            maxSeqList.add(Integer.MAX_VALUE);
        }

        Option.init(attrInfoDirPath, dataFilePath,
                varHistDirPath , outputDirPath, maxSeqList);
    }
}
