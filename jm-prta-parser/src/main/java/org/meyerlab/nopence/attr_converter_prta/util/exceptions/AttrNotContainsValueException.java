package org.meyerlab.nopence.attr_converter_prta.util.exceptions;


import org.meyerlab.nopence.attr_converter_prta.attributes.Attribute;
import org.meyerlab.nopence.attr_converter_prta.util.PrtaParserConstants;

/**
 * @author Dennis Meyer
 */
public class AttrNotContainsValueException extends Exception {

    public AttrNotContainsValueException(Attribute attr, double value) {
        super(String.format(
                PrtaParserConstants.EX_ATTR_NOT_CONTAIN_VAL, attr.getName(), value));
    }
}
