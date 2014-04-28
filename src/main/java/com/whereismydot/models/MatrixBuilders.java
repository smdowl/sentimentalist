package com.whereismydot.models;


import java.util.*;
import java.util.Map.Entry;
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
            // We implement the Moore-Penrose pseudo-inverse for a square matrix.
            // as given in the lecture notes of the module : Inverse Problems in Imaging.
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