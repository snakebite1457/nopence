package org.meyerlab.nopence.jm_prta_parser.converter;

import org.meyerlab.nopence.jm_prta_parser.attributes.Attribute;
import org.meyerlab.nopence.jm_prta_parser.attributes.BinaryAttribute;
import org.meyerlab.nopence.jm_prta_parser.attributes.NumericAttribute;
import org.meyerlab.nopence.jm_prta_parser.util.IntGenerator;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.AttrNotContainsValueException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class NumericBinaryConverter extends Converter {

    /**
     * key -> discretization bin index
     * value -> list of new id's in case of ordinal
     */
    private Map<Integer, List<Integer>> _attrMapping;


    private NumericAttribute _numericAttribute;

    public NumericBinaryConverter(NumericAttribute numericAttribute,
                                  IntGenerator intGenerator) {
        _numericAttribute = numericAttribute;
        _attrMapping = new HashMap<>();
        _intGenerator = intGenerator;

        try {
            convert();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convert() throws Exception {
        int numberOfBins = _numericAttribute.getNumDiscretizationBins();

        List<String> binValues = new ArrayList<>(2);
        binValues.add(0, "Inactive");
        binValues.add(1, "Active");

        for (int i = 0; i < numberOfBins; i++) {
            double[] borders = _numericAttribute.getBorder(i);

            int newAttrId = _intGenerator.getNext();
            String newAttrName = new StringBuilder(_numericAttribute.getName())
                    .append(" ").append(borders[0])
                    .append(" - ").append(borders[1]).toString();

            List<Integer> mappedAttrIds = new ArrayList<>();
            mappedAttrIds.add(newAttrId);

            // In case of ordinal attribute after discretization
            if (_numericAttribute.getConvertedType() ==
                    Attribute.Type.ordinal && i != 0) {

                // It is guaranteed that the borders are ordered from - to +
                mappedAttrIds.addAll(_attrMapping.get(i - 1));
            }

            _attrMapping.put(i, mappedAttrIds);

            convertedAttrs.put(newAttrId, new BinaryAttribute(newAttrId, newAttrName,
                    binValues, _numericAttribute.getConvertedType()));
            originalAttrTypeByConvertedId.put(newAttrId, _numericAttribute);
        }
    }

    @Override
    public List<Attribute> getConvertedAttr(double value)
            throws AttrNotContainsValueException {

        int bin = _numericAttribute.getBinByValue(value);
        List<Integer> convertedBinAttrIds = _attrMapping.get(bin);

        List<Attribute> curConvertedAttrs = new ArrayList<>();
        convertedBinAttrIds.forEach(id ->
                curConvertedAttrs.add(convertedAttrs.get(id)));

        return curConvertedAttrs;
    }

    @Override
    public Attribute getOriginalAttr() {
        return _numericAttribute;
    }

}
