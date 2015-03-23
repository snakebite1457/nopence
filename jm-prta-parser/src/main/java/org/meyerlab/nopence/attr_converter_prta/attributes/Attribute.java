package org.meyerlab.nopence.attr_converter_prta.attributes;

import java.lang.String;

/**
 * @author Dennis Meyer
 */
public abstract class Attribute {

    protected int _id;
    protected String _name;
    protected Type _convertedType;
    protected double _weight;

    public Attribute(int id, String name, Type convertedType, double weight) {
        _id = id;
        _name = name;
        _convertedType = convertedType;
        _weight = weight;
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

    public double getWeight() {
        return _weight;
    }

    public enum Type {
        binary, ordinal, nominal, numeric
    }
}

