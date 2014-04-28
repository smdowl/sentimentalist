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
import weka.*;
import weka.core.Instance;
import weka.core.SparseInstance;


public class LinearRegression<T> implements Model<T, Double>{

    private final MatrixBuilder<T> matrixBuilder;
    private Vector beta;
    private List<T> trainingX;
    public LinearRegression(MatrixBuilder<T> mB){
       this.matrixBuilder = mB;
    }

    @Override
    public void train(List<T> x, List<Double> y) {

       this.trainingX = x;

        Vector K = matrixBuilder.getXYVector(x, y);
        System.out.println("Done calculating vector K");
        Matrix C = matrixBuilder.getCMatrix(x);
        System.out.println("Done getting the C matrix");
        LinearSystemSolver solver = C.withSolver(LinearAlgebra.FORWARD_BACK_SUBSTITUTION);
        System.out.println("Initialized the solver");
        beta = solver.solve(K, LinearAlgebra.SPARSE_FACTORY);
        System.out.println("Found the beta");

    }

    @Override
    public Double predict(T x) {
        Double prediction = matrixBuilder.getPrediction(x,beta);
        return prediction;
    }


}
