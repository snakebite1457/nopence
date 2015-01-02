package org.meyerlab.nopence.jm_prta_parser.converter;

import org.meyerlab.nopence.jm_prta_parser.attributes.Attribute;
import org.meyerlab.nopence.jm_prta_parser.attributes.BinaryAttribute;
import org.meyerlab.nopence.jm_prta_parser.attributes.OrdinalAttribute;
import org.meyerlab.nopence.jm_prta_parser.util.IntGenerator;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.AttrNotContainsValueException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class OrdinalBinaryConverter extends Converter {

    private OrdinalAttribute _ordinalAttr;
    private Map<Double, List<Integer>> _attrMapping;

    public OrdinalBinaryConverter(OrdinalAttribute ordinalAttribute,
                                  IntGenerator intGenerator) {
        _ordinalAttr = ordinalAttribute;
        _attrMapping = new HashMap<>();
        _intGenerator = intGenerator;
        convert();
    }

    private void convert() {
        Map<Double, String> values = _ordinalAttr.getValues();
        values.forEach(this::addValueToConvertedAttr);
    }

    private void addValueToConvertedAttr(double ordinalNumber,
                                         String ordinalName) {

        int attrId = _intGenerator.getNext();
        List<String> binValues = new ArrayList<>(2);
        binValues.add(0, "Inactive");
        binValues.add(1, "Active");

        double firstLowerOrdinalNumberMappingId = _attrMapping.keySet()
                .stream()
                .filter(key -> key < ordinalNumber)
                .max(Double::compareTo).orElse(ordinalNumber);

        List<Integer> mappedConvertedAttrs = new ArrayList<>();

        // Current given ordinal number is the lowest one
        if (firstLowerOrdinalNumberMappingId == ordinalNumber) {
            mappedConvertedAttrs.add(attrId);
        } else {
            mappedConvertedAttrs.addAll(
                    _attrMapping.get(firstLowerOrdinalNumberMappingId));

            mappedConvertedAttrs.add(attrId);

            // Add current converted attr to all mapped attrs which have a
            // higher ordinal number
            _attrMapping.keySet()
                    .stream()
                    .filter(key -> key > firstLowerOrdinalNumberMappingId)
                    .forEach(key -> _attrMapping.get(key).add(attrId));
        }

        _attrMapping.put(ordinalNumber, mappedConvertedAttrs);

        BinaryAttribute binAttr =
                new BinaryAttribute(attrId, ordinalName,
                        binValues, Attribute.Type.ordinal);


        convertedAttrs.put(attrId, binAttr);
        originalAttrTypeByConvertedId.put(attrId, _ordinalAttr);
    }

    @Override
    public List<Attribute> getConvertedAttr(double value) {
        try {
            if (!_attrMapping.containsKey(value)) {
                throw new AttrNotContainsValueException(_ordinalAttr, value);
            }
        } catch (AttrNotContainsValueException ex) {
            ex.printStackTrace();
        }


        List<Integer> convertedAttrIds = _attrMapping.get(value);

        return convertedAttrIds
                .stream()
                .map(convertedAttrs::get)
                .collect(Collectors.toList());
    }

    @Override
    public Attribute getOriginalAttr() {
        return _ordinalAttr;
    }
}
