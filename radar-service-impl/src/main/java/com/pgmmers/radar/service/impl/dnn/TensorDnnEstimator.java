package com.pgmmers.radar.service.impl.dnn;

import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.model.MoldService;
import com.pgmmers.radar.vo.model.MoldParamVO;
import com.pgmmers.radar.vo.model.MoldVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
public class TensorDnnEstimator implements Estimator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TensorDnnEstimator.class);
    @Resource
    private MoldService moldService;
    private Map<Long, SavedModelBundle> modelBundleMap = new HashMap<>();

    @Override
    public double predict(Long modelId, Map<String, Map<String, ?>> data) {
        MoldVO mold = moldService.getByModelId(modelId);
        if (mold == null) {
            LOGGER.debug("没有找到模型配置，ModelId：{}", modelId);
            return 0;
        }
        SavedModelBundle modelBundle = loadAndCacheModel(mold);
        if (modelBundle == null) {
            LOGGER.warn("模型文件不存在或加载失败，ModelId：{}", modelId);
            return 0;
        }
        Session tfSession = modelBundle.session();
        try {
            List<MoldParamVO> params = mold.getParams();
            Session.Runner runner = tfSession.runner();
            for (MoldParamVO moldParam : params) {
                runner.feed(moldParam.getFeed(), convert2Tensor(moldParam, data));
            }
            Tensor<?> output = runner.fetch(mold.getOperation()).run().get(0);
            double[] results = new double[1];
            output.copyTo(results);
            return results[0];
        } catch (Exception e) {
            LOGGER.error("模型调用失败，ModelId:" + modelId, e);
        } finally {
            tfSession.close();
        }
        return 0;
    }

    private Tensor<?> convert2Tensor(MoldParamVO moldParam, Map<String, Map<String, ?>> data) {
        String expressions = moldParam.getExpressions();
        if (StringUtils.isEmpty(expressions)) {
            return Tensor.create(new double[1][1]);
        }
        String[] expList = expressions.split(",");
        double[][] vec = new double[expList.length][1];
        int a = 0;
        for (String s : expList) {
            double xn = 0;
            String[] ss = s.split("\\.");//fields.deviceId，abstractions.log_uid_ip_1_day_qty
            Map<String, ?> stringMap = data.get(ss[0]);
            if (stringMap != null) {
                xn = (Double) stringMap.get(ss[1]);
            }
            vec[a++][0] = xn;
        }
        return Tensor.create(vec);
    }

    private synchronized SavedModelBundle loadAndCacheModel(MoldVO mold) {
        SavedModelBundle modelBundle = modelBundleMap.get(mold.getId());
        if (modelBundle == null) {
            File file = new File(mold.getPath());
            if (file.exists() && file.isDirectory()) {
                // 模型加载，比较耗时
                try {
                    modelBundle = SavedModelBundle.load(mold.getPath(), mold.getTag());
                    modelBundleMap.put(mold.getId(), modelBundle);
                } catch (Exception e) {
                    LOGGER.warn("模型加载失败，MoldId：{}", mold.getId());
                }
            }
        }
        return modelBundle;
    }

    @Override
    public String getType() {
        return Estimator.TYPE_TENSOR_DNN;
    }
}
