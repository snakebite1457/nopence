package org.meyerlab.nopence.attr_converter_prta.converter;



import org.meyerlab.nopence.attr_converter_prta.attributes.Attribute;
import org.meyerlab.nopence.attr_converter_prta.attributes.BinaryAttribute;
import org.meyerlab.nopence.attr_converter_prta.attributes.NominalAttribute;
import org.meyerlab.nopence.attr_converter_prta.util.IntGenerator;
import org.meyerlab.nopence.attr_converter_prta.util.exceptions.AttrNotContainsValueException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class NominalBinaryConverter extends Converter {

    private NominalAttribute _nomAttr;
    private Map<Double, Integer> _attrMapping;
    private boolean _nameAsPrefix;

    public NominalBinaryConverter(NominalAttribute nomAttr,
                                  IntGenerator intGenerator,
                                  boolean nameAsPrefix) {
        _nomAttr = nomAttr;
        _attrMapping = new HashMap<>();
        _intGenerator = intGenerator;
        _nameAsPrefix = nameAsPrefix;
        convert();
    }

    private void convert() {
        Map<Double, String> values = _nomAttr.getValues();
        values.forEach(this::addValueToConvertedAttr);
    }

    private void addValueToConvertedAttr(double number,
                                         String name) {

        int attrId = _intGenerator.getNext();
        List<String> binValues = new ArrayList<>(2);
        binValues.add(0, "Inactive");
        binValues.add(1, "Active");

        _attrMapping.put(number, attrId);

        if (_nameAsPrefix) {
            name = _nomAttr.getName() + "; " + name;
        }

        BinaryAttribute binAttr =
                new BinaryAttribute(attrId, name,
                        binValues, Attribute.Type.nominal, _nomAttr.getWeight());

        convertedAttrs.put(attrId, binAttr);
        originalAttrTypeByConvertedId.put(attrId, _nomAttr);
    }

    @Override
    public List<Attribute> getConvertedAttr(double value)
            throws AttrNotContainsValueException {

        if (!_attrMapping.containsKey(value)) {
            throw new AttrNotContainsValueException(_nomAttr, value);
        }

        List<Attribute> curConvertedAttrs = new ArrayList<>();

        Attribute convertedAttr = convertedAttrs.get(_attrMapping.get(value));
        curConvertedAttrs.add(convertedAttr);

        return curConvertedAttrs;
    }

    @Override
    public Attribute getOriginalAttr() {
        return _nomAttr;
    }
}
