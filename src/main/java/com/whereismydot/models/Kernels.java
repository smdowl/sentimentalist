package com.whereismydot.models;

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
}
