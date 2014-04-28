package com.whereismydot.models;

import java.util.List;
import libsvm.*;


public class SVM<T> implements Model<T,Double> {

    private svm_model model;
    private svm_parameter param = new svm_parameter();
    private final SVMtrain<T> svmtrain;


    public SVM(SVMtrain<T> svmtrain) {
        this.svmtrain = svmtrain;
    }

    @Override
    public void train(List<T> x, List<Double> y) {

        svm_problem prob = new svm_problem();
        prob.y = new double[x.size()];
        prob.l = x.size();
        prob.x = new svm_node[x.size()][];

        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 1;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.cache_size = 20000;
        param.eps = 0.001;

        svmtrain.initialize(x);

        for (int i = 0; i < x.size(); i++){
            prob.x[i] = svmtrain.getNode(x.get(i));
            prob.y[i] = y.get(i);
        }

        model = svm.svm_train(prob, param);
    }

    @Override
    public Double predict(T x) {
        svm_node[] node = svmtrain.getNode(x);
        Double result = svm.svm_predict(model,node);
        return result;
    }
}
