package org.meyerlab.nopence.clustering.algorithms.dysc.ConcurencyWorkers;

import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.measures.distance.IDistanceMeasure;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.FixedCluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.Cluster.PendingCluster;
import org.meyerlab.nopence.util.ClusterHashMap;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class APrePendingWorker extends APreWorker implements Callable<Boolean> {

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

    public List<Point> getClusterPoints(long clusterId){
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

    public FixedCluster makeFixedCluster(long pendingClusterId) {
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

        return makeFixedCluster(pendingCluster.getClusterId());
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
                boolean tmp = cluster.addPoint(
                        _distanceMeasure, _epsilonDistance, _inputEvent.Point);

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