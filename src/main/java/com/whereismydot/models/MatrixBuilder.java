package com.whereismydot.models;

import java.util.List;
import Jama.Matrix;

/**
 * Created by Matthieu on 15/04/2014.
 */

public interface MatrixBuilder<T> {

    public Matrix getMatrix(List<T> x);
}