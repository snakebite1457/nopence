package org.meyerlab.nopence.clustering.algorithms.dysc;

import org.meyerlab.nopence.clustering.IClusterer;
import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.FixedCluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.PendingCluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurencyEvents.APreCallbackEvent;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurencyWorkers.APreFixedWorker;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurencyWorkers.APrePendingWorker;
import org.meyerlab.nopence.clustering.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurencyEvents.APreInputEvent;
import org.meyerlab.nopence.util.ClusterHashMap;
import org.meyerlab.nopence.util.WorkerHashMap;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Dennis Meyer
 */
public class Dysc implements IClusterer {

    private static Queue<Point> _points;
    private static IDistanceMeasure _distanceMeasure;

    private static int _maxClustersInWorker;
    private static int _maxPointsInWorker;

    private static WorkerHashMap<APreFixedWorker> _fixedWorkers;
    private static WorkerHashMap<APrePendingWorker> _pendingWorker;
    private static double _epsilonSeedRange;
    private static int _maxPendingClusterSize;
    private ExecutorService _executorService;

    public Dysc(double epsilonSeedRange,
                int maxPendingClusterSize,
                int maxClustersInWorker,
                int maxPointsInWorker) {

        _epsilonSeedRange = epsilonSeedRange;
        _maxPendingClusterSize = maxPendingClusterSize;

        _maxPointsInWorker = maxPointsInWorker;
        _maxClustersInWorker = maxClustersInWorker;

        _executorService = Executors.newCachedThreadPool();

        _fixedWorkers = new WorkerHashMap<>();
        _pendingWorker = new WorkerHashMap<>();
    }

    @Override
    public void buildClusterer(List<Point> points,
                               IDistanceMeasure distanceMeasure) {

        _points = new LinkedList<>(points);
        _distanceMeasure = distanceMeasure;
    }

    public ClusterHashMap<Cluster> getCluster() {
        ClusterHashMap<Cluster> clusters = new ClusterHashMap<>();

        _fixedWorkers.values()
                .stream()
                .forEach(worker
                        -> worker.getCluster()
                            .forEach(clusters::addCluster));
        return clusters;
    }

    @Override
    public void start() {

        Point firstPoint = _points.poll();
        createPendingWorker(firstPoint);

        // Iterate over all states and call the event handlers
        while (!_points.isEmpty()) {
            Point point = _points.poll();

            if (_fixedWorkers.size() > 0 && runFixedWorker(point)) {
                continue;
            }
            runPendingWorker(point);
        }

        System.out.println("Threads ready");

        // When ready make all pending clusters fixed
        while (!_pendingWorker.isEmpty()) {
            APrePendingWorker pendingWorker = _pendingWorker.entrySet()
                    .iterator().next().getValue();

            while (!pendingWorker.isEmpty()) {
                FixedCluster fixedCluster = pendingWorker.makeFixedCluster();
                if (fixedCluster == null) {
                    continue;
                }

                removePointsFromPendingClusters(fixedCluster);
                addFixedCluster(fixedCluster);
            }

            _pendingWorker.remove(pendingWorker.getWorkerId());
        }

        /*Map<Long, List<Long>> clusterMap = new HashMap<>();

        final long[] clusterCounter = {0};

        _fixedWorkers
                .values()
                .stream()
                .forEach(worker
                        -> worker.getCluster()
                        .forEach(cluster
                                -> clusterMap.put(clusterCounter[0]++,
                                cluster.getClusterPointIds())));

        return clusterMap;*/
    }

    private boolean runFixedWorker(Point point) {
        CountDownLatch doneSignal = new CountDownLatch(_fixedWorkers.size());

        List<Future<APreCallbackEvent>> fixedFutureEvents = new ArrayList<>();

        for (APreFixedWorker worker : _fixedWorkers.values()) {
            Point copiedState = point.copy();
            worker.setInputEvent(new APreInputEvent(copiedState), doneSignal);
            fixedFutureEvents.add(_executorService.submit(worker));
        }

        try {
            doneSignal.await(20, TimeUnit.SECONDS);

            double minDistance = Double.MAX_VALUE;
            long clusterId = -1;
            int workerId = -1;

            for (Future<APreCallbackEvent> future : fixedFutureEvents) {
                APreCallbackEvent callback = future.get();

                if (callback != null) {
                    if (callback.MinDistance < minDistance) {
                        minDistance = callback.MinDistance;
                        clusterId = callback.MinDistanceClusterId;
                        workerId = callback.WorkerId;
                    }
                }
            }

            if (workerId != -1) {
                _fixedWorkers.get(workerId).addPoint(clusterId, point);
                return true;
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean runPendingWorker(Point point) {
        CountDownLatch doneSignal = new CountDownLatch(_pendingWorker.size());

        List<Future<Boolean>> futurePointAdded = new ArrayList<>();
        for (int key : _pendingWorker.keySet()) {
            APrePendingWorker worker = _pendingWorker.get(key);

            Point copiedState = point.copy();

            worker.setInputEvent(new APreInputEvent(copiedState), doneSignal);

            // Add point to all cluster where the distance is smaller then
            // the epsilon distance
            futurePointAdded.add(_executorService.submit(worker));
        }


        try {
            doneSignal.await(20, TimeUnit.SECONDS);

            // Check if point is added to at least one cluster
            boolean added = false;
            for (Future<Boolean> pointAdded : futurePointAdded) {
                if (pointAdded.get()) {
                    added = true;
                    break;
                }
            }

            // If not then add new cluster
            if (!added) {
                APrePendingWorker pendingWorker = _pendingWorker.values()
                        .stream()
                        .filter(worker -> !worker.isLimitReached())
                        .findFirst()
                        .orElse(null);

                Point copiedState = point.copy();

                if (pendingWorker != null) {
                    pendingWorker.addCluster(
                            new PendingCluster(copiedState, _maxPendingClusterSize));
                } else {
                    createPendingWorker(copiedState);
                }
            }

            // Get clusters where the max pending cluster size are reached
            while (_pendingWorker.values()
                    .stream()
                    .filter(APrePendingWorker::hasFullPendingCluster)
                    .count() > 0) {

                APrePendingWorker penWorker = _pendingWorker.values()
                        .stream()
                        .filter(APrePendingWorker::hasFullPendingCluster)
                        .findFirst().get();

                FixedCluster fixedCluster = penWorker.makeFixedCluster(
                        penWorker.getFullPendingCluster());

                // Check if converted pending cluster still has points
                List<Point> remainingPoints =  penWorker
                        .getClusterPoints(fixedCluster.getOldPendingClusterId());

                if (remainingPoints != null) {
                    _points.addAll(remainingPoints);
                }

                removePointsFromPendingClusters(fixedCluster);
                addFixedCluster(fixedCluster);
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void createPendingWorker(Point initialState) {
        if (_pendingWorker.size() == 1) {
            recalculateWorkerSize();
        }

        APrePendingWorker pendingWorker = new APrePendingWorker(
                _distanceMeasure.copy(),
                _epsilonSeedRange,
                _maxClustersInWorker,
                _maxPointsInWorker);

        pendingWorker.addCluster(
                new PendingCluster(initialState.copy(), _maxPendingClusterSize));

        _pendingWorker.addWorker(pendingWorker);
    }

    private void createFixedWorker(FixedCluster initFixedCluster) {
        APreFixedWorker fixedWorker =
                new APreFixedWorker(
                        _distanceMeasure.copy(),
                        _epsilonSeedRange,
                        _maxClustersInWorker,
                        _maxPointsInWorker);

        fixedWorker.addCluster(initFixedCluster);
        _fixedWorkers.addWorker(fixedWorker);
    }

    private void addFixedCluster(FixedCluster fixedCluster) {

        if (_fixedWorkers.size() == 0 || _fixedWorkers.allWorkersFull()) {
            createFixedWorker(fixedCluster);
        } else {
            _fixedWorkers.getFreeWorker().addCluster(fixedCluster);
        }
    }

    private void recalculateWorkerSize() {
        APrePendingWorker firstWorker = _pendingWorker.getFirstWorker();

        double usedPointCapacity = (double)_maxPointsInWorker / (double)
                firstWorker.numPoints();
        double usedClusterCapacity = (double)_maxClustersInWorker / (double)
                firstWorker.numClusters();

        double absDif = Math.abs(usedPointCapacity - usedClusterCapacity);

        if (absDif < 0.35) {
            return;
        }

        if (usedPointCapacity - usedClusterCapacity > 0) {
            _maxClustersInWorker *= 1 - absDif * 0.3;
        } else {
            _maxPointsInWorker *= 1 - absDif * 0.3;
        }
    }

    private void removePointsFromPendingClusters(FixedCluster fixedCluster) {
        // TODO: parallelize this

        _pendingWorker.values()
                .forEach(worker -> worker.removePoints(fixedCluster));
    }
}
