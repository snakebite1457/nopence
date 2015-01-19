package org.meyerlab.nopence.jm_prta_parser.util.exceptions;


import org.meyerlab.nopence.jm_prta_parser.attributes.Attribute;
import org.meyerlab.nopence.jm_prta_parser.util.PrtaParserConstants;

/**
 * @author Dennis Meyer
 */
public class AttrNotContainsValueException extends Exception {

    public AttrNotContainsValueException(Attribute attr, double value) {
        super(String.format(
                PrtaParserConstants.EX_ATTR_NOT_CONTAIN_VAL, attr.getName(), value));
    }
}
