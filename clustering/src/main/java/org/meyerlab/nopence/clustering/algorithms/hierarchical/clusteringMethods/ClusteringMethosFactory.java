package org.meyerlab.nopence.clustering.algorithms.hierarchical.clusteringMethods;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Dennis Meyer
 */
public class ClusteringMethosFactory {

    public static IClusteringMethod createClusteringMethods(String methodClassName)
            throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException {

        Class methodClass = Class.forName(ClusteringMethosFactory.class.getPackage()
                .getName() + "." + methodClassName);
        Constructor methodConstructor = methodClass.getDeclaredConstructor();

        return (IClusteringMethod) methodConstructor.newInstance();
    }
}
