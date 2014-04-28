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
            Jama.Matrix cInv = getPseudoInverse(cMatrix);
            Jama.Matrix resMatrix = cInv.times(xMatrix.transpose().times(yMatrix));

            return resMatrix;
        }

        public static Jama.Matrix getPseudoInverse(Jama.Matrix x) {
            //in our case the matrix is square, hence :
            double eps = 2E-16;
            SingularValueDecomposition svd = new SingularValueDecomposition(x);
            Jama.Matrix W = svd.getS();
            for (int i = 0; i < W.getColumnDimension(); i++) {
                System.out.println("This is the singular values: " + W.get(i,i));
                double temp = W.get(i,i) < eps ? 0 : 1.0/W.get(i,i);
                W.set(i,i,temp);

            }
            Jama.Matrix u = svd.getU();
            Jama.Matrix v = svd.getV();

            Jama.Matrix resMatrix = v.times(W.times(u.transpose()));
            return resMatrix;
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

 }