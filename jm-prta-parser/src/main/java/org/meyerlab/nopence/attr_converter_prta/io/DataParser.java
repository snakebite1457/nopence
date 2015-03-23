package org.meyerlab.nopence.attr_converter_prta.io;

import org.meyerlab.nopence.attr_converter_prta.attributes.Attribute;
import org.meyerlab.nopence.attr_converter_prta.converter.Converter;
import org.meyerlab.nopence.attr_converter_prta.util.PrtaParserConstants;
import org.meyerlab.nopence.attr_converter_prta.util.PrtaParserHelper;
import org.meyerlab.nopence.attr_converter_prta.util.Option;
import org.meyerlab.nopence.attr_converter_prta.util.model.Instance;
import org.meyerlab.nopence.attr_converter_prta.util.model.OutputFile;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class DataParser {

    private File _varHistFile;
    private SimpleDateFormat _dateFormat;
    private TypeConverter _typeConverter;

    // These output files differ only in the number of sequences which are
    // allowed.
    private ArrayList<OutputFile> _outputFiles;

    /**
     * Initializes a data parser
     *
     * @param attrInfoFile the information file for the original attributes
     * @param varHistFile  the file where the attribute information about the
     *                     converted attribute will be written. This file will
     *                     be deleted if exists.
     * @throws IOException
     * @throws SAXException
     */
    public DataParser(File attrInfoFile, File varHistFile)
            throws IOException, SAXException {

        _varHistFile = varHistFile;
        _dateFormat = new SimpleDateFormat(Option.getInstance().getDateFormat());
        _outputFiles = new ArrayList<>();

        _typeConverter = new TypeConverter(attrInfoFile);
        _typeConverter.buildConverter();

        for (int maxSeq : Option.getInstance().getMaxSeqList()) {
            String outputFileName =
                    PrtaParserHelper.generateOutputFileName(attrInfoFile, maxSeq);
            File outputFile = new File(
                    Option.getInstance().getPathOutputDir(), outputFileName);

            _outputFiles.add(new OutputFile(outputFile, maxSeq));
        }
    }

    /**
     * Parse the given line with the attribute values into the converted
     * attributes. After that a instance will created.
     * <p/>
     * The sequences are written to the output file.
     *
     * @param splittedLine the splitted line from the instances file
     */
    public void parse(String[] splittedLine, boolean lastLine) {

        try {
            long personId = Long.parseLong(
                    splittedLine[Option.getInstance().getColPersonId()]);

            Date observationDate = _dateFormat.parse(
                    splittedLine[Option.getInstance().getColObservationDate()]);

            Instance instance = new Instance(observationDate);
            for (int i = 0; i < splittedLine.length; i++) {
                try {
                    double value = Double.parseDouble(splittedLine[i]);

                    Map<Attribute, Double> conAttr =
                            _typeConverter.getConvertedAttrWithValues(i, value);

                    if (conAttr != null) {
                        instance.addEvents(conAttr.keySet());
                    }
                } catch (NumberFormatException ignored) { }
            }

            // Add the instance to the output files.
            _outputFiles
                    .stream()
                    .filter(out -> !out.seqLimitReached())
                    .forEach(out -> out.addInstance(instance, personId, lastLine));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Based on the converted attributes, this method will create the
     * variables histogramm xml file.
     */
    public void writeVarHistFile() throws IOException {
        Helper.createFile(_varHistFile);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        try {

            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element varHistogrammRootElement = document.createElement
                    ("VariablesHistogramm");

            int maxOriginalId = _typeConverter.numConvertedAttrs();

            for (Converter converter : _typeConverter.getConverterAttrIdMap().values()) {
                Map<Integer, Attribute> convertedAttrs = converter
                        .getConvertedAttrs();

                for (int convertedAttrKey : convertedAttrs.keySet()) {
                    Attribute curConvertedAttr = convertedAttrs.get(convertedAttrKey);

                    Element varElement = document.createElement("variable");

                    // Create the elements for the current var element
                    createVarElements(document, maxOriginalId,
                            curConvertedAttr, varElement, converter);

                    // Adds the current var element as a child of the root element
                    varHistogrammRootElement.appendChild(varElement);
                }
            }

            document.appendChild(varHistogrammRootElement);

            // Push the already created xml dom to the given file path
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.METHOD, Constants.FILE_EXTENSION_XML);
            tr.setOutputProperty(OutputKeys.ENCODING, Constants.FILE_ENCODING);

            // send DOM to file
            tr.transform(new DOMSource(document),
                    new StreamResult(new FileOutputStream(_varHistFile)));


        } catch (ParserConfigurationException
                | FileNotFoundException
                | TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the child elements for the variable and adds
     * these to the current var element.
     */
    private void createVarElements(Document document,
                                   int maxOriginalId,
                                   Attribute curConvertedAttr,
                                   Element varElement,
                                   Converter curConverter) {
        // Create varId element
        Element varIdElement =
                document.createElement(PrtaParserConstants.VAR_HIST_ELEMENT_VAR_ID);
        varIdElement.appendChild(
                document.createTextNode(
                        String.valueOf(curConvertedAttr.getId())));

        // Create labelShort
        Element labelShortElement =
                document.createElement(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_SHORT);
        labelShortElement.appendChild(
                document.createTextNode(curConvertedAttr.getName()));

        // Create labelLong
        Element labelLongElement =
                document.createElement(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_LONG);
        labelLongElement.appendChild(
                document.createTextNode(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_LONG_TEXT));

        // Create cluster
        Element clusterElement =
                document.createElement(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_CLUSTER);
        clusterElement.appendChild(
                document.createTextNode(""));

        // Create adds
        Element addsElement =
                document.createElement(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_ADDS);
        addsElement.appendChild(
                document.createTextNode(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_ADDS_TEXT));

        Attribute orgAttr = curConverter
                .getOriginalAttrByConvertedAttrId(curConvertedAttr.getId());

        // Create type
        Element typeElement =
                document.createElement("type");
        typeElement.appendChild(
                document.createTextNode(orgAttr.getConvertedType().name()));

        // Create originalId
        /*int ordinalId = orgAttr.getConvertedType() == Attribute.Type.ordinal
                ? orgAttr.getId()
                : maxOriginalId + curConvertedAttr.getId();*/

        /*Element ordinalIdElement =
                document.createElement(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_ORDINALID);
        ordinalIdElement.appendChild(
                document.createTextNode(String.valueOf(ordinalId)));*/

        // Create originalId
        Element ordinalIdElement =
                document.createElement("originalAttrId");
        ordinalIdElement.appendChild(
                document.createTextNode(String.valueOf(orgAttr.getId())));

        // Create weight
        Element weightElement =
                document.createElement(PrtaParserConstants.VAR_HIST_ELEMENT_LABEL_WEIGHT);
        weightElement.appendChild(
                document.createTextNode(String.valueOf(curConvertedAttr.getWeight())));

        varElement.appendChild(varIdElement);
        varElement.appendChild(labelShortElement);
        varElement.appendChild(labelLongElement);
        varElement.appendChild(clusterElement);
        varElement.appendChild(addsElement);
        varElement.appendChild(ordinalIdElement);
        varElement.appendChild(typeElement);
        varElement.appendChild(weightElement);
    }


}