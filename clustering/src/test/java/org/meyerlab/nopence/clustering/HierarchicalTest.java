package org.meyerlab.nopence.clustering;

import com.google.common.base.Stopwatch;
import junit.framework.TestCase;
import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.dysc.Dysc;
import org.meyerlab.nopence.clustering.algorithms.hierarchical.HierarchicalClusterer;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.HammingDistance;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.IPerformanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.measures.performance.SilhouetteCoefficient;
import org.meyerlab.nopence.clustering.util.Cluster.Cluster;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;
import org.meyerlab.nopence.clustering.util.ClusteringMethod;
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

            DataStream dataStream = new DataStream("us-census.txt", 8000);
            IClusterer dyscClusterer = new HierarchicalClusterer(20,
                    ClusteringMethod.averageLink);
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
            System.out.println("Cluster count: " + cluster.size());
            System.out.println("Point count: " + cluster.numPoints());

            Map<Long, Point> pointMap = new HashMap<>();
            cluster.values().forEach(cl -> cl.getClusterPoints()
                    .forEach(point -> pointMap.put(point.Id, point)));

            System.out.println("Unique Points: " + pointMap.size());

            IPerformanceMeasure performanceMeasure = new
                    SilhouetteCoefficient(cluster, points, hammingDistance);

            double performance = performanceMeasure.estimatePerformance();

            System.out.println("Silhouette coeff: " + performance);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
