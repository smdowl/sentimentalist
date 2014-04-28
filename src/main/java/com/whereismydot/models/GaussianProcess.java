package com.whereismydot.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Jama.Matrix;

public class GaussianProcess implements Model<Map<String, Double>, Double>{

	private final Kernel<Map<String,Double>> kernel;
	private final double    beta;

	private Matrix  Cinv;        
	private List<Map<String,Double>> trainingX;
	private Matrix  trainingY; 
	private Map<String,Double> z; 
	
	public GaussianProcess(Kernel<Map<String, Double>> kernel, double beta){
		this.kernel = kernel;
		this.beta   = beta;
	}
	

	@Override
	public void train(List<Map<String, Double>> x, List<Double> y) {
		
		this.z 	 	   = computeZ(x);
		this.trainingX = normalise(x);
		this.trainingY = new Matrix(y.size(), 1);
		int N 		   = x.size();
		
		for(int i = 0; i < N; i++){
			trainingY.set(i, 0, y.get(i));
		}
		
		Matrix C = new Matrix(N, N);
		
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j ++){
				double k = kernel.apply(trainingX.get(i), trainingX.get(j));
				C.set(i, j, k);
			}
		}
		
		for(int i = 0; i < x.size(); i++){
			C.set(i, i, C.get(i,i) + beta);
		}
		
		Cinv = C.inverse();
	}

	@Override
	public Double predict(Map<String, Double> x) {
		Matrix k = new Matrix(Cinv.getRowDimension(), 1);
		Map<String, Double> normX = normalise(x);
		
		for(int i = 0; i < trainingX.size(); i++){
			k.set(i, 0, kernel.apply(trainingX.get(i), normX));
		}
		
		Matrix mean = k.transpose().times(Cinv).times(trainingY);
		
		return mean.get(0, 0);
	}
	 
	private List<Map<String, Double>> normalise(List<Map<String, Double>> input){
		
		List<Map<String, Double>> result = new ArrayList<Map<String,Double>>();
		for(Map<String,Double> vector : input){
			result.add(normalise(vector));
		}		
		return result;		
	}
	
	private Map<String, Double> normalise(Map<String, Double> vector){	

		Map<String,Double> normVector = new HashMap<String, Double>();
		for(Entry<String,Double> elem : vector.entrySet()){
			normVector.put(elem.getKey(), elem.getValue() / z.get(elem.getKey()));
		}
		
		return normVector;		
	}
	
	public Map<String,Double> computeZ(List<Map<String,Double>> input){
		Map<String,Double> z = new HashMap<String, Double>();
		
		// Find the maximum values for all elements
		for(Map<String,Double> vector : input){
			for(Entry<String,Double> elem : vector.entrySet()){
				double val = elem.getValue() == 0 ? 1 : Math.abs(elem.getValue());
				
				if(!z.containsKey(elem.getKey()) || z.get(elem.getKey()) < val){
					z.put(elem.getKey(), val);
				}
			}
		}
		return z;
	}
}
