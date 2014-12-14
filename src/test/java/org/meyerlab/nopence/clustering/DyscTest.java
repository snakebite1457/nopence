package org.meyerlab.nopence.clustering;

import com.google.common.base.Stopwatch;
import junit.framework.TestCase;
import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.distanceMeasures.HammingDistance;
import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;
import org.meyerlab.nopence.clustering.online.dysc.Dysc;
import org.meyerlab.nopence.clustering.util.DataStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

            DataStream dataStream = new DataStream("us-census.txt", 600000);
            IClusterer dyscClusterer = new Dysc(20, 2500, 100, 10000);
            IDistanceMeasure hammingDistance = new HammingDistance(
                    dataStream.getDimInformation().copy());

            List<Point> points = new ArrayList<>();

            int counter = 0;
            while (dataStream.hasNext() && counter++ < 500000) {
                points.add(new Point(dataStream.next().Values, counter));
            }

            dyscClusterer.buildClusterer(points, hammingDistance);

            Map<Long, List<Long>> cluster = dyscClusterer.start();

            System.out.println("Time elapsed: : "
                    + stopwatch.stop().elapsed(TimeUnit.SECONDS));
            System.out.println("Cluster count: " + cluster.size());

            int a = 0;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
