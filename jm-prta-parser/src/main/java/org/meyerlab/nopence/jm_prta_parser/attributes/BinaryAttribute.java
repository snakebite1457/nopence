package org.meyerlab.nopence.jm_prta_parser.attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis Meyer
 */
public class BinaryAttribute extends Attribute {


    private List<String> _values;

    public BinaryAttribute(int id, String name,
                           List<String> values,
                           Type convertedType,
                           double weight) {
        super(id, name, convertedType, weight);
        _values = new ArrayList<>(values);
    }

    @Override
    public Type getType() {
        return Type.binary;
    }


}
