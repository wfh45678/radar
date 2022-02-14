package com.pgmmers.radar.service.impl.engine.plugin;

import com.alibaba.fastjson.JSONObject;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.impl.util.BeanUtils;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * author: wangcheng Date: 2020/5/19 Time: 上午11:47 Description:
 */
@Component
public class HTTP_UTIL implements PluginServiceV2 {

    private static final Logger logger = LoggerFactory.getLogger(HTTP_UTIL.class);

    @Override
    public Integer key() {
        return 8;
    }

    @Override
    public String desc() {
        return "HttpUtil";
    }

    @Override
    public String getType() {
        return "JSON";
    }

    @Override
    public Object handle(PreItemVO item, Map<String, Object> jsonInfo, String[] sourceField) {
        String url = item.getArgs();
        String reqType = item.getReqType();
        //String arg = jsonInfo.get(sourceField[0]).toString();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = (RestTemplate) BeanUtils.getBean("restTemplate");
        ResponseEntity<JSONObject> responseEntity = restTemplate
                .exchange(url, HttpMethod.valueOf(reqType), entity, JSONObject.class, sourceField);
        logger.info("http plugin:{}\n{}\n {}", url, sourceField, responseEntity.toString());
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
    }
}
