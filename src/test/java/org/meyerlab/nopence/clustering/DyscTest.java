package org.meyerlab.nopence.clustering;

import com.google.common.base.Stopwatch;
import junit.framework.TestCase;
import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.measures.distance.HammingDistance;
import org.meyerlab.nopence.clustering.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.measures.performance.IPerformanceMeasure;
import org.meyerlab.nopence.clustering.measures.performance.SilhouetteCoefficient;
import org.meyerlab.nopence.clustering.algorithms.dysc.Dysc;
import org.meyerlab.nopence.clustering.util.DataStream;
import org.meyerlab.nopence.util.ClusterHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Dennis Meyer
 */
public class DyscTest extends TestCase {

    public DyscTest(String name) {
        super(name);
    }

    public void testDysc() {
        try {

            Stopwatch stopwatch = Stopwatch.createStarted();

            DataStream dataStream = new DataStream("us-census.txt", 500000);
            Dysc dyscClusterer = new Dysc(36, 250, 100, 300);
            IDistanceMeasure hammingDistance = new HammingDistance(
                    dataStream.getDimInformation().copy());

            List<Point> points = new ArrayList<>();

            int counter = 0;
            while (dataStream.hasNext() && counter++ < 50000) {
                points.add(new Point(dataStream.next().Values, counter));
            }

            dyscClusterer.buildClusterer(points, hammingDistance);

            dyscClusterer.start();

            ClusterHashMap<Cluster> cluster = dyscClusterer.getCluster();

            System.out.println("Time elapsed: : "
                    + stopwatch.stop().elapsed(TimeUnit.SECONDS));
            System.out.println("Cluster count: " + cluster.size());
            System.out.println("Point count: " + cluster.numPoints());

            IPerformanceMeasure performanceMeasure = new
                    SilhouetteCoefficient(cluster, points, hammingDistance);

            double performance = performanceMeasure.estimatePerformance();

            System.out.println("Silhouette coeff: " + performance);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
