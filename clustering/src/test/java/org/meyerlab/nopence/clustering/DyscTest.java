package org.meyerlab.nopence.clustering;

import com.google.common.base.Stopwatch;
import junit.framework.TestCase;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.IPerformanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.SilhouetteCoefficient;
import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.Dysc;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.HammingDistance;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.DataStream;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Meyer
 */
public class DyscTest extends TestCase {

    private final int epsilonRange = 15;
    private final int maxPendingSize = 400;
    private final int maxClustersInThread = 100;
    private final int maxPointsInThread = 800;

    public DyscTest(String name) {
        super(name);
    }

    public void testDysc() {
        try {
            DataStream dataStream = new DataStream("us-census-little.csv", 1000);

            Stopwatch stopwatch = Stopwatch.createStarted();

            Dysc dyscClusterer = new Dysc(epsilonRange, maxPendingSize,
                    maxClustersInThread, maxPointsInThread, true);

            IDistanceMeasure hammingDistance = new HammingDistance(
                    dataStream.getDimInformation().copy());

            List<Point> points = new ArrayList<>();

            int counter = 0;
            while (dataStream.hasNext() && counter++ < 1000) {
                points.add(new Point(dataStream.next().Values, counter));
            }

            dyscClusterer.buildClusterer(points, hammingDistance);

            dyscClusterer.start();

            ClusterHashMap<Cluster> cluster = dyscClusterer.getCluster();

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

            System.out.println("Silhouette coeff: " + performance + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
