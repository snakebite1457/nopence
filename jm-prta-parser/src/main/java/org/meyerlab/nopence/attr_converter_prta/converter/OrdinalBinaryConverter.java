package org.meyerlab.nopence.attr_converter_prta.converter;

import net.openhft.koloboke.collect.map.hash.HashIntDoubleMaps;
import org.meyerlab.nopence.attr_converter_prta.attributes.Attribute;
import org.meyerlab.nopence.attr_converter_prta.attributes.BinaryAttribute;
import org.meyerlab.nopence.attr_converter_prta.attributes.OrdinalAttribute;
import org.meyerlab.nopence.attr_converter_prta.util.IntGenerator;
import org.meyerlab.nopence.attr_converter_prta.util.exceptions.AttrNotContainsValueException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class OrdinalBinaryConverter extends Converter {

    private OrdinalAttribute _ordinalAttr;
    private Map<Double, List<Integer>> _attrMapping;
    private Map<Integer, Double> _ordinalOrder;
    private boolean _nameAsPrefix;

    public OrdinalBinaryConverter(OrdinalAttribute ordinalAttribute,
                                  IntGenerator intGenerator,
                                  Map<Integer, Double> ordinalOrder,
                                  boolean nameAsPrefix) {
        _ordinalAttr = ordinalAttribute;
        _attrMapping = new HashMap<>();
        _intGenerator = intGenerator;
        _ordinalOrder = HashIntDoubleMaps.newImmutableMap(ordinalOrder);
        _nameAsPrefix = nameAsPrefix;
        convert();
    }

    private void convert() {
        HashMap<Double, String> values = new HashMap<>(_ordinalAttr.getValues());
        _ordinalOrder
                .entrySet()
                .stream()
                .sorted((o1, o2) -> Double.compare(o1.getKey(), o2.getKey()))
                .forEach(entry -> this.addValueToConvertedAttr(entry.getKey()
                        , entry.getValue()
                        , values.get(entry.getValue())));
    }

    private void addValueToConvertedAttr(int ordinalNumber,
                                         double originalValue,
                                         String ordinalName) {

        int attrId = _intGenerator.getNext();
        List<String> binValues = new ArrayList<>(2);
        binValues.add(0, "Inactive");
        binValues.add(1, "Active");

        int firstLowerOrdinalId = _ordinalOrder.keySet()
                .stream()
                .filter(key -> key < ordinalNumber)
                .max(Integer::compareTo).orElse(ordinalNumber);

        List<Integer> mappedConvertedAttrs = new ArrayList<>();

        // Current given ordinal number is the lowest one
        if (firstLowerOrdinalId == ordinalNumber) {
            mappedConvertedAttrs.add(attrId);
        } else {
            mappedConvertedAttrs.addAll(
                    _attrMapping.get(_ordinalOrder.get(firstLowerOrdinalId)));

            mappedConvertedAttrs.add(attrId);
        }

        _attrMapping.put(originalValue, mappedConvertedAttrs);

        if (_nameAsPrefix) {
            ordinalName = _ordinalAttr.getName() + "; " + ordinalName;
        }

        BinaryAttribute binAttr =
                new BinaryAttribute(attrId, ordinalName,
                        binValues, Attribute.Type.ordinal, _ordinalAttr.getWeight());


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
