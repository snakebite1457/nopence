package org.meyerlab.nopence.clustering.algorithms.dysc;

import org.meyerlab.nopence.clustering.IClusterer;
import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.FixedCluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.PendingCluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyEvents.APreCallbackEvent;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyEvents.APreInputEvent;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyWorkers.APreFixedWorker;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyWorkers.APrePendingWorker;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;
import org.meyerlab.nopence.clustering.util.WorkerHashMap;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class Dysc implements IClusterer {

    private Queue<Point> _points;
    private IDistanceMeasure _distanceMeasure;

    private int _maxClustersInWorker;
    private int _maxPointsInWorker;

    private WorkerHashMap<APreFixedWorker> _fixedWorkers;
    private WorkerHashMap<APrePendingWorker> _pendingWorker;
    private double _epsilonSeedRange;
    private int _maxPendingClusterSize;
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
        processPoints();

        System.out.println("Threads ready");

        Map<Long, Point> pointMap = new HashMap<>();
        _pendingWorker.values()
                .forEach(worker -> worker.getCluster()
                        .forEach(cl -> cl.getClusterPoints()
                                .forEach(point -> pointMap.put(point.Id, point))));

        try {
            // When ready make all pending clusters fixed
            while (!_pendingWorker.isEmpty()) {
                APrePendingWorker pendingWorker = _pendingWorker.entrySet()
                        .iterator().next().getValue();

                while (!pendingWorker.isEmpty()) {
                    makePendingClusterFix(pendingWorker, false);

                    if (!_points.isEmpty()) {
                        processPoints();
                    }
                }

                _pendingWorker.remove(pendingWorker.getWorkerId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processPoints() {
        // Iterate over all states and call the event handlers
        while (!_points.isEmpty()) {
            Point point = _points.poll();

            if (_fixedWorkers.size() > 0 && runFixedWorker(point)) {
                continue;
            }
            runPendingWorker(point);
        }
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

        List<Future<APreCallbackEvent>> futurePointAdded = new ArrayList<>();
        for (int key : _pendingWorker.keySet()) {
            APrePendingWorker worker = _pendingWorker.get(key);
            worker.setInputEvent(new APreInputEvent(point.copy()), doneSignal);

            // Add point to all cluster where the distance is smaller then
            // the epsilon distance
            futurePointAdded.add(_executorService.submit(worker));
        }

        try {
            doneSignal.await();

            // Check if point is added to at least one cluster
            boolean added = false;
            for (Future<APreCallbackEvent> pointAdded : futurePointAdded) {
                if (pointAdded.get().pointAdded) {
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
                            new PendingCluster(copiedState,
                                    _epsilonSeedRange,
                                    _maxPendingClusterSize));
                } else {
                    createPendingWorker(copiedState);
                }
            }

            // Get clusters where the max pending cluster size are reached
            APrePendingWorker penWorker;
            while ((penWorker = _pendingWorker.values()
                    .stream()
                    .filter(APrePendingWorker::hasFullPendingCluster)
                    .findFirst()
                    .orElse(null)) != null) {

                makePendingClusterFix(penWorker, true);
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void makePendingClusterFix(APrePendingWorker penWorker,
                                       boolean fullCluster)
            throws InterruptedException {

        FixedCluster fixedCluster;
        if (fullCluster) {
            fixedCluster = penWorker.makeFixedCluster(
                    penWorker.getFullPendingCluster());
        } else {
            fixedCluster = penWorker.makeFixedCluster();
        }

        // Check if converted pending cluster still has points
        List<Point> remainingPoints = fixedCluster.remainingPendingPoints();

        if (remainingPoints != null) {
            removePointsHelper(remainingPoints
                    .stream()
                    .collect(Collectors.toList()));

        }

        removePointsFromPendingClusters(fixedCluster.getClusterPointIds());
        addFixedCluster(fixedCluster);
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
                new PendingCluster(initialState.copy(),
                        _epsilonSeedRange,
                        _maxPendingClusterSize));

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

        double usedPointCapacity = (double) _maxPointsInWorker / (double)
                firstWorker.numPoints();
        double usedClusterCapacity = (double) _maxClustersInWorker / (double)
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

    private void removePointsFromPendingClusters(List<Long> points)
            throws InterruptedException {

        CountDownLatch doneSignal = new CountDownLatch(_pendingWorker.size());

        List<Future<APreCallbackEvent>> callFutures = new ArrayList<>();
        for (APrePendingWorker worker : _pendingWorker.values()) {
            APreInputEvent inputEvent = new APreInputEvent(points);
            worker.setInputEvent(inputEvent, doneSignal);
            callFutures.add(_executorService.submit(worker));
        }

        doneSignal.await();

        for (Future<APreCallbackEvent> future : callFutures) {
            try {
                if (future.get().PointsWithoutSeed != null
                        && !future.get().PointsWithoutSeed.isEmpty()) {

                    removePointsHelper(future.get().PointsWithoutSeed);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void removePointsHelper(List<Point> points) {
        points
            .stream()
            .filter(point
                    -> _pendingWorker.values()
                    .stream()
                    .filter(worker -> worker.containsPoint(point.Id))
                    .count() == 0)
            .forEach(point -> {
                _pendingWorker.values()
                        .forEach(worker
                                -> worker.removeOutsiderPoint(point.Id));
                _points.add(point);
            });
    }
}
