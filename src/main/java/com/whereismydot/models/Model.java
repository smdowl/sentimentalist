package com.whereismydot.models;

import java.util.List;

/**
 * Interface defining what a model is. SVM, linear regression and Gaussian 
 * process will be an instance 
 * 
 * @author andrey
 *
 * @param <X> Type of the feature vector
 * @param <Y> Type of the result
 */
public interface Model<X,Y> {

	public void train(List<X> x, List<Y> y);
	
	public Y    predict(X x);
	
}
