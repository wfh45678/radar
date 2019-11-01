package com.pgmmers.radar.intercpt;

import java.util.Date;
import java.util.Map;

/**
 * token信息
 * @author xushuai
 */
public class TokenBody {
    private String subject;
    private String displayName;
    private String type;
    private Date expireDate;
    private Map<String, Object> additionalProperties;

    public TokenBody() {
    }

    public TokenBody(String subject, String displayName, String type, Date expireDate, Map<String, Object> additionalProperties) {
        this.subject = subject;
        this.displayName = displayName;
        this.type = type;
        this.expireDate = expireDate;
        this.additionalProperties = additionalProperties;
    }

    public TokenBody(String subject, String displayName, String type, long validTimeInSeconds, Map<String, Object> additionalProperties) {
        this(subject, displayName, type, new Date(System.currentTimeMillis() + validTimeInSeconds * 1000L), additionalProperties);
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getExpireDate() {
        return this.expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
