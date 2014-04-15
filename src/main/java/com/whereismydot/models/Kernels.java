package com.whereismydot.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of simple kernel functions
 * @author andrey
 *
 */
public class Kernels {

	/**
	 * Compose kernels into one uber kernel
	 * @author andrey
	 *
	 * @param <T>
	 */
	public static class CompositeKernel<T> implements Kernel<T>{
		private final List<Kernel<T>> components;

		public CompositeKernel(List<Kernel<T>> components){
			this.components = components;
		}
		
		@Override
		public double apply(T x1, T x2) {
			
			double result = 1;
			for(Kernel<T> kernel : components){
				result *= kernel.apply(x1, x2);
			}
			return result;
		}
		
	}
	
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
	
	public static class WaveKernel implements Kernel<Map<String,Double>>{

		private final double theta;
		
		public WaveKernel(double theta){
			this.theta = theta;
		}
		
		@Override
		public double apply(Map<String, Double> x1, Map<String, Double> x2) {
		
			double normDiff = norm2(diff(x1,x2));
			if(normDiff == 0){
				return 0;
			}else{
				return (theta / normDiff) * Math.sin(normDiff / theta);	
			}
		}		
	}
	
	static public <K> Map<K, Double> diff(Map<K, Double> v1, Map<K, Double> v2){
		Map<K, Double> result = new HashMap<K, Double>();
		for(java.util.Map.Entry<K, Double> elem : v1.entrySet()){
			Double v2elem = v2.get(elem.getKey());
			
			if(v2elem == null){
				result.put(elem.getKey(), elem.getValue() - 0);
			}else{
				result.put(elem.getKey(), elem.getValue() - v2elem);	
			}
			
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
