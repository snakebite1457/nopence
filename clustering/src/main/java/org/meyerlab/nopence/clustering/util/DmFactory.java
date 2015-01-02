package org.meyerlab.nopence.clustering.util;

import org.meyerlab.nopence.clustering.DimensionInformation;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Dennis Meyer
 */
public class DmFactory {

    public static IDistanceMeasure createDistanceMeasure(
            String measureName, DimensionInformation dimensionInformation)
            throws
                ClassNotFoundException,
                NoSuchMethodException,
                IllegalAccessException,
                InvocationTargetException,
                InstantiationException {

        Class measureClass = Class.forName("org.meyerlab.nopence.clustering" +
                ".algorithms.measures.distance." + measureName);
        Constructor measureConstructor = measureClass.getDeclaredConstructor
                (DimensionInformation.class);

        return (IDistanceMeasure) measureConstructor.newInstance(dimensionInformation);
    }
}
