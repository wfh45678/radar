package com.pgmmers.radar.service.impl.dnn;

import com.pgmmers.radar.service.dnn.Estimator;
import com.pgmmers.radar.service.model.ModelConfService;
import com.pgmmers.radar.vo.model.ModelConfParamVO;
import com.pgmmers.radar.vo.model.ModelConfVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TensorDnnEstimator implements Estimator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TensorDnnEstimator.class);
    @Resource
    private ModelConfService modelConfService;
    private Map<Long, SavedModelBundle> modelBundleMap = new HashMap<>();

    @Value("${sys.conf.workdir}")
    private String  workDir;

    @Override
    public float predict(Long modelId, Map<String, Map<String, ?>> data) {
        ModelConfVO mold = modelConfService.getByModelId(modelId);
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
            List<ModelConfParamVO> params = mold.getParams();
            Session.Runner runner = tfSession.runner();
            for (ModelConfParamVO moldParam : params) {
                runner.feed(moldParam.getFeed(), convert2Tensor(moldParam, data));
            }
            Tensor<?> output = runner.fetch(mold.getOperation()).run().get(0);
            float[][] results = new float[1][1];
            output.copyTo(results);
            return results[0][0];
        } catch (Exception e) {
            LOGGER.error("模型调用失败，ModelId:" + modelId, e);
        }
        return 0;
    }

    private Tensor<?> convert2Tensor(ModelConfParamVO moldParam, Map<String, Map<String, ?>> data) {
        String expressions = moldParam.getExpressions();
        if (StringUtils.isEmpty(expressions)) {
            return Tensor.create(new float[1][1]);
        }
        String[] expList = expressions.split(",");
        float[][] vec = new float[1][expList.length];
        int a = 0;
        for (String s : expList) {
            float xn = 0;
            String[] ss = s.split("\\.");//fields.deviceId，abstractions.log_uid_ip_1_day_qty
            Map<String, ?> stringMap = data.get(ss[0]);
            if (stringMap != null) {
                xn = Float.parseFloat(String.valueOf(stringMap.get(ss[1])));
            }
            vec[0][a++] = xn;
        }
        return Tensor.create(vec);
    }

    private synchronized SavedModelBundle loadAndCacheModel(ModelConfVO mold) {
        SavedModelBundle modelBundle = modelBundleMap.get(mold.getId());
        if (modelBundle == null) {
            String path = workDir + "\\" + mold.getPath();
            String decomposePath = path.substring(0, path.lastIndexOf("."));
            File file = new File(decomposePath);
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

    public static void main(String[] args) {
        SavedModelBundle modelBundle = SavedModelBundle.load("d:/radar01", "serve");
        Session tfSession = modelBundle.session();
        try {
            Session.Runner runner = tfSession.runner();
            float[][] aa = new float[1][6];
            aa[0] = new float[]{20f, 1f, 1f, 1f, 10f, 2f};
            runner.feed("input_x", Tensor.create(aa));
            Tensor<?> output = runner.fetch("output_y/BiasAdd").run().get(0);
            float[][] results = new float[1][1];
            output.copyTo(results);
            System.out.println(results[0][0]);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tfSession.close();
        }
    }
}
