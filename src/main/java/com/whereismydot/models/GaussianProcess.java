package com.whereismydot.models;

import java.util.List;

import Jama.Matrix;

public class GaussianProcess<T> implements Model<T, Double>{
	public interface BasisFunction<T>{
		
		public int size();
		
		public Matrix apply(T features);
		 
	}

	private final BasisFunction basis;
	private final double 	    sigma2;
	private  	  Matrix        Cinv;        
	
	public GaussianProcess(BasisFunction basis, double sigma2){
		this.basis  = basis;
		this.sigma2 = sigma2; 
	}
	

	@Override
	public void train(List<T> x, List<Double> y) {
		
		Matrix R = new Matrix(basis.size(), x.size());
		
		int i = 0;
		for(T features : x){
			R.setMatrix(i, i, 0, basis.size(),basis.apply(features));
			i++;
		}
		
		Matrix Q = R.times(R.transpose()).times(sigma2);
		
		Matrix C = Q.plus(Matrix.identity(Q.getRowDimension(), Q.getColumnDimension()).times(sigma2));
		
		Cinv = C.inverse();
		// TODO 
		
	}

	@Override
	public Double predict(T x) {
		// TODO Auto-generated method stub
		return null;
	}
	
	 
}
