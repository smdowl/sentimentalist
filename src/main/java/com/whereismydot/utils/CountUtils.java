package com.whereismydot.utils;

import java.util.Map;

public class CountUtils {

    public static <K> void increment(Map<K, Integer> map, K key) {
        if (!map.containsKey(key))
            map.put(key, 0);

        map.put(key, map.get(key) + 1);
    }

}
