package org.meyerlab.nopence.util;

import org.meyerlab.nopence.clustering.algorithms.dysc.ConcurencyWorkers.APreWorker;

import java.util.HashMap;

/**
 * @author Dennis Meyer
 */
public class WorkerHashMap<E extends APreWorker> extends HashMap<Integer, E> {

    private int addedCluster = 0;

    public void addWorker(E worker) {
        worker.setWorkerId(addedCluster++);
        this.put(worker.getWorkerId(), worker);
    }


    public boolean allWorkersFull() {
        return this.values()
                .stream()
                .filter(worker -> !worker.isLimitReached())
                .count() == 0;
    }

    public E getFreeWorker() {
        return this.values()
                .stream()
                .filter(worker -> !worker.isLimitReached())
                .findFirst()
                .orElse(null);
    }

    public E getFirstWorker() {
        return this.get(0);
    }

}