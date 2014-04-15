package com.whereismydot.models;

import java.util.List;

import Jama.Matrix;
import java.util.ArrayList;


public class LinearRegression<T> implements Model<T, Double>{

    private final MatrixBuilder<T> matrixBuilder;

    private Matrix beta;

    private List<T> trainingX;
    private Matrix  trainingY;

    public LinearRegression(MatrixBuilder<T> mB){
       this.matrixBuilder = mB;
    }

    @Override
    public void train(List<T> x, List<Double> y) {

       this.trainingX = x;
       this.trainingY = new Matrix(x.size(), 1);

        for(int i = 0; i < x.size(); i++){
            trainingY.set(i, 0, y.get(i));
        }


        Matrix X = matrixBuilder.getMatrix(x);
        Matrix tX = X.transpose();

      beta = tX.times(X).inverse().times(tX.times(trainingY));

    }

    @Override
    public Double predict(T x) {

        List<T> listx = new ArrayList<T>();
        listx.add(0,x);

        Matrix xVector = matrixBuilder.getMatrix(listx);

        return xVector.times(beta).get(0,0);
    }




}
