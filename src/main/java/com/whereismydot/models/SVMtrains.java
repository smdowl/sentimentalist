package com.whereismydot.models;

import java.util.*;

import libsvm.*;
import org.la4j.matrix.Matrix;

/**
 * Created by Matthieu on 24/04/2014.
 */
public class SVMtrains {
    public static class simple implements SVMtrain<Map<String, Double>>{


        private Map<String, Integer> namesIdx = new HashMap<String, Integer>();
        private int lengthOfFeatures;


        @Override
        public svm_node[] getNode(Map<String, Double> x) {

            int didContain = 0;
            int didNotContain = 0;
            for(String key : x.keySet()) {
                if(namesIdx.containsKey(key)) {
                    didContain++;
                } else {
                    didNotContain++;
                }
            }

            svm_node[] nodeArray = new svm_node[didContain+1];
            int i = 0;

            for(String key : x.keySet()){
                svm_node node = new svm_node();
                if(namesIdx.containsKey(key)) {
                    node.index = namesIdx.get(key);
                    node.value = x.get(key);
                    nodeArray[i] = node;
                    i++;
                }
            }
            svm_node finalNode = new svm_node();
            finalNode.index = -1;
            nodeArray[i] = finalNode;

            return nodeArray;
            }

        @Override
        public void initialize(List<Map<String,Double>> x) {
           Set<String> featureNames = new HashSet<String>();
           for (Map<String, Double> vector : x) {
               featureNames.addAll(vector.keySet());
           }

           int idx = 0;
           for (String name : featureNames) {
               namesIdx.put(name, idx++);
           }
           lengthOfFeatures = namesIdx.size();
        }

    }
}
