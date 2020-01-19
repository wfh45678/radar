package com.pgmmers.radar.service.dnn;

import java.util.Map;

/**
 * <p>
 * 机器学习模型执行器接口
 * </p>
 * <p>
 * 该接口内置TensorFlow实现，目前版本只考虑输入层为离散值的情况，不考虑词嵌入和融入卷积层，其中
 * 离散值通过表达式取数从前置流程传递过来，模型的预测结果为事件评分。
 * </p>
 * <p>
 * 模型抽象：y=f(x)
 * </p>
 *
 * @author guor
 * @date 2019/11/28
 */
public interface Estimator {
    /**
     * 线性回归模型
     */
    String TYPE_REGRESSION = "REGRESSION";
    /**
     * 基于TensorFlow实现的神经网络模型
     */
    String TYPE_TENSOR_DNN = "TENSOR_DNN";

    float predict(Long modelId, Map<String, Map<String, ?>> data);

    String getType();
}
