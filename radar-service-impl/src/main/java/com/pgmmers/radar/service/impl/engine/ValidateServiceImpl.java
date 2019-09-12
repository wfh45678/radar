package com.pgmmers.radar.service.impl.engine;


import com.pgmmers.radar.enums.ValidateType;
import com.pgmmers.radar.service.engine.ValidateService;
import com.pgmmers.radar.service.impl.util.ValidateUtil;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.vo.model.FieldVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValidateServiceImpl implements ValidateService {

    @Autowired
    private FieldService fieldService;

    @Override
    public Map<String, Object> validate(Long modelId, Map data) {
        List<FieldVO> fieldList = fieldService.listField(modelId);
        Map<String, Object> result = new HashMap<>();
        for (FieldVO field : fieldList) {
            String validateType = field.getValidateType();
            if (StringUtils.isEmpty(validateType)) {
                continue;
            }
            ValidateType vld = ValidateType.valueOf(validateType);
            Object value = data.get(field.getFieldName());
            if (value == null) {
                result.put(field.getFieldName(), "字段为空");
                continue;
            }
            switch (vld) {
            case STRING:
                if (!(value instanceof String)) {
                    result.put(field.getFieldName(), "不是字符串");
                }
                break;
            case INTEGER:
                if (!(value instanceof Integer)) {
                    result.put(field.getFieldName(), "不是整数类型");
                }
                break;
            case LONG:
                if (value instanceof Integer || value instanceof Long) {

                } else {
                    result.put(field.getFieldName(), "不是整数类型");
                }
                break;
            case DOUBLE:
                if (!(value instanceof Number)) {
                    result.put(field.getFieldName(), "不是数字类型");
                }
                break;
            case LENGTH:
                Integer len = Integer.parseInt(field.getValidateArgs());
                if (value instanceof String && value.toString().length() == len) {

                } else {
                    result.put(field.getFieldName(), "长度不正确");
                }
                break;
            case MOBILE:
                boolean matched = ValidateUtil.checkMobile(value.toString());
                if (!matched) {
                    result.put(field.getFieldName(), "手机号码格式不正确");
                }
                break;
            case EMAIL:
                matched = ValidateUtil.checkEmail(value.toString());
                if (!matched) {
                    result.put(field.getFieldName(), "邮箱格式不正确");
                }
                break;
            case IP:
                matched = ValidateUtil.checkIp(value.toString());
                if (!matched) {
                    result.put(field.getFieldName(), "IP格式不正确");
                }
                break;
            case REGEX:
                if (!value.toString().matches(field.getValidateArgs())) {
                    result.put(field.getFieldName(), "字段格式不匹配");
                }
                break;
            default:
                break;
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> validate(List<FieldVO> fieldList, Map data) {
        // TODO Auto-generated method stub
        return null;
    }

}
