package org.meyerlab.nopence.soep_parser;

import com.google.common.io.Files;
import com.google.gson.*;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;
import org.meyerlab.nopence.soep_parser.util.AttributeMap;
import org.meyerlab.nopence.soep_parser.util.AttributeType;
import org.meyerlab.nopence.soep_parser.util.SoepOption;
import org.meyerlab.nopence.soep_parser.util.model.Attribute;
import org.meyerlab.nopence.soep_parser.util.model.Instance;
import org.meyerlab.nopence.util.CsvFileHelper;
import org.meyerlab.nopence.util.FileHelper;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.DirNotValidException;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Dennis Meyer
 */
public class Parser {

    private static JsonParser _jsonParser;

    // key -> personIdentifier, value -> ordered list of observations
    private static Map<Integer, List<Instance>> _histories;

    private static AttributeMap _metaAttributeMap;
    private static String _identifierVarName;
    private static Attribute _monthObservationAttribute;


    public static void parse() throws DirNotValidException, FileNotValidException, IOException {
        _jsonParser = new JsonParser();
        _metaAttributeMap = new AttributeMap();
        _histories = HashIntObjMaps.newMutableMap();

        if (!Helper.isFileValid(new File(SoepOption.MetaInputFile))) {
            throw new FileNotValidException(String.format(Constants
                    .EX_DIR_NOT_VALID, SoepOption.MetaInputFile));
        }

        Helper.createFile(new File(SoepOption.OutputFile));

        if (!Helper.isDirValid(new File(SoepOption.RawDataDirectory))) {
            throw new DirNotValidException(String.format(Constants
                    .EX_DIR_NOT_VALID, SoepOption.RawDataDirectory));
        }

        generateMetaAttrMap();

        File rawSoepInputDirectory = new File(SoepOption.RawDataDirectory);

        FileHelper fileHelper = new FileHelper();
        try {
            for (File dataFile : rawSoepInputDirectory.listFiles()) {
                fileHelper.reset(dataFile, true);

                // Read header and build an mapping between global metaMap
                // and header attributes
                String line = fileHelper.nextLine();

                Map<Integer, Integer> fileAttributeMapping = new HashMap<>();
                List<String> header = Arrays.asList(line.toUpperCase().split(","));

                String currentMonthAttrName = _monthObservationAttribute
                        .generateFullName(Files.getNameWithoutExtension
                                (dataFile.getName()));

                if (!header.contains(currentMonthAttrName)) {
                    System.out.println("Error");
                }

                int monthIndex = header.indexOf(currentMonthAttrName);

                _metaAttributeMap
                    .forEach((key, attr) -> {
                        String fullName = attr
                                .generateFullName(Files.getNameWithoutExtension(dataFile.getName()));
                        if (header.contains(fullName)) {
                            fileAttributeMapping.put(header.indexOf(fullName), key);
                        }
                    });

                int identifierIndex = header.indexOf(_identifierVarName);
                int currentYear = org.meyerlab.nopence.soep_parser.util
                        .Helper.getYearFromFileName(Files.getNameWithoutExtension(dataFile.getName()));

                while (fileHelper.hasNextLine()) {
                    line = fileHelper.nextLine();

                    String[] lineArray = line.split(",");
                    int currentIdentifier = Integer.parseInt(lineArray[identifierIndex]);
                    int observationMonth = Integer.parseInt(lineArray[monthIndex]);

                    Map<Integer, String> instValues = new HashMap<>();
                    fileAttributeMapping
                        .forEach((keyFile, keyGlobal) -> {
                            instValues.put(keyGlobal, lineArray[keyFile]);
                    });

                    Instance inst = new Instance(currentYear,
                            observationMonth,  instValues, currentIdentifier);

                    if (_histories.containsKey(currentIdentifier)) {
                        _histories.get(currentIdentifier).add(inst);
                    } else {
                        List<Instance> history = new ArrayList<>();
                        history.add(inst);
                        _histories.put(currentIdentifier, history);
                    }
                }
            }
        } catch (FileNotValidException | IOException e) {
            e.printStackTrace();
        }

        writeHistories();
    }

    private static void writeHistories() {
        CsvFileHelper csvFileHelper = new CsvFileHelper(5000, SoepOption.OutputFile);

        StringBuilder header = new StringBuilder();
        header.append(_identifierVarName).append(",");
        header.append("ODATE").append(",");

        Set<Integer> keySet = _metaAttributeMap.keySet();
        List<Integer> sortedKeys = new ArrayList<>(keySet);
        Collections.sort(sortedKeys);


        sortedKeys
            .forEach(key ->
                    header.append(_metaAttributeMap.get(key).getShortName()).append(","));

        csvFileHelper.addHeader(header.toString());

        _histories
                .forEach((personId, instances) -> {
                            Collections.sort(instances);
                            instances.forEach(inst ->
                                    csvFileHelper.addInstance(inst.toString()));
                        });


        csvFileHelper.emptyBuffer();
    }

    private static void generateMetaAttrMap() throws FileNotFoundException {
        FileReader fileReader = new FileReader(new File(SoepOption.MetaInputFile));

        JsonObject rootElement = _jsonParser.parse(fileReader).getAsJsonObject();

        _identifierVarName = rootElement.get("identifier").getAsString();

        JsonObject monthObservationAttr = rootElement.getAsJsonObject
                ("monthObservationVar");

        _monthObservationAttribute = new Attribute(-1,
                monthObservationAttr.get("short").getAsString(),
                AttributeType.valueOf(monthObservationAttr.get("type").getAsString()));

        JsonArray vars = rootElement.getAsJsonArray("vars");

        vars
            .forEach(element -> {
                JsonObject varObj = element.getAsJsonObject();
                _metaAttributeMap.addAttribute(
                        varObj.get("short").getAsString(),
                        AttributeType.valueOf(varObj.get("type").getAsString()));
            });
    }
}


