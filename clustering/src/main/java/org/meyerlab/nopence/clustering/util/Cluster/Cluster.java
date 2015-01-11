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
public abstract class Cluster {

    protected Map<Long, Point> _points;
    protected long _seedStateId;
    protected long _clusterId;


    public Cluster(Point first) {
        _points = HashLongObjMaps.newMutableMap();


        _seedStateId = first.Id;
        _points.put(_seedStateId, first);
    }

    public Point getClusterSeed() {
        return _points.get(_seedStateId);
    }

    public int numPoints() {
        return _points.size();
    }

    public abstract boolean addPoint(IDistanceMeasure distanceMeasure,
                                     Point point);

    public abstract void removePoint(long pointId,
                                     IDistanceMeasure distanceMeasure);

    public abstract void reassignClusterSeed(IDistanceMeasure distanceMeasure);

    public List<Point> getClusterPoints() {
        return _points.values()
                .stream()
                .collect(Collectors.toList());
    }

    public List<Long> getClusterPointIds() {
        return _points
                .values()
                .stream()
                .map(point -> point.Id)
                .collect(Collectors.toList());
    }

    public long getClusterId() {
        return _clusterId;
    }

    public void setClusterId(long clusterId) {
        _clusterId = clusterId;
    }

    public void addPoint(Point point) {
        _points.put(point.Id, point);
    }

    public boolean containsPoint(long pointId) {
        return _points.containsKey(pointId);
    }

    public boolean hasSeed() {
        return _points.containsKey(_seedStateId);
    }

    public void merge(Cluster cluster, IDistanceMeasure distanceMeasure) {
        _points.putAll(cluster._points);
    }

    @Override
    public int hashCode() {
        return _points.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj instanceof Cluster) {
            Cluster other = (Cluster) obj;
            return other._clusterId == _clusterId;
        }

        return false;
    }
}