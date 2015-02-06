package org.meyerlab.nopence.soep_parser.util.model;

import org.meyerlab.nopence.soep_parser.util.AttributeType;
import org.meyerlab.nopence.soep_parser.util.Helper;

/**
 * @author Dennis Meyer
 */
public class Attribute {

    private int _id;
    private String _shortName;
    private AttributeType _attributeType;

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getShortName() {
        return _shortName;
    }

    public void setShortName(String prefix) {
        _shortName = prefix;
    }

    public Attribute(int id, String prefix, AttributeType attributeType) {
        _id = id;
        _shortName = prefix;
        _attributeType = attributeType;
    }

    public String generateFullName(String fileName) {
        return Helper.completeVarName(
                fileName, _shortName, _attributeType);
    }
}
