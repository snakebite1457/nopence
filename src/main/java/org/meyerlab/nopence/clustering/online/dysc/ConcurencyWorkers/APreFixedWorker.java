package org.meyerlab.nopence.clustering.online.dysc.ConcurencyWorkers;

import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;
import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.FixedCluster;
import org.meyerlab.nopence.clustering.online.dysc.ConcurencyEvents.APreCallbackEvent;
import org.meyerlab.nopence.util.ClusterHashMap;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class APreFixedWorker extends APreWorker
        implements Callable<APreCallbackEvent> {

    private ClusterHashMap<FixedCluster> _clusterMap;

    public APreFixedWorker(IDistanceMeasure distanceMeasure,
                           double epsilonDistance,
                           int maxClusterSize) {
        super(distanceMeasure, epsilonDistance, maxClusterSize);

        _clusterMap = new ClusterHashMap<>();
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
        if (cluster instanceof FixedCluster) {
            _clusterMap.addCluster((FixedCluster) cluster);
            updateLimitReached();
        }
    }

    @Override
    public void addPoint(long clusterId, Point point) {
        _clusterMap.get(clusterId).addPoint(point);
        updateLimitReached();
    }

    @Override
    public void updateLimitReached() {

        // Save info if limit is reached for performance reasons
        _clusterLimitReached = _clusterMap.size() > _maxClusterSize;
    }

    @Override
    public APreCallbackEvent call() throws Exception {
        APreCallbackEvent event =
                calculateMinDistance(_inputEvent.Point);

        _doneSignal.countDown();

        return event;
    }

    public ClusterHashMap<FixedCluster> getClusterMap() {
        return _clusterMap;
    }

    private APreCallbackEvent calculateMinDistance(Point state) {
        APreCallbackEvent callBackEvent = new APreCallbackEvent();

        ClusterDistance result  =_clusterMap.values()
                .stream()
                .map(cluster ->
                        new ClusterDistance(
                                _distanceMeasure.computeDistance(
                                        cluster.getClusterSeed(), state),
                                cluster))
                .filter(clusterDistance -> clusterDistance.Distance <= _epsilonDistance)
                .min(new Comparator<ClusterDistance>() {
                    @Override
                    public int compare(ClusterDistance o1, ClusterDistance o2) {
                        return Double.compare(o1.Distance, o2.Distance);
                    }})
                .orElse(null);

        if (result == null) {
            return null;
        }

        callBackEvent.MinDistance = result.Distance;
        callBackEvent.MinDistanceClusterId = result.Cluster.getClusterId();
        callBackEvent.WorkerId = _workerId;

        return callBackEvent;
    }

    private static class ClusterDistance {
        public double Distance;
        public Cluster Cluster;

        public ClusterDistance(double distance, Cluster cluster) {
            Distance = distance;
            Cluster = cluster;
        }
    }
}