package com.whereismydot.models;

import libsvm.*;
import java.util.List;

public interface SVMtrain<T> {
    public svm_node[] getNode(T x);
    public void initialize(List<T> x);
}
