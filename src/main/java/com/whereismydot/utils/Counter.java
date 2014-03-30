package com.whereismydot.utils;

import java.util.HashMap;
import java.util.Map;

public class Counter<T> {

    public Map<T, Integer> counts = new HashMap<T, Integer>();

    public void increment(T key) {
        if (!counts.containsKey(key))
            counts.put(key, 0);

        counts.put(key, counts.get(key) + 1);
    }

}
