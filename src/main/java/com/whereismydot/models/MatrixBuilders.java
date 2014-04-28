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
import Jama.Matrix.*;
import Jama.SingularValueDecomposition;

public class MatrixBuilders {

    public static class MatrixBuilderLinear implements MatrixBuilder<Map<String,Double>>{

        private HashMap<String,Integer> namesIdx = new HashMap<String,Integer>();

        @Override
        public Jama.Matrix getBetaMatrix(List<Map<String, Double>> x, List<Double> y) {
        	
        	// First compute feature indices 
        	Set<String> featureNames = new HashSet<String>();
        	for(Map<String, Double> vector : x){
        		featureNames.addAll(vector.keySet());
        	}
        	
        	//namesIdx = new HashMap<String,Integer>();
        	int idx = 0;
        	for(String name : featureNames){
        		namesIdx.put(name, idx++);
        	}

        	// Now build the matrix
        	Jama.Matrix xMatrix = new Jama.Matrix(x.size(), namesIdx.size());
        	int i = 0;
        	for(Map<String, Double> vector : x){
        		for(Entry<String, Double> entry : vector.entrySet()){
        			xMatrix.set(i, namesIdx.get(entry.getKey()), entry.getValue());
                    System.out.println(entry.getValue());
        		}
        		i++;
        	}

            Jama.Matrix yMatrix = new Jama.Matrix(y.size(),1);

            i = 0;
            for(Double entry : y) {
                yMatrix.set(i,0,entry);
                i++;
            }
            Jama.Matrix cMatrix = xMatrix.transpose().times(xMatrix);
            Jama.Matrix cInv = pinv(cMatrix);
            Jama.Matrix resMatrix = cInv.times(xMatrix.transpose().times(yMatrix));

            return resMatrix;
        }

        public static Jama.Matrix pinv(Jama.Matrix x) {
            if (x.rank() < 1)
                return null;
            if (x.getColumnDimension() > x.getRowDimension())
                return pinv(x.transpose()).transpose();
            SingularValueDecomposition svdX = new SingularValueDecomposition(x);
            double[] singularValues = svdX.getSingularValues();
            double tol = Math.max(x.getColumnDimension(), x.getRowDimension()) * singularValues[0] * 2E-16;
            double[] singularValueReciprocals = new double[singularValues.length];
            for (int i = 0; i < singularValues.length; i++)
                singularValueReciprocals[i] = Math.abs(singularValues[i]) < tol ? 0 : (1.0 / singularValues[i]);
            double[][] u = svdX.getU().getArray();
            double[][] v = svdX.getV().getArray();
            int min = Math.min(x.getColumnDimension(), u[0].length);
            double[][] inverse = new double[x.getColumnDimension()][x.getRowDimension()];
            for (int i = 0; i < x.getColumnDimension(); i++)
                for (int j = 0; j < u.length; j++)
                    for (int k = 0; k < min; k++)
                        inverse[i][j] += v[i][k] * singularValueReciprocals[k] * u[j][k];
            return new Jama.Matrix(inverse);
        }



        @Override
        public Double getPrediction(Map<String, Double> x, Jama.Matrix beta) {
            System.out.println("We are here");
            System.out.println(namesIdx.size());
            Jama.Matrix xVector = new Jama.Matrix(1,namesIdx.size());
            for(String key : x.keySet()) {
               if(namesIdx.containsKey(key)) {
                   System.out.println("here is one :" + namesIdx.size());
                   System.out.println("here is other :" + namesIdx.get(key));
                   xVector.set(0,namesIdx.get(key),x.get(key));
               }
            }
            return xVector.times(beta).get(0,0);
        }


    }



        /*

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
        */
//
//        @Override
//        public Double getPrediction(Map<String, Double> x, Vector beta) {
//            double[] sparseArray = new double[namesIdx.size()];
//            for (String featureName : x.keySet()) {
//                sparseArray[namesIdx.get(featureName)] = x.get(featureName);
//            }
//            Vector xi = new CompressedVector(sparseArray);
//            Double res = xi.innerProduct(beta);
//            return res;
//        }
    }