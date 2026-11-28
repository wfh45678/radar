package com.pgmmers.radar.model.query;

public class ScenarioQuery extends PageQuery {

	private String scenarioName;
	private Integer status;
	private String merchantCode;
	private String label;

	public String getModelName() {
		return scenarioName;
	}

	public void setModelName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


}
