package com.whereismydot.utils;

import java.util.*;

public class Counter<T> {

    public Map<T, Integer> counts = new HashMap<T, Integer>();

    public void increment(T key) {
        if (!counts.containsKey(key))
            counts.put(key, 0);

        counts.put(key, counts.get(key) + 1);
    }

    public void filterCounts(int minCount) {
        List<T> toRemove = new LinkedList<T>();
        for (T key : counts.keySet()) {
            if (counts.get(key) < minCount)
                toRemove.add(key);
        }

        for (T key : toRemove)
            counts.remove(key);
    }
}
