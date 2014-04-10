package com.whereismydot.models;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of simple kernel functions
 * @author andrey
 *
 */
public class Kernels {

	public static class Linear implements Kernel<Map<String, Double>>{

		@Override
		public double apply(Map<String, Double> x1, Map<String, Double> x2) {
			double result = 0;
			for(String key : x1.keySet()){
				if(x2.containsKey(key)){
					result += x1.get(key) * x2.get(key);
				}
			}
			
			return result;
		}
		
	}
	
	public static class Gaussian implements Kernel<Map<String, Double>>{

		private final double sigma2;
		
		public Gaussian(double sigma2){
			this.sigma2 = sigma2;
		}
		
		@Override
		public double apply(Map<String, Double> x1, Map<String, Double> x2) {
			double normDiff = norm2(diff(x1,x2));
			return Math.exp(-(normDiff*normDiff) / (2 * sigma2) );
		}
		
	}
	
	static public <K> Map<K, Double> diff(Map<K, Double> v1, Map<K, Double> v2){
		Map<K, Double> result = new HashMap<K, Double>();
		for(java.util.Map.Entry<K, Double> elem : v1.entrySet()){
			result.put(elem.getKey(), elem.getValue() - v2.getOrDefault(elem.getKey(), 0.0));
		}
		return result;
	}
	
	static public double norm2(Map<String, Double> vector){
		double result = 0;
		for(double val : vector.values()){
			result += val * val;
		}
		return Math.sqrt(result);
	}
}
