package org.meyerlab.nopence.jm_prta_parser.io;


import moa.recommender.rc.utils.Hash;
import org.kramerlab.carbon.util.discretization.Discretization;
import org.kramerlab.carbon.util.discretization.DiscretizationType;

import org.meyerlab.nopence.jm_prta_parser.attributes.Attribute;
import org.meyerlab.nopence.jm_prta_parser.attributes.NominalAttribute;
import org.meyerlab.nopence.jm_prta_parser.attributes.NumericAttribute;
import org.meyerlab.nopence.jm_prta_parser.attributes.OrdinalAttribute;
import org.meyerlab.nopence.jm_prta_parser.converter.Converter;
import org.meyerlab.nopence.jm_prta_parser.converter.NominalBinaryConverter;
import org.meyerlab.nopence.jm_prta_parser.converter.NumericBinaryConverter;
import org.meyerlab.nopence.jm_prta_parser.converter.OrdinalBinaryConverter;
import org.meyerlab.nopence.jm_prta_parser.util.Constants;
import org.meyerlab.nopence.jm_prta_parser.util.Helper;
import org.meyerlab.nopence.jm_prta_parser.util.IntGenerator;
import org.meyerlab.nopence.jm_prta_parser.util.Option;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.AttrNotContainsValueException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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
     * </p>
     *
     * @throws IOException
     * @throws SAXException
     */
    public void buildConverter() throws IOException, SAXException {
        Document attrInfoDocument = _builder.parse(_attrInfoFile);
        NodeList nodeList = attrInfoDocument
                .getElementsByTagName(Constants.ATTR_INFO_ATTR_ROOT);

        ArrayList<NumericAttribute> numAttrList = new ArrayList<>();
        HashMap<Integer, NumericBinaryConverter.OrdinalMapping> numAttrOrdinalMapping = new HashMap<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            NamedNodeMap nodeAttrs = nodeList.item(i).getAttributes();

            int id = Integer.parseInt(nodeAttrs.getNamedItem(
                    Constants.ATTR_INFO_ATTR_NODE_ID).getNodeValue());
            String name = nodeAttrs.getNamedItem(
                    Constants.ATTR_INFO_ATTR_NODE_NAME).getNodeValue();

            HashMap<Double, String> valueMap;

            Converter converter = null;
            Attribute currentAttr = null;

            String curAttrType = nodeList.item(i).getAttributes()
                    .getNamedItem(Constants.ATTR_INFO_ATTR_NODE_TYPE)
                    .getNodeValue();

            switch (Attribute.Type.valueOf(curAttrType)) {
                case ordinal:
                    HashMap<Integer, Double> ordinalOrder = new HashMap<>();

                    valueMap = getValues(nodeList.item(i), true, ordinalOrder);
                    currentAttr = new OrdinalAttribute(id, name,
                            valueMap, Attribute.Type.ordinal);

                    converter = new OrdinalBinaryConverter(
                            (OrdinalAttribute) currentAttr,
                            _intGenerator, ordinalOrder);
                    break;
                case numeric:
                    Attribute.Type afterDisType =
                            Attribute.Type.valueOf(nodeAttrs.getNamedItem(
                                    Constants.ATTR_INFO_VAL_NODE_NUMTYPE_AFTERTYPE).getNodeValue());
                    DiscretizationType disType =
                            DiscretizationType.valueOf(nodeAttrs.getNamedItem(
                                    Constants.ATTR_INFO_VAL_NODE_NUMTYPE_DISTYPE).getNodeValue());
                    int numberOfBins =
                            Integer.parseInt(nodeAttrs.getNamedItem(
                                    Constants.ATTR_INFO_VAL_NODE_NUMTYPE_BINS).getNodeValue());

                    if (afterDisType == Attribute.Type.ordinal) {
                        NumericBinaryConverter.OrdinalMapping ordinalMapping =
                                NumericBinaryConverter.OrdinalMapping
                                        .valueOf(nodeAttrs.getNamedItem(
                                                Constants.ATTR_INFO_VAL_NODE_NUMTYPE_ORDINALORDER).getNodeValue());

                        numAttrOrdinalMapping.put(id, ordinalMapping);
                    }

                    numAttrList.add(new NumericAttribute(id, name,
                            disType, numberOfBins, afterDisType));
                    continue;
                case nominal:
                    valueMap = getValues(nodeList.item(i), false, null);
                    currentAttr = new NominalAttribute(id, name,
                            valueMap, Attribute.Type.nominal);
                    converter =
                            new NominalBinaryConverter(
                                    (NominalAttribute) currentAttr, _intGenerator);
                    break;
                default:
                    continue;
            }
            _converterAttrIdMap.put(currentAttr.getId(), converter);
        }

        // Read out the numAttrList and create the discretization for all
        // these numeric attributes. And then create the converter.
        setDiscretizationToAttrs(numAttrList);
        numAttrList.forEach(attr -> {
            if (attr.getConvertedType() == Attribute.Type.ordinal) {
                _converterAttrIdMap.put(attr.getId(),
                        new NumericBinaryConverter(attr, _intGenerator,
                                numAttrOrdinalMapping.get(attr.getId())));
            } else {
                _converterAttrIdMap.put(attr.getId(),
                        new NumericBinaryConverter(attr, _intGenerator));
            }
        });
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

    private HashMap<Double, String> getValues(
            Node node, boolean ordinal, HashMap<Integer, Double> ordinalOrder) {

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
                    .getAttributes().getNamedItem(Constants.ATTR_INFO_VAL_NODE_NUM).getNodeValue());
            String valueName = values.item(i).getAttributes().getNamedItem
                    (Constants.ATTR_INFO_VAL_NODE_NAME).getNodeValue();

            valueMap.put(valueNumber, valueName);

            if (ordinal) {
                int oPosition = Integer.parseInt(values.item(i)
                        .getAttributes().getNamedItem(Constants.ATTR_INFO_VAL_NODE_OPOSITION)
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
    private void setDiscretizationToAttrs(
            ArrayList<NumericAttribute> numericAttrs) {

        // key -> attr id; value -> list for minMax values for this attribute.
        // 0 -> minValue; 1 -> maxValue
        HashMap<Integer, List<Double>> minMaxAttr = new HashMap<>();

        BufferedReader br = null;
        try {

            br = new BufferedReader(new InputStreamReader(new
                    FileInputStream(Option.getInstance().getPathDataFile()),
                    Constants.FILE_ENCODING));

            // Skip header
            br.readLine();

            for (int counter = 0; counter <
                    Constants.NUM_INST_FOR_DISCRETIZATION; counter++) {
                String line = br.readLine();
                if (line == null) {
                    continue;
                }

                String[] lineArray = line.split(";");

                for (NumericAttribute attr : numericAttrs) {
                    String strValue = lineArray[attr.getId()].replace(",", ".");
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

            numericAttrs.forEach(attr -> attr.setDiscretization(
                    new Discretization(10,
                            attr.getDiscretizationType(),
                            minMaxAttr.get(attr.getId()).get(0),
                            minMaxAttr.get(attr.getId()).get(1))));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
