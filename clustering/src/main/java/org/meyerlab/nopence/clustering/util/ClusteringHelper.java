package org.meyerlab.nopence.clustering.util;

import org.meyerlab.nopence.clustering.algorithms.points.Point;

/**
 * @author Dennis Meyer
 */
public class ClusteringHelper {

    public static  String buildPointHash(Point first, Point second) {
        if (first.Id == second.Id) {
            System.out.println("Warning: Hash between points with the same " +
                    "id created.");
        }

        return first.Id + ";;;" + second.Id;
    }

    public static  String buildPointHash(long firstId, long secondId) {
        if (firstId == secondId) {
            System.out.println("Warning: Hash between points with the same " +
                    "id created.");
        }

        return firstId + ";;;" + secondId;
    }
}
