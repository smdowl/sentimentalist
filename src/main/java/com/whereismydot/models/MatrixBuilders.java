package com.whereismydot.models;

import java.util.*;

import Jama.Matrix;



public class MatrixBuilders {

    public static class MatrixBuilderLinear implements MatrixBuilder<Map<String,Double>>{


        @Override
        public Matrix getMatrix(List<Map<String, Double>> x) {

            TreeMap<String, Double> treeMap = new TreeMap<String, Double>();
            treeMap.putAll(x.get(0));

            int n1 = x.size();
            int n2 = treeMap.size();
            Matrix resMatrix = new Matrix(n1, n2);

            Iterator<String> iter = treeMap.keySet().iterator();

            int index = 0;
            while(iter.hasNext()) {
                String currentMapEntry = iter.next();
                for(int i=0; i<n1; i++) {
                    resMatrix.set(i,index,x.get(i).get(currentMapEntry));
                }
                index++;
            }

            return resMatrix;
        }

    }
}