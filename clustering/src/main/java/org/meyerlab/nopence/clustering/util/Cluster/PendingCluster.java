package org.meyerlab.nopence.clustering.util.Cluster;

import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import org.meyerlab.nopence.clustering.algorithms.Points.Point;
import org.meyerlab.nopence.clustering.algorithms.measures.distance.IDistanceMeasure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Dennis Meyer
 */
public class PendingCluster extends Cluster {

    public Map<Long, Point> _recentOutsiders;

    private int _maxPendingSize;
    protected double _epsilonDistance;


    public PendingCluster(Point first,
                          double epsilonDistance,
                          int maxPendingSize) {
        super(first);

        _epsilonDistance = epsilonDistance;
        _recentOutsiders = HashLongObjMaps.newMutableMap();
        _maxPendingSize = maxPendingSize;
    }

    public FixedCluster transform(IDistanceMeasure distanceMeasure) {

        // calc new seed
        reassignClusterSeed(distanceMeasure);

        FixedCluster fixedCluster = new FixedCluster(_points.get
                (_seedStateId), _epsilonDistance);

        _points.values()
                .stream()
                .filter(point -> distanceMeasure.computeDistance(fixedCluster
                        .getClusterSeed(), point) < _epsilonDistance)
                .forEach(point -> fixedCluster._points.put(point.Id, point));

        _recentOutsiders.values()
                .stream()
                .filter(point -> distanceMeasure.computeDistance(fixedCluster
                        .getClusterSeed(), point) < _epsilonDistance)
                .forEach(point -> fixedCluster._points.put(point.Id, point));

        // Add remaining points which was not inserted to fixed cluster.
        if (!_points.isEmpty()) {
            fixedCluster.setRemainingPendingPoints(
                    _points.values()
                            .stream()
                            .filter(point -> !fixedCluster.containsPoint(point.Id))
                            .collect(Collectors.toList()));
        }

        return fixedCluster;
    }

    @Override
    public void reassignClusterSeed(IDistanceMeasure distanceMeasure) {
        double minDistance = Double.MAX_VALUE;
        long seedId = _seedStateId;

        for (Point point : _points.values()) {
            double distance = _points.values()
                    .stream()
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
    }

    public void removePoints(List<Long> pointIds,
                             IDistanceMeasure distanceMeasure) {
        pointIds.forEach(pointId -> removePoint(pointId, distanceMeasure));
    }

    public void removeOutsiderPoint(long pointId) {
        _recentOutsiders.remove(pointId);
    }

    @Override
    public boolean addPoint(IDistanceMeasure distanceFunction,
                            Point point) {

        if (!_points.containsKey(_seedStateId)) {
            reassignClusterSeed(distanceFunction);
        }

        double distanceToSeed = distanceFunction
                .computeDistance(point, getClusterSeed());

        if (distanceToSeed < _epsilonDistance) {
            _points.put(point.Id, point);
            return true;
        } else if (distanceToSeed < _epsilonDistance * 2) {
            _recentOutsiders.put(point.Id, point);
        }

        return false;
    }

    public boolean maxPendingSizeReached() {
        return _points.size() >= _maxPendingSize;
    }
}