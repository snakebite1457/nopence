package org.meyerlab.nopence.clustering.algorithms.dysc.Cluster;

import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import org.meyerlab.nopence.clustering.Points.Point;
import org.meyerlab.nopence.clustering.measures.distance.IDistanceMeasure;

import java.util.Map;

/**
 * @author Dennis Meyer
 */
public class PendingCluster extends Cluster {

    public Map<Long, Point> _recentOutsiders;
    private int _maxPendingSize;

    public PendingCluster(Point first,
                          int maxPendingSize) {
        super(first);

        _recentOutsiders = HashLongObjMaps.newMutableMap();
        _maxPendingSize = maxPendingSize;
    }

    public FixedCluster transform(IDistanceMeasure distanceMeasure,
                                  double epsilonDistance) {

        // calc new seed
        reassignClusterSeed(distanceMeasure);

        FixedCluster fixedCluster = new FixedCluster(_points.get
                (_seedStateId), _clusterId);
        _points.values()
                .stream()
                .filter(point -> distanceMeasure.computeDistance(fixedCluster
                        .getClusterSeed(), point) < epsilonDistance)
                .forEach(point -> fixedCluster._points.put(point.Id, point));

        // Remove points
        removePoints(fixedCluster, distanceMeasure);

        _recentOutsiders.values()
                .stream()
                .filter(point -> distanceMeasure.computeDistance(fixedCluster
                        .getClusterSeed(), point) < epsilonDistance)
                .forEach(point -> fixedCluster._points.put(point.Id, point));

        return fixedCluster;
    }

    @Override
    public void reassignClusterSeed(IDistanceMeasure distanceMeasure) {
        double minDistance = Double.MAX_VALUE;
        long seedId = 0;

        for (Point point : _points.values()) {
            double distance = _points.values()
                    .stream()
                    .filter(p -> p.Id != point.Id)
                    .mapToDouble(p -> distanceMeasure.computeDistance(point, p))
                    .sum();

            if (distance < minDistance) {
                minDistance = distance;
                seedId = point.Id;
            }
        }
        _seedStateId = seedId;
    }

    @Override
    public void removePoint(long pointId,
                            IDistanceMeasure distanceMeasure) {

        if (_points.containsKey(pointId)) {
            _points.remove(pointId);
        }

        if (_recentOutsiders.containsKey(pointId)) {
            _recentOutsiders.remove(pointId);
        }

        if (pointId == _seedStateId) {
            reassignClusterSeed(distanceMeasure);
        }
    }

    public void removePoints(FixedCluster fixedCluster,
                             IDistanceMeasure distanceMeasure) {
        fixedCluster._points.keySet()
                .forEach(pointId -> removePoint(pointId, distanceMeasure));
    }

    @Override
    public boolean addPoint(IDistanceMeasure distanceFunction,
                            double epsilonDistance,
                            Point point) {

        double distanceToSeed = distanceFunction
                .computeDistance(point, getClusterSeed());

        if (distanceToSeed < epsilonDistance) {
            _points.put(point.Id, point);
            return true;
        } else if (distanceToSeed < epsilonDistance * 2) {
            _recentOutsiders.put(point.Id, point);
        }

        return false;
    }

    public boolean maxPendingSizeReached() {
        return _points.size() >= _maxPendingSize;
    }
}