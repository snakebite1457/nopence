package org.meyerlab.nopence.clustering.online.dysc;

import org.meyerlab.nopence.clustering.DimensionInformation;
import org.meyerlab.nopence.clustering.IClusterer;
import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;
import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.FixedCluster;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.PendingCluster;
import org.meyerlab.nopence.clustering.online.dysc.ConcurencyEvents.APreCallbackEvent;
import org.meyerlab.nopence.clustering.online.dysc.ConcurencyEvents.APreInputEvent;
import org.meyerlab.nopence.clustering.online.dysc.ConcurencyWorkers.APreFixedWorker;
import org.meyerlab.nopence.clustering.online.dysc.ConcurencyWorkers.APrePendingWorker;
import org.meyerlab.nopence.util.WorkerHashMap;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Dennis Meyer
 */
public class Dysc implements IClusterer {

    private static Queue<Point> _points;
    private static DimensionInformation _dimensionInformation;
    private static IDistanceMeasure _distanceMeasure;

    private static WorkerHashMap<APreFixedWorker> _fixedWorker;
    private static WorkerHashMap<APrePendingWorker> _pendingWorker;

    private ExecutorService _executorService;

    private static double _epsilonSeedRange;
    private static int _maxPendingClusterSize;

    private int counter = 0;

    public Dysc(double epsilonSeedRange,
                int maxPendingClusterSize) {

        _epsilonSeedRange =  epsilonSeedRange;
        _maxPendingClusterSize = maxPendingClusterSize;

        _executorService = Executors.newCachedThreadPool();

        _fixedWorker = new WorkerHashMap<>();
        _pendingWorker = new WorkerHashMap<>();
    }

    @Override
    public void buildClusterer(List<Point> points,
                               DimensionInformation dimensionInformation,
                               IDistanceMeasure distanceMeasure) {

        _points = new LinkedList<>(points);
        _dimensionInformation = dimensionInformation;
        _distanceMeasure = distanceMeasure;
    }

    @Override
    public Map<Long, List<Long>> start() {

        Point firstPoint = _points.poll();
        createPendingWorker(firstPoint);

        // Iterate over all states and call the event handlers
        while (!_points.isEmpty()) {
            Point point = _points.poll();

            if (_fixedWorker.size() > 0 && runFixedWorker(point)) {
                continue;
            } else {
                runPendingWorker(point);
            }

            counter++;
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

        Map<Long, List<Long>> clusterMap = new HashMap<>();

        final long[] clusterCounter = {0};

        _fixedWorker
                .values()
                .stream()
                .forEach(worker
                        -> worker.getCluster()
                            .forEach(cluster
                                    -> clusterMap.put(clusterCounter[0]++,
                                    cluster.getClusterPoints())));

        return clusterMap;
    }

    private boolean runFixedWorker(Point point) {
        CountDownLatch doneSignal = new CountDownLatch(_fixedWorker.size());

        List<Future<APreCallbackEvent>> fixedFutureEvents = new ArrayList<>();

        for (APreFixedWorker worker : _fixedWorker.values()) {
            Point copiedState = point.copy();
            worker.setInputEvent(new APreInputEvent(copiedState), doneSignal);
            fixedFutureEvents.add(_executorService.submit(worker));
        }

        try {
            doneSignal.await();

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
                _fixedWorker.get(workerId).addPoint(clusterId, point);
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
                APrePendingWorker pendingWorker =_pendingWorker.values()
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

                FixedCluster fixedCluster = penWorker.makePendingCluster(
                        penWorker.getFullPendingCluster());

                removePointsFromPendingClusters(fixedCluster);
                addFixedCluster(fixedCluster);
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void createPendingWorker(Point initialState) {
        APrePendingWorker pendingWorker = new APrePendingWorker(
                _distanceMeasure.copy(),
                _epsilonSeedRange, 100, 5000);

        pendingWorker.addCluster(new PendingCluster(initialState
                , _maxPendingClusterSize));

        _pendingWorker.addWorker(pendingWorker);
    }

    private void addFixedCluster(FixedCluster cluster) {

        if (_fixedWorker.size() == 0 || _fixedWorker.allWorkersFull()) {

            APreFixedWorker fixedWorker = new APreFixedWorker(
                    _distanceMeasure.copy(),
                    _epsilonSeedRange, 1000);

            fixedWorker.addCluster(cluster);
            _fixedWorker.addWorker(fixedWorker);

        } else {
            _fixedWorker.getFreeWorker().addCluster(cluster);
        }
    }

    private void removePointsFromPendingClusters(FixedCluster fixedCluster) {
        _pendingWorker.values()
                .forEach(worker -> worker.removePoints(fixedCluster));
    }
}
