package com.pgmmers.radar.intercpt;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 本地线程缓存信息
 * @author xushuai
 */
public class Context implements Serializable {
    private String username;
    private String displayName;
    private String operationName;
    private String code;
    private Map<String, Object> attributes;

    public Context() {
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String toString() {
        String result = "Context{\n\tusername='" + this.username + '\'' + "\n\tdisplayName='" + this.displayName + '\'' + "\n\toperationName='" + this.operationName + '\'';
        if (this.attributes != null) {
            result = result + "\n\tattributes={\n" + (String)this.attributes.entrySet().stream().map((entry) -> {
                return "\t\t" + (String)entry.getKey() + "='" + entry.getValue() + "'";
            }).collect(Collectors.joining("\n")) + "\n\t}";
        }

        result = result + "\n}";
        return result;
    }
}

