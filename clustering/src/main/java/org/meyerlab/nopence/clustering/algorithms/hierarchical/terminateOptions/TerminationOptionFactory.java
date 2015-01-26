package org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions;

/**
 * @author Dennis Meyer
 */
public class TerminationOptionFactory {

    public static ITerminateOption createTerminationOption(
            String terminationClassName, Number terminationValue)
            throws ClassNotFoundException {

        switch (terminationClassName) {
            case "ClusterSizeTerminationOption":
                return new ClusterSizeTerminateOption((Integer) terminationValue);
            case "MinDistanceTerminationOption":
                return new MinDistanceTerminationOption((Double) terminationValue);
            default:
                throw new ClassNotFoundException();
        }
    }
}
