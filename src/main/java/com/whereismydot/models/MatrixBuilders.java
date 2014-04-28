package com.whereismydot.models;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.Map.Entry;


import com.google.common.collect.Sets;
import org.apache.commons.math.linear.SparseRealVector;
import org.la4j.*;
import org.la4j.factory.Factory;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.vector.*;
import org.la4j.vector.Vector;
import org.la4j.vector.functor.VectorAccumulator;
import org.la4j.vector.functor.VectorFunction;
import org.la4j.vector.functor.VectorPredicate;
import org.la4j.vector.functor.VectorProcedure;
import org.la4j.vector.sparse.CompressedVector;
import org.la4j.vector.sparse.SparseVector;

public class MatrixBuilders {

    public static class MatrixBuilderLinear implements MatrixBuilder<Map<String,Double>>{

        /*
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
        }*/

        private Map<String, Integer> namesIdx = new HashMap<String, Integer>();

        @Override
        public Matrix getCMatrix(List<Map<String, Double>> x) {
            Set<String> featureNames = new HashSet<String>();
            for (Map<String, Double> vector : x) {
                featureNames.addAll(vector.keySet());
            }

            int idx = 0;
            for (String name : featureNames) {
                namesIdx.put(name, idx++);
            }

            //Matrix resMatrix = new CRSMatrix(new double[namesIdx.size()][namesIdx.size()]);
            Matrix resMatrix = new CRSMatrix();

            for( int i = 0 ; i < x.size(); i++) {

                double[] sparseArray = new double[namesIdx.size()];

                Map<String, Double> vector = x.get(i);
                for (String featureName : vector.keySet()) {
                    sparseArray[namesIdx.get(featureName)] = vector.get(featureName);
                }
                //double[][] sparseArrayMatrix = {sparseArray};
                Vector a = new CompressedVector(sparseArray);
                Vector b = new CompressedVector(sparseArray);
                //Matrix m = new CRSMatrix();
                Matrix m = a.outerProduct(b);
                if ( i == 0 ) {
                    resMatrix = m;
                } else {
                    resMatrix.add(m);
                }
                System.out.print(i);
                //CRSMatrix a = new CRSMatrix(sparseArrayMatrix);
                //CCSMatrix b = new CCSMatrix(sparseArrayMatrix);
                //Matrix m = b.multiply(a);
                //resMatrix.add(m);
            }
            //resMatrix = resMatrix.multiply(1.0/x.size());
            return resMatrix;
        }

        @Override
        public Vector getXYVector(List<Map<String, Double>> x, List<Double>y) {
            Vector resVector = new CompressedVector(new double [namesIdx.size()]);

            for (int i=0; i<x.size(); i++) {
                double[] sparseArray = new double[namesIdx.size()];
                Map<String, Double> vector = x.get(i);
                for (String featureName : vector.keySet()) {
                    if(namesIdx.containsKey(featureName)) {
                        sparseArray[namesIdx.get(featureName)] = vector.get(featureName);
                    }
                }
                Vector xi = new CompressedVector(sparseArray);
                xi = xi.multiply(y.get(i));
                resVector = resVector.add(xi);
            }
            resVector = resVector.multiply(1.0/y.size());
            return resVector;
        }

        @Override
        public Double getPrediction(Map<String, Double> x, Vector beta) {
            double[] sparseArray = new double[namesIdx.size()];
            for (String featureName : x.keySet()) {
                sparseArray[namesIdx.get(featureName)] = x.get(featureName);
            }
            Vector xi = new CompressedVector(sparseArray);
            Double res = xi.innerProduct(beta);
            return res;
        }
    }
}