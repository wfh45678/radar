package com.pgmmers.radar.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 根据字段名获取字段
 * @author xushuai
 */
public class ReflectUtil {

    public static void setFieldValueByName(Field field, Object fieldValue, Object o) throws Exception {
        field.setAccessible(true);
        //获取字段类型
        Class<?> fieldType = field.getType();
        //根据字段类型给字段赋值
        if (String.class == fieldType) {
            field.set(o, String.valueOf(fieldValue));
        } else if ((Integer.TYPE == fieldType)
                || (Integer.class == fieldType)) {
            field.set(o, Integer.parseInt(fieldValue.toString()));
        } else if ((Long.TYPE == fieldType)
                || (Long.class == fieldType)) {
            field.set(o, Long.valueOf(fieldValue.toString()));
        } else if ((Float.TYPE == fieldType)
                || (Float.class == fieldType)) {
            field.set(o, Float.valueOf(fieldValue.toString()));
        } else if ((Short.TYPE == fieldType)
                || (Short.class == fieldType)) {
            field.set(o, Short.valueOf(fieldValue.toString()));
        } else if ((Byte.TYPE == fieldType)
                || (Byte.class == fieldType)) {
            field.set(o, Byte.valueOf(fieldValue.toString()));
        }
        else if ((Double.TYPE == fieldType)
                || (Double.class == fieldType)) {
            field.set(o, Double.valueOf(fieldValue.toString()));
        } else if (Character.TYPE == fieldType) {
            if ((fieldValue != null) && (fieldValue.toString().length() > 0)) {
                field.set(o, fieldValue.toString().charAt(0));
            }
        } else if (BigDecimal.class == fieldType) {
            Long v1 = Long.valueOf(fieldValue.toString());
            field.set(o, BigDecimal.valueOf(v1));
        } else if (Date.class == fieldType) {
            field.set(o, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fieldValue.toString()));
        } else if (boolean.class == fieldType) {
            if ("1".equals(fieldValue)) {
                field.set(o, true);
            }
            if ("0".equals(fieldValue)) {
                field.set(o, false);
            }
            field.set(o, Boolean.valueOf(fieldValue.toString()));
        } else if (String[].class == fieldType) {
            String[] arr = JsonUtils.fromJson(fieldValue.toString(), String[].class);
            field.set(o, arr);
        } else {
            field.set(o, fieldValue);
        }
    }

    public static List<Field> getListField(Class<?> clazz) {
        Field[] selfFields = clazz.getDeclaredFields();
        List<Field> list = new ArrayList<>(Arrays.asList(selfFields));
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && superClazz != Object.class) {
            List<Field> listSuperField = getListField(superClazz);
            for (Field field : listSuperField) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 根据字段名获取字段
     *
     * @param fieldName 字段名
     * @param clazz     包含该字段的类
     * @return 字段
     */
    public static Field getFieldByName(String fieldName, Class<?> clazz) {
        //拿到本类的所有字段
        Field[] selfFields = clazz.getDeclaredFields();
        //如果本类中存在该字段，则返回
        for (Field field : selfFields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        //否则，查看父类中是否存在此字段，如果有则返回
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && superClazz != Object.class) {
            return getFieldByName(fieldName, superClazz);
        }
        //如果本类和父类都没有，则返回空
        return null;
    }

    /**
     * getFieldValueByNameSequence
     *
     * @param fieldNameSequence 带路径的属性名或简单属性名
     * @param o                 对象
     * @return 属性值
     * @throws Exception 根据带路径或不带路径的属性名获取属性值
     *                   即接受简单属性名，如userName等，又接受带路径的属性名，如student.department.name等
     */
    public static Object getFieldValueByNameSequence(String fieldNameSequence, Object o) throws Exception {
        Object value;
        //将fieldNameSequence进行拆分
        String[] attributes = fieldNameSequence.split("\\.");
        if (attributes.length == 1) {
            value = getFieldValueByName(fieldNameSequence, o);
        } else {
            //根据属性名获取属性对象
            Object fieldObj = getFieldValueByName(attributes[0], o);
            String subFieldNameSequence = fieldNameSequence.substring(fieldNameSequence.indexOf(".") + 1);
            value = getFieldValueByNameSequence(subFieldNameSequence, fieldObj);
        }
        return value;

    }
 /*<-------------------------辅助的私有方法----------------------------------------------->*/

    /**
     * 根据字段名获取字段值
     *
     * @param fieldName 字段名
     * @param o         对象
     * @return 字段值
     */
    public static Object getFieldValueByName(String fieldName, Object o) throws Exception {
        Object value;
        Field field = getFieldByName(fieldName, o.getClass());
        if (field == null) {
            throw new Exception(o.getClass().getSimpleName() + "类不存在字段名 " + fieldName);
        }
        value = getValueByField(fieldName, o, field);
        return value;
    }

    private static Object getValueByField(String fieldName, Object o, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(o);
    }
}
