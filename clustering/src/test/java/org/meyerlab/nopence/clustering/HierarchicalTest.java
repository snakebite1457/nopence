package org.meyerlab.nopence.clustering;

import com.google.common.base.Stopwatch;
import junit.framework.TestCase;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.clusteringMethods.SingleLinkageClusteringMethod;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions.MinDistanceTerminationOption;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.terminateOptions.ITerminateOption;
import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.HierarchicalClusterer;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.HammingDistance;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.IPerformanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.SilhouetteCoefficient;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;
import org.meyerlab.nopence.clustering.util.DataStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Meyer
 */
public class HierarchicalTest extends TestCase {

    public HierarchicalTest(String name) {
        super(name);
    }

    public void testHierarchical() {
        try {

            Stopwatch stopwatch = Stopwatch.createStarted();

            DataStream dataStream = new DataStream("us-census.txt", 1200);

            IDistanceMeasure hammingDistance = new HammingDistance(
                    dataStream.getDimInformation().copy());

            ITerminateOption terminateOption =
                    new MinDistanceTerminationOption(9);

            IClusterer hierarchicalClusterer = new HierarchicalClusterer
                    (terminateOption, new SingleLinkageClusteringMethod());


            List<Point> points = new ArrayList<>();

            int counter = 0;
            while (dataStream.hasNext() && counter++ < 1000) {
                points.add(new Point(dataStream.next().Values, counter));
            }

            hierarchicalClusterer.buildClusterer(points, hammingDistance);

            hierarchicalClusterer.start();

            ClusterHashMap<Cluster> cluster = hierarchicalClusterer.getCluster();

            System.out.println("Time elapsed: : "
                    + stopwatch.stop().elapsed(TimeUnit.SECONDS));
            System.out.println("cluster count: " + cluster.size());
            System.out.println("Point count: " + cluster.numPoints());

            Map<Long, Point> pointMap = new HashMap<>();
            cluster.values().forEach(cl -> cl.getClusterPoints()
                    .forEach(point -> pointMap.put(point.Id, point)));

            System.out.println("Unique points: " + pointMap.size());

            IPerformanceMeasure performanceMeasure = new
                    SilhouetteCoefficient(cluster, points, hammingDistance);

            double performance = performanceMeasure.estimatePerformance();

            System.out.println("Silhouette coeff: " + performance);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
