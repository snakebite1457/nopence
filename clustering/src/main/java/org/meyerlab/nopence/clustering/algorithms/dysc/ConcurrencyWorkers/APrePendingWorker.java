package org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyWorkers;

import org.meyerlab.nopence.clustering.algorithms.points.Point;
import org.meyerlab.nopence.clustering.util.cluster.Cluster;
import org.meyerlab.nopence.clustering.util.cluster.FixedCluster;
import org.meyerlab.nopence.clustering.util.cluster.PendingCluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyEvents.APreCallbackEvent;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.util.ClusterHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class APrePendingWorker extends APreWorker
        implements Callable<APreCallbackEvent> {

    private ClusterHashMap<PendingCluster> _clusterMap;

    public APrePendingWorker(IDistanceMeasure distanceMeasure,
                             double epsilonDistance,
                             int initMaxClusters,
                             int initMaxPoints) {
        super(distanceMeasure, epsilonDistance,
                initMaxClusters, initMaxPoints);

        _clusterMap = new ClusterHashMap<>();
    }

    @Override
    public int numPoints() {
        return _clusterMap.numPoints();
    }

    @Override
    public int numClusters() {
        return _clusterMap.size();
    }

    @Override
    public List<Cluster> getCluster() {
        return _clusterMap
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    public List<Point> getClusterPoints(long clusterId) {
        if (_clusterMap.containsKey(clusterId)) {
            return _clusterMap.get(clusterId).getClusterPoints();
        }

        return null;
    }

    @Override
    public void addCluster(Cluster cluster) {
        if (cluster instanceof PendingCluster) {
            _clusterMap.addCluster((PendingCluster) cluster);
            updateLimitReached();
        }
    }

    @Override
    public void addPoint(long clusterId, Point point) {
        _clusterMap.get(clusterId).addPoint(point);
        updateLimitReached();
    }

    @Override
    public boolean containsPoint(long pointId) {
        return _clusterMap.containsPoint(pointId);
    }

    private List<Point> removePoints(List<Long> pointIds) {
        _clusterMap.values()
                .forEach(cluster ->
                        cluster.removePoints(pointIds, _distanceMeasure));

        List<Point> pointsWithoutSeed = new ArrayList<>();
        _clusterMap.values()
                .stream()
                .filter(cluster -> !cluster.hasSeed())
                .forEach(cluster
                        -> pointsWithoutSeed.addAll(cluster.getClusterPoints()));

        // Check empty cluster and remove
        List<Long> emptyClusters = _clusterMap.values()
                .stream()
                .filter(cluster
                        -> cluster.numPoints() == 0 || !cluster.hasSeed())
                .map(Cluster::getClusterId)
                .collect(Collectors.toList());

        emptyClusters.forEach(_clusterMap::remove);

        updateLimitReached();

        return pointsWithoutSeed;
    }

    public void removeOutsiderPoint(long pointId) {
        _clusterMap.values()
                .forEach(cluster -> cluster.removeOutsiderPoint(pointId));
    }

    public FixedCluster makeFixedCluster(long pendingClusterId) {
        FixedCluster fixedCluster = _clusterMap.get(pendingClusterId)
                .transform(_distanceMeasure);

        _clusterMap.remove(pendingClusterId);
        updateLimitReached();

        return fixedCluster;
    }

    public FixedCluster makeFixedCluster() {
        if (_clusterMap.isEmpty()) {
            return null;
        }

        return makeFixedCluster(_clusterMap.getCluster().getClusterId());
    }

    public boolean isEmpty() {
        return _clusterMap.isEmpty();
    }

    public long getFullPendingCluster() {
        return _clusterMap.values()
                .stream()
                .filter(PendingCluster::maxPendingSizeReached)
                .map(Cluster::getClusterId)
                .findFirst().orElse(-1l);
    }

    public boolean hasFullPendingCluster() {
        return _clusterMap.values()
                .stream()
                .filter(PendingCluster::maxPendingSizeReached)
                .count() > 0;
    }

    @Override
    public APreCallbackEvent call() throws Exception {

        APreCallbackEvent callbackEvent = new APreCallbackEvent();

        if (_inputEvent.removePoints && _inputEvent.PointIdsToBeRemoved != null) {
            callbackEvent.PointsWithoutSeed =
                    removePoints(_inputEvent.PointIdsToBeRemoved);
        } else {
            callbackEvent.pointAdded = addPointConcurrency();
        }

        _doneSignal.countDown();

        return callbackEvent;
    }

    private Boolean addPointConcurrency() {
        boolean pointAdded = false;

        try {
            for (PendingCluster cluster : _clusterMap.values()) {
                boolean tmp = cluster.addPoint(
                        _distanceMeasure, _inputEvent.Point);

                if (!pointAdded && tmp) {
                    pointAdded = true;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return pointAdded;
    }
}