package org.meyerlab.nopence.attr_converter_prta.io;


import org.kramerlab.carbon.util.discretization.Discretization;
import org.kramerlab.carbon.util.discretization.DiscretizationType;
import org.meyerlab.nopence.attr_converter_prta.attributes.Attribute;
import org.meyerlab.nopence.attr_converter_prta.attributes.NominalAttribute;
import org.meyerlab.nopence.attr_converter_prta.attributes.NumericAttribute;
import org.meyerlab.nopence.attr_converter_prta.attributes.OrdinalAttribute;
import org.meyerlab.nopence.attr_converter_prta.converter.Converter;
import org.meyerlab.nopence.attr_converter_prta.converter.NominalBinaryConverter;
import org.meyerlab.nopence.attr_converter_prta.converter.NumericBinaryConverter;
import org.meyerlab.nopence.attr_converter_prta.converter.OrdinalBinaryConverter;
import org.meyerlab.nopence.attr_converter_prta.util.IntGenerator;
import org.meyerlab.nopence.attr_converter_prta.util.Option;
import org.meyerlab.nopence.attr_converter_prta.util.PrtaParserConstants;
import org.meyerlab.nopence.attr_converter_prta.util.exceptions.AttrNotContainsValueException;
import org.meyerlab.nopence.util.FileHelper;
import org.meyerlab.nopence.utils.Constants;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class which makes it possible to create the converter for the
 * given attributes. These attribute must be described in a attribute
 * information file.
 * After the converter was created the variables histogramm xml file is
 * created.
 *
 * @author Dennis Meyer
 */
public class TypeConverter {

    private File _attrInfoFile;
    private IntGenerator _intGenerator;

    private Map<Integer, Converter> _converterAttrIdMap;
    private DocumentBuilder _builder;

    public TypeConverter(File attrInfoFile) {

        _attrInfoFile = attrInfoFile;
        init();
    }

    /**
     * Reads the attribute information file out and build based in this
     * information the converter for each attribute.
     * <p>
     * In this case we only need converter from x to binary.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void buildConverter() throws IOException, SAXException {
        Document attrInfoDocument = _builder.parse(_attrInfoFile);
        NodeList nodeList = attrInfoDocument
                .getElementsByTagName(PrtaParserConstants.ATTR_INFO_ATTR_ROOT);


        List<Node> continuousNodes = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            NamedNodeMap nodeAttrs = nodeList.item(i).getAttributes();

            String curAttrType = nodeAttrs.getNamedItem(
                    PrtaParserConstants.ATTR_INFO_ATTR_NODE_TYPE).getNodeValue();

            switch (Attribute.Type.valueOf(curAttrType)) {
                case ordinal:
                case nominal:
                    createDiscreteConverter(nodeList.item(i));
                    break;
                case numeric:
                    continuousNodes.add(nodeList.item(i));
                    break;
            }
        }

        createContinuousConverter(continuousNodes);
    }


    /**
     * Returns the converted attributes and whose value. Because this is a
     * special case the value is always 1, which means that this attribute
     * must be set based on the given original attribute and whose value.
     *
     * @param attrId the original attribute id. Comes either from the
     *               attribute information file or the column index from the
     *               instances file.
     * @param value  the value of the original attribute. Comes from the
     *               instances file.
     * @return the attributes/events which must be set in the input file for
     * the automata.
     */
    public Map<Attribute, Double> getConvertedAttrWithValues(
            int attrId, double value) {

        if (!_converterAttrIdMap.containsKey(attrId)) {
            return null;
        }

        try {
            Map<Attribute, Double> convertedAttrsWithValues = new HashMap<>();

            Converter currentAttrConverter = _converterAttrIdMap.get(attrId);

            List<Attribute> convertedAttrs =
                    currentAttrConverter.getConvertedAttr(value);

            // This only works for the special case of x to binary mappings. If
            // this is not the case then this method will fail, cause the value
            // of the converted attribute is set to 1.
            convertedAttrs.forEach(attr -> convertedAttrsWithValues.put(attr, 1.0));

            return convertedAttrsWithValues;

        } catch (AttrNotContainsValueException ex) {
            return null;
        }
    }

    public Map<Integer, Converter> getConverterAttrIdMap() {
        return new HashMap<>(_converterAttrIdMap);
    }

    public int numConvertedAttrs() {
        return _intGenerator.getLastNumber();
    }

    private void init() {

        DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();

        try {

            _builder = _factory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        _converterAttrIdMap = new HashMap<>();
        _intGenerator = new IntGenerator();
    }

    private Map<Double, String> getValues(Node node,
                                          boolean ordinal,
                                          Map<Integer, Double> ordinalOrder) {

        HashMap<Double, String> valueMap = new HashMap<>();
        NodeList values = node.getChildNodes();

        for (int i = 0; i < values.getLength(); i++) {
            if (values.item(i).getNodeType() == Node.ELEMENT_NODE) {
                values = values.item(i).getChildNodes();
                break;
            }
        }

        for (int i = 0; i < values.getLength(); i++) {
            if (values.item(i).getNodeType() == Node.TEXT_NODE) {
                continue;
            }

            double valueNumber = Double.parseDouble(values.item(i)
                    .getAttributes().getNamedItem(PrtaParserConstants.ATTR_INFO_VAL_NODE_NUM).getNodeValue());

            String valueName = values.item(i).getAttributes().getNamedItem
                    (PrtaParserConstants.ATTR_INFO_VAL_NODE_NAME).getNodeValue();

            valueMap.put(valueNumber, valueName);

            if (ordinal) {
                int oPosition = Integer.parseInt(values.item(i)
                        .getAttributes().getNamedItem(PrtaParserConstants.ATTR_INFO_VAL_NODE_OPOSITION)
                        .getNodeValue());
                ordinalOrder.put(oPosition, valueNumber);
            }
        }
        return valueMap;
    }

    /**
     * Tries to initialize the discretization for the numeric attributes.
     *
     * @param numericAttrs the numeric attributes
     */
    private void setDiscretizationToAttrs(List<NumericAttribute> numericAttrs) {
        try {
            initDiscretizations(numericAttrs);

            // Add values to the discretizations

            File instancesFile = new File(Option.getInstance().getPathDataFile());
            FileHelper fileHelper = new FileHelper(instancesFile, true);

            fileHelper.reset(instancesFile, true);
            while (fileHelper.getNumberOfReadLines() <=
                    PrtaParserConstants.NUM_INST_FOR_DISCRETIZATION
                    && fileHelper.hasNextLine()) {
                String line = fileHelper.nextLine();
                if (line == null) {
                    continue;
                }

                String[] lineArray = line.split(Constants.FILE_CSV_SEPARATION);

                for (NumericAttribute attr : numericAttrs) {
                    String strValue = lineArray[attr.getId()];
                    if (!Helper.isNumeric(strValue)) {
                        continue;
                    }

                    attr.addValueToDiscretization(Double.parseDouble(strValue));
                }
            }

            fileHelper.close();

        } catch (IOException | FileNotValidException e) {
            e.printStackTrace();
        }
    }

    private void initDiscretizations(List<NumericAttribute> numericAttrs)
            throws IOException, FileNotValidException {

        // key -> attr id; value -> list for minMax values for this attribute.
        // 0 -> minValue; 1 -> maxValue
        Map<Integer, List<Double>> minMaxAttr = new HashMap<>();

        File instancesFile = new File(Option.getInstance().getPathDataFile());
        FileHelper fileHelper = new FileHelper(instancesFile, true);

        // Skip header
        fileHelper.nextLine();

        // Find min, max value for every numeric attribute
        while (fileHelper.getNumberOfReadLines() <=
                PrtaParserConstants.NUM_INST_FOR_FIND_MIN_MAX_DIS
                && fileHelper.hasNextLine()) {

            String line = fileHelper.nextLine();
            if (line == null) {
                continue;
            }

            String[] lineArray = line.split(Constants.FILE_CSV_SEPARATION);

            for (NumericAttribute attr : numericAttrs) {
                String strValue = lineArray[attr.getId()];
                if (!Helper.isNumeric(strValue)) {
                    continue;
                }

                double value = Double.parseDouble(strValue);
                if (minMaxAttr.containsKey(attr.getId())) {
                    List<Double> minMax = minMaxAttr.get(attr.getId());

                    // Update min max values
                    if (minMax.get(0) > value) {
                        minMax.set(0, value);
                    } else if (minMax.get(1) < value) {
                        minMax.set(1, value);
                    }

                    minMaxAttr.put(attr.getId(), minMax);
                } else {
                    ArrayList<Double> minMax = new ArrayList<Double>() {{
                        add(0, value);
                        add(1, value);
                    }};

                    minMaxAttr.put(attr.getId(), minMax);
                }
            }
        }

        // Init discretizations
        numericAttrs.forEach(attr -> attr.setDiscretization(
                new Discretization(attr.getNumDiscretizationBins(),
                        attr.getDiscretizationType(),
                        minMaxAttr.get(attr.getId()).get(0),
                        minMaxAttr.get(attr.getId()).get(1))));

        fileHelper.close();
    }

    private void createDiscreteConverter(Node attributeNode) {
        NamedNodeMap nodeAttrs = attributeNode.getAttributes();

        int id = Integer.parseInt(nodeAttrs.getNamedItem(
                PrtaParserConstants.ATTR_INFO_ATTR_NODE_ID).getNodeValue());

        String name = nodeAttrs.getNamedItem(
                PrtaParserConstants.ATTR_INFO_ATTR_NODE_NAME).getNodeValue();

        boolean nameAsPrefix = Boolean.parseBoolean(nodeAttrs.getNamedItem(
                PrtaParserConstants.ATTR_INFO_ATTR_NODE_NAME_AS_PREFIX).getNodeValue());

        double weight = Double.parseDouble(nodeAttrs.getNamedItem(
                PrtaParserConstants.ATTR_INFO_ATTR_NODE_WEIGHT).getNodeValue());

        Map<Double, String> valueMap;

        Converter converter = null;
        Attribute currentAttr = null;

        String curAttrType = nodeAttrs.getNamedItem(
                PrtaParserConstants.ATTR_INFO_ATTR_NODE_TYPE).getNodeValue();

        switch (Attribute.Type.valueOf(curAttrType)) {
            case ordinal:
                HashMap<Integer, Double> ordinalOrder = new HashMap<>();

                valueMap = getValues(attributeNode, true, ordinalOrder);
                currentAttr = new OrdinalAttribute(id, name,
                        valueMap, Attribute.Type.ordinal, weight);

                converter = new OrdinalBinaryConverter(
                        (OrdinalAttribute) currentAttr, _intGenerator,
                        ordinalOrder, nameAsPrefix);
                break;
            case numeric:
                return;
            case nominal:
                valueMap = getValues(attributeNode, false, null);
                currentAttr = new NominalAttribute(id, name,
                        valueMap, Attribute.Type.nominal, weight);
                converter =
                        new NominalBinaryConverter(
                                (NominalAttribute) currentAttr,
                                _intGenerator, nameAsPrefix);
                break;
        }

        if (currentAttr != null) {
            _converterAttrIdMap.put(currentAttr.getId(), converter);
        }
    }

    private void createContinuousConverter(List<Node> continuousAttrs) {

        ArrayList<NumericAttribute> numAttrList = new ArrayList<>();
        HashMap<Integer, NumericBinaryConverter.OrdinalMapping> ordinalMapping = new HashMap<>();
        HashMap<Integer, Boolean> nameAsPrefixMapping = new HashMap<>();

        for (Node continuousAttr : continuousAttrs) {
            int id = Integer.parseInt(continuousAttr.getAttributes().getNamedItem(
                    PrtaParserConstants.ATTR_INFO_ATTR_NODE_ID).getNodeValue());

            String name = continuousAttr.getAttributes().getNamedItem(
                    PrtaParserConstants.ATTR_INFO_ATTR_NODE_NAME).getNodeValue();

            boolean nameAsPrefix = Boolean.parseBoolean(continuousAttr.getAttributes().getNamedItem(
                    PrtaParserConstants.ATTR_INFO_ATTR_NODE_NAME_AS_PREFIX).getNodeValue());

            double weight = Double.parseDouble(continuousAttr.getAttributes().getNamedItem(
                    PrtaParserConstants.ATTR_INFO_ATTR_NODE_WEIGHT).getNodeValue());

            nameAsPrefixMapping.put(id, nameAsPrefix);

            Attribute.Type afterDisType =
                    Attribute.Type.valueOf(continuousAttr.getAttributes().getNamedItem(
                            PrtaParserConstants.ATTR_INFO_VAL_NODE_NUMTYPE_AFTERTYPE).getNodeValue());
            DiscretizationType disType =
                    DiscretizationType.valueOf(continuousAttr.getAttributes().getNamedItem(
                            PrtaParserConstants.ATTR_INFO_VAL_NODE_NUMTYPE_DISTYPE).getNodeValue());
            int numberOfBins =
                    Integer.parseInt(continuousAttr.getAttributes().getNamedItem(
                            PrtaParserConstants.ATTR_INFO_VAL_NODE_NUMTYPE_BINS).getNodeValue());

            if (afterDisType == Attribute.Type.ordinal) {

                ordinalMapping.put(id, NumericBinaryConverter.OrdinalMapping
                        .valueOf(continuousAttr.getAttributes().getNamedItem(
                                PrtaParserConstants.ATTR_INFO_VAL_NODE_NUMTYPE_ORDINALORDER).getNodeValue()));
            }

            numAttrList.add(new NumericAttribute(id, name,
                    disType, numberOfBins, afterDisType, weight));
        }

        // Read out the numAttrList and create the discretization for all
        // these numeric attributes. And then create the converter.
        setDiscretizationToAttrs(numAttrList);
        numAttrList.forEach(attr -> {
            boolean nameAsPrefix = nameAsPrefixMapping.get(attr.getId());
            NumericBinaryConverter.OrdinalMapping ordMapping = ordinalMapping
                    .get(attr.getId());


            if (attr.getConvertedType() == Attribute.Type.ordinal) {
                _converterAttrIdMap.put(attr.getId(),
                        new NumericBinaryConverter(attr, _intGenerator,
                                ordMapping, nameAsPrefix));
            } else {
                _converterAttrIdMap.put(attr.getId(),
                        new NumericBinaryConverter(attr, _intGenerator, nameAsPrefix));
            }
        });
    }
}
