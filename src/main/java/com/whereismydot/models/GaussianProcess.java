package com.whereismydot.models;

import java.util.List;

import Jama.Matrix;

public class GaussianProcess<T> implements Model<T, Double>{

	private final Kernel<T> kernel;
	private final double    beta;

	private Matrix  Cinv;        
	private List<T> trainingX;
	private Matrix  trainingY; 
	
	public GaussianProcess(Kernel<T> kernel, double sigma2){
		this.kernel = kernel;
		this.beta   = 0.5;
	}
	

	@Override
	public void train(List<T> x, List<Double> y) {
		
		
		this.trainingX = x;
		this.trainingY = new Matrix(y.size(), 1);
		
		for(int i = 0; i < y.size(); i++){
			trainingY.set(i, 0, y.get(i));
		}
		
		Matrix C = new Matrix(x.size(), x.size());
		
		for(int i = 0; i < x.size(); i++){
			for(int j = 0; j < x.size(); j ++){
				C.set(i, j, kernel.apply(x.get(i), x.get(j)));
			}
		}
		
		for(int i = 0; i < x.size(); i++){
			C.set(i, i, C.get(i,i) + beta);
		}
		
		Cinv = C.inverse();
	}

	@Override
	public Double predict(T x) {
		Matrix k = new Matrix(Cinv.getRowDimension(), 1);
		
		for(int i = 0; i < trainingX.size(); i++){
			k.set(i, 0, kernel.apply(trainingX.get(i), x));
		}
		
		Matrix mean = k.transpose().times(Cinv).times(trainingY);
		
		return mean.get(0, 0);
	}
	 
}
