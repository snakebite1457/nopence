package org.meyerlab.nopence.clustering.online.dysc.Cluster;

import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import org.meyerlab.nopence.clustering.distanceMeasures.IDistanceMeasure;
import org.meyerlab.nopence.clustering.Points.Point;

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
        _seedStateId = first.Id;
        _points.put(_seedStateId, first);
        _points = HashLongObjMaps.newMutableMap();
    }

    public Point getClusterSeed() {
        return _points.get(_seedStateId);
    }

    public int numPoints() {
        return _points.size();
    }

    public abstract boolean addPoint(IDistanceMeasure distanceMeasure,
                                     double epsilonDistance,
                                     Point point);

    public abstract void removePoint(long pointId,
                                     IDistanceMeasure distanceMeasure);

    public List<Long> getClusterPoints() {
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