package com.whereismydot.models;

import java.util.List;

/**
 * A model that predicts the last seen value as the next value. 
 * @author andrey
 *
 * @param <X>
 * @param <Y>
 */
public class LastValueModel<X, Y> implements Model<X, Y> {
	
	private Y last;
	
	@Override
	public void train(List<X> x, List<Y> y) {
		last = y.get(y.size() - 1);
	}

	@Override
	public Y predict(X x) {
		return last;
	}

}
