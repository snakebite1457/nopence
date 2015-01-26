package org.meyerlab.nopence.clustering.algorithms.measures.performance;

import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Dennis Meyer
 */
public class SilhouetteCoefficient implements IPerformanceMeasure {

    private IDistanceMeasure _distanceMeasure;
    private ClusterHashMap<Cluster> _clusters;
    private List<Point> _points;

    private Map<Long, Point> _secondClusterSeed;


    public SilhouetteCoefficient(ClusterHashMap<Cluster> clusters,
                                 List<Point> points,
                                 IDistanceMeasure distanceMeasure) {
        _clusters = clusters;
        _points = new ArrayList<>(points);
        _distanceMeasure = distanceMeasure;
        _secondClusterSeed = HashLongObjMaps.newMutableMap();
    }

    @Override
    public double estimatePerformance() {

        /*List<ReassignWorker> worker = new ArrayList<>();

        CountDownLatch doneSignal = new CountDownLatch(_clusters.size());

        _clusters.values()
                .stream()
                .forEach(cluster -> {
                    ReassignWorker reassignWorker = new ReassignWorker
                            (doneSignal, cluster, _distanceMeasure.copy());

                    worker.add(reassignWorker);
                });

        ExecutorService executor = Executors.newCachedThreadPool();
        worker
                .stream()
                .forEach(executor::submit);

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        estimateSecondNearestClusterSeed();

        double value = 0;
        long counter = 0;

        for (Cluster cluster : _clusters.values()) {
            Point clusterSeed = cluster.getClusterSeed();

            for (Point point : cluster.getClusterPoints()) {

                double distanceToSeed = _distanceMeasure.computeDistance
                        (clusterSeed, point);

                double distanceToSecondSeed = _distanceMeasure
                        .computeDistance(_secondClusterSeed.get(point.Id), point);

                value += (distanceToSecondSeed - distanceToSeed) / Math.max
                        (distanceToSecondSeed, distanceToSeed);

                counter++;
            }
        }

        return value / counter;
    }

    private void estimateSecondNearestClusterSeed() {

        for (Point point : _points) {
            final double[] nearestClusterSeedDistance = {Double.MAX_VALUE};
            final double[] secondDistance = {Double.MAX_VALUE};

            final Point[] nearestClusterSeed = {null};
            final Point[] secondNearestClusterSeed = {null};

            _clusters.values()
                    .forEach(cluster
                            -> {
                        Point clusterSeed = cluster.getClusterSeed();

                        double distance = _distanceMeasure.computeDistance
                                (clusterSeed, point);

                        if (distance < nearestClusterSeedDistance[0]) {

                            if (nearestClusterSeed[0] != null) {
                                secondDistance[0] = nearestClusterSeedDistance[0];
                                secondNearestClusterSeed[0] =
                                        nearestClusterSeed[0].copy();
                            }

                            nearestClusterSeedDistance[0] = distance;
                            nearestClusterSeed[0] = clusterSeed.copy();
                        } else if (distance < secondDistance[0]
                                && distance > nearestClusterSeedDistance[0]) {
                            secondDistance[0] = distance;
                            secondNearestClusterSeed[0] = clusterSeed.copy();
                        }
                    });

            _secondClusterSeed.put(point.Id, secondNearestClusterSeed[0]);
        }

    }

    private static class ReassignWorker implements Runnable {

        private CountDownLatch _doneSignal;
        private Cluster _cluster;
        private IDistanceMeasure _distanceMeasure;

        public ReassignWorker(CountDownLatch doneSignal,
                              Cluster cluster,
                              IDistanceMeasure distanceMeasure) {
            _cluster = cluster;
            _doneSignal = doneSignal;
            _distanceMeasure = distanceMeasure;
        }

        @Override
        public void run() {
            _cluster.reassignClusterSeed(_distanceMeasure);
            _doneSignal.countDown();
        }
    }

}
