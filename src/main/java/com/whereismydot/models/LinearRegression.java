package com.whereismydot.models;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import org.la4j.*;
import org.la4j.linear.LinearSystemSolver;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.*;
import org.la4j.matrix.*;
import java.util.ArrayList;
import Jama.Matrix.*;


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
