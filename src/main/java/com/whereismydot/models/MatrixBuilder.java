package com.whereismydot.models;

import java.util.List;
import Jama.Matrix;
import org.la4j.vector.Vector;

/**
 * Created by Matthieu on 15/04/2014.
 */

public interface MatrixBuilder<T> {

    public org.la4j.matrix.Matrix getCMatrix(List<T> x);
    public Vector getXYVector(List<T> x, List<Double> y);
    public Double getPrediction(T x, Vector beta);
}