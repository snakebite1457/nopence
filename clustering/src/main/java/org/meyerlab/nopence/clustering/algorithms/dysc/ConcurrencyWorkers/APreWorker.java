package org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyWorkers;

import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.util.Cluster.Cluster;
import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurrencyEvents.APreInputEvent;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Dennis Meyer
 */
public abstract class APreWorker {

    protected IDistanceMeasure _distanceMeasure;
    protected int _workerId;

    protected int _maxClusters;
    protected int _maxPoints;

    protected double _epsilonDistance;
    protected boolean _clusterLimitReached;
    protected APreInputEvent _inputEvent;
    protected CountDownLatch _doneSignal;

    public APreWorker(IDistanceMeasure distanceMeasure,
                      double epsilonDistance,
                      int initMaxClusters,
                      int initMaxPoints) {

        _epsilonDistance = epsilonDistance;
        _maxClusters = initMaxClusters;
        _maxPoints = initMaxPoints;
        _distanceMeasure = distanceMeasure;
        _clusterLimitReached = false;
    }

    public int getWorkerId() {
        return _workerId;
    }

    public void setWorkerId(int workerId) {
        _workerId = workerId;
    }

    public void setInputEvent(APreInputEvent inputEvent,
                              CountDownLatch doneSignal) {
        _inputEvent = inputEvent;
        _doneSignal = doneSignal;
    }

    public boolean isLimitReached() {
        return _clusterLimitReached;
    }

    public abstract int numPoints();

    public abstract int numClusters();

    public abstract List<Cluster> getCluster();

    public abstract void addCluster(Cluster cluster);

    public abstract void addPoint(long clusterId, Point point);

    public abstract boolean containsPoint(long pointId);

    public void updateLimitReached() {
        _clusterLimitReached = numClusters() > _maxClusters;
    }
}