package com.whereismydot.models;

import java.util.List;


public interface MatrixBuilder<T> {
    public Jama.Matrix getBetaMatrix(List<T> x,List<Double> y);
    public Double getPrediction(T x, Jama.Matrix beta);
}