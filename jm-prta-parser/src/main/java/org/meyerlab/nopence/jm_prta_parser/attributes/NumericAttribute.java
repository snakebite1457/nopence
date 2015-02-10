package org.meyerlab.nopence.jm_prta_parser.attributes;

import org.kramerlab.carbon.util.discretization.Discretization;
import org.kramerlab.carbon.util.discretization.DiscretizationType;

import java.util.stream.IntStream;

/**
 * Provides a numeric attribute. Contains information about the attribute but
 * also the discretization which will be used to transfer this attribute to
 * other attribute types.
 *
 * @author Dennis Meyer
 */
public class NumericAttribute extends Attribute {

    private Discretization _discretization;

    // Makes it possible to store the discretization type before the
    // discretization is ready to initialize
    private DiscretizationType _discretizationType;

    // Same reason as discretization type
    private int _numDiscretizationBins;

    /**
     * Create an object which stores all
     * the information for a numeric attribute
     *
     * @param id             the attribute/variable id
     * @param name           the name of the attribute
     * @param discretization the discretization which belongs to the numeric
     *                       attribute. This is necessary because the numeric
     *                       attribute must be converted to an other attribute type.
     */
    public NumericAttribute(int id,
                            String name,
                            Discretization discretization,
                            Type convertedType) {
        super(id, name, convertedType);

        _discretization = discretization;
    }

    /**
     * Create an object which stores all the information for a numeric
     * attribute.
     *
     * @param id                    the attribute/variable id
     * @param name                  the name of the attribute
     * @param discretizationType    give an possibility to store the
     *                              discretization type while the discretization
     *                              is not yet available
     * @param numDiscretizationBins possibility to store the number of
     *                              discretization bins while the
     *                              discretization is not yet available
     */
    public NumericAttribute(int id,
                            String name,
                            DiscretizationType discretizationType,
                            int numDiscretizationBins,
                            Type convertedType) {
        super(id, name, convertedType);

        _discretizationType = discretizationType;
        _numDiscretizationBins = numDiscretizationBins;
    }

    @Override
    public Type getType() {
        return Type.numeric;
    }

    /**
     * Returns the bin number based on the attribute value which belongs to
     * this discretization
     *
     * @param value the attribute value
     * @return the bin number which contains the given value
     */
    public int getBinByValue(double value) {
        return IntStream.range(0, _discretization.getNumberOfBins() + 1)
                .filter(a -> _discretization.getBorder(a) >= value)
                .findFirst().getAsInt();
    }

    /**
     * Returns the upper and lower bounds for the given bin number
     *
     * @param index the bin number
     * @return array where 0 is the lower bound and 1 is the upper bound
     * @throws Exception
     */
    public double[] getBorder(int index) throws Exception {
        if (index > _discretization.getNumberOfBins() - 1) {
            throw new Exception();
        }

        double[] borders = new double[2];
        borders[0] = _discretization.getBorder(index); //min value
        borders[1] = _discretization.getBorder(index + 1); //max value
        return borders;
    }

    /**
     * Returns the discretization type for the discretization for this numeric
     * attribute
     *
     * @return the discretization type
     */
    public DiscretizationType getDiscretizationType() {
        return _discretizationType;
    }

    /**
     * Makes it possible to set the discretization after the numeric
     * attribute is already initialized. The internal fields for the number
     * of discretization bins and the discretization type are also updated.
     *
     * @param discretization the discretization which will be used for this
     *                       numeric attribute
     */
    public void setDiscretization(Discretization discretization) {
        _discretization = discretization;
        _discretizationType = discretization.getDiscretizationType();
        _numDiscretizationBins = discretization.getNumberOfBins();
    }

    /**
     * Returns the number of bins for the discretization
     *
     * @return the number of bins
     */
    public int getNumDiscretizationBins() {
        return _numDiscretizationBins;
    }

    public void addValueToDiscretization(double value) {
        if (_discretization == null) {
            return;
        }

        _discretization.addValue(value);
    }
}
