package org.meyerlab.nopence.jm_prta_parser.attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class NominalAttribute extends Attribute {

    private Map<Double, String> _values;

    public NominalAttribute(int id, String name,
                            Map<Double, String> values, Type convertedType) {
        super(id, name, convertedType);

        _values = new HashMap<>(values);
    }

    @Override
    public Type getType() {
        return Type.nominal;
    }

    public int numValues() {
        return _values.size();
    }

    public String getValue(double index) throws Exception {
        if (!_values.containsKey(index)) {
            throw new Exception();
        }

        return _values.get(index);
    }

    public Map<Double, String> getValues() {
        return new HashMap<>(_values);
    }
}
