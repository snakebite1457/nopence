package org.meyerlab.nopence.soep_parser.util;

import org.meyerlab.nopence.soep_parser.util.model.Attribute;

import java.util.HashMap;

/**
 * @author Dennis Meyer
 */
public class AttributeMap extends HashMap<Integer, Attribute> {
    private int lastId = -1;

    public void addAttribute(String prefix, AttributeType attributeType) {
        Attribute attr = new Attribute(++lastId, prefix, attributeType);
        put(attr.getId(), attr);
    }
}
