package org.meyerlab.nopence.clustering.online.dysc.ConcurencyWorkers;

import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;
import org.meyerlab.nopence.clustering.online.dysc.Cluster.Cluster;
import org.meyerlab.nopence.clustering.online.dysc.ConcurencyEvents.APreInputEvent;

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

    public void updateLimitReached() {
        _clusterLimitReached = numClusters() > _maxClusters;
    }
}