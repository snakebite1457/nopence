package org.meyerlab.nopence.clustering.online.dysc.ConcurencyWorkers;

import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;
import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.FixedCluster;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.PendingCluster;
import org.meyerlab.nopence.util.ClusterHashMap;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class APrePendingWorker extends APreWorker implements Callable<Boolean> {

    private ClusterHashMap<PendingCluster> _clusterMap;
    private int _maxPointSize;

    public APrePendingWorker(IDistanceMeasure distanceMeasure,
                             double epsilonDistance,
                             int maxClusterSize,
                             int maxPointSize) {
        super(distanceMeasure, epsilonDistance, maxClusterSize);

        _clusterMap = new ClusterHashMap<>();
        _maxPointSize = maxPointSize;
    }

    @Override
    public List<Cluster> getCluster() {
        return _clusterMap
                .values()
                .stream()
                .collect(Collectors.toList());
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

    public void removePoints(FixedCluster fixedCluster) {
        _clusterMap.values()
                .forEach(cluster ->
                        cluster.removePoints(fixedCluster, _distanceMeasure));

        // Check empty cluster and remove
        List<Long> emptyClusters = _clusterMap.values()
                .stream()
                .filter(cluster -> cluster.numPoints() == 0)
                .map(Cluster::getClusterId)
                .collect(Collectors.toList());

        emptyClusters.forEach(_clusterMap::remove);

        updateLimitReached();
    }

    @Override
    public void updateLimitReached() {

        int pointCount = _clusterMap.values()
                .stream()
                .mapToInt(Cluster::numPoints)
                .sum();

        // Save info if limit is reached for performance reasons
        _clusterLimitReached = _clusterMap.size() > _maxClusterSize
                || pointCount > _maxPointSize;
    }

    public FixedCluster makePendingCluster(long pendingClusterId) {
        FixedCluster fixedCluster = _clusterMap.get(pendingClusterId)
                .transform(_distanceMeasure, _epsilonDistance);

        _clusterMap.remove(pendingClusterId);
        updateLimitReached();

        return fixedCluster;
    }

    public FixedCluster makeFixedCluster() {
        if (_clusterMap.isEmpty()) {
            return null;
        }

        PendingCluster pendingCluster = _clusterMap.getCluster();
        while (pendingCluster != null && pendingCluster.numPoints() == 0) {
            _clusterMap.remove(pendingCluster);
            pendingCluster = _clusterMap.getCluster();
        }

        if (pendingCluster == null) {
            return null;
        }

        return makePendingCluster(pendingCluster.getClusterId());
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
    public Boolean call() throws Exception {
        boolean pointAdded = false;

        try {
            for (PendingCluster cluster : _clusterMap.values()) {
                boolean tmp = cluster.addPoint(_distanceMeasure,
                        _epsilonDistance, _inputEvent.Point);

                if (!pointAdded && tmp) {
                    pointAdded = true;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        _doneSignal.countDown();

        return pointAdded;
    }
}