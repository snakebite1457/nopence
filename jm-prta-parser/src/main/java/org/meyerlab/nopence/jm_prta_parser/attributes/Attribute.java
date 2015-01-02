package org.meyerlab.nopence.jm_prta_parser.attributes;

import java.lang.String;

/**
 * @author Dennis Meyer
 */
public abstract class Attribute {

    protected int _id;
    protected String _name;
    protected Type _convertedType;

    public Attribute(int id, String name, Type convertedType) {
        _id = id;
        _name = name;
        _convertedType = convertedType;
    }

    /**
     * Returns the attribute type of the given attribute
     *
     * @return the attribute type
     */
    public abstract Type getType();

    /**
     * Returns the attribute type which should be used if the
     * VariablesHistogramm will be created.
     *
     * @return the attribute type
     */
    public Type getConvertedType() {
        return _convertedType;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public enum Type {
        binary, ordinal, nominal, numeric
    }
}

