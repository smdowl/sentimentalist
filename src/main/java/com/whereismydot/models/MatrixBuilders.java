package com.whereismydot.models;

import java.util.*;
import java.util.Map.Entry;

import Jama.Matrix;



public class MatrixBuilders {

    public static class MatrixBuilderLinear implements MatrixBuilder<Map<String,Double>>{


        @Override
        public Matrix getMatrix(List<Map<String, Double>> x) {
        	
        	// First compute feature indices 
        	Set<String> featureNames = new HashSet<String>();
        	for(Map<String, Double> vector : x){
        		featureNames.addAll(vector.keySet());
        	}
        	
        	Map<String, Integer> namesIdx = new HashMap<String,Integer>();
        	int idx = 0;
        	for(String name : featureNames){
        		namesIdx.put(name, idx++);
        	}
        	
        	
        	// Now build the matrix
        	Matrix resMatrix = new Matrix(x.size(), featureNames.size());        	
        	int i = 0;
        	for(Map<String, Double> vector : x){
        		for(Entry<String, Double> entry : vector.entrySet()){
        			resMatrix.set(i, namesIdx.get(entry.getKey()), entry.getValue());
        		}
        		i++;
        	}
        	
            return resMatrix;
        }

    }
}