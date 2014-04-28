package com.whereismydot.models;

import java.util.List;


public class LinearRegression<T> implements Model<T, Double>{

    private final MatrixBuilder<T> matrixBuilder;
    private Jama.Matrix beta;
    private List<T> trainingX;

    public LinearRegression(MatrixBuilder<T> mB){
       this.matrixBuilder = mB;
    }

    @Override
    public void train(List<T> x, List<Double> y) {
        this.trainingX = x;
        beta = matrixBuilder.getBetaMatrix(x, y);
    }

    @Override
    public Double predict(T x) {
        return matrixBuilder.getPrediction(x,beta);
    }


}
