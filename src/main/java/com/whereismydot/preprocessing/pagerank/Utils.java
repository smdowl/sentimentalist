package com.whereismydot.preprocessing.pagerank;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

class Utils {
    static private Gson gson = new Gson();
    static final double COUNTER_SCALE = 1e6;

    static final String LOST_MASS = "LostMass";
    static final String USER_COUNT = "UserCount";

    static Map<String, Object> parseNode(String json) {
        HashMap<String, Object> node = new HashMap<>();
        node = gson.fromJson(json, node.getClass());
        return node;
    }

    static long convertMass(double mass) {
        return (long) (mass * COUNTER_SCALE);
    }

    static double convertMass(long mass) {
        return (1.0 * mass) / COUNTER_SCALE;
    }
}
