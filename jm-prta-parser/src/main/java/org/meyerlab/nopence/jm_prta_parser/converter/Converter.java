package org.meyerlab.nopence.jm_prta_parser.converter;



import org.meyerlab.nopence.jm_prta_parser.attributes.Attribute;
import org.meyerlab.nopence.jm_prta_parser.util.IntGenerator;
import org.meyerlab.nopence.jm_prta_parser.util.exceptions.AttrNotContainsValueException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public abstract class Converter {

    protected IntGenerator _intGenerator;

    protected Map<Integer, Attribute> originalAttrTypeByConvertedId = new HashMap<>();

    protected Map<Integer, Attribute> convertedAttrs = new HashMap<>();

    public Map<Integer, Attribute> getConvertedAttrs() {
        return new HashMap<>(convertedAttrs);
    }

    public Attribute getOriginalAttrByConvertedAttrId(int id) {
        return originalAttrTypeByConvertedId.get(id);
    }

    public abstract List<Attribute> getConvertedAttr(double value)
            throws AttrNotContainsValueException;

    public abstract Attribute getOriginalAttr();
}
