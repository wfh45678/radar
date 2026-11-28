package com.pgmmers.radar.model.query;

public class EventExportQuery extends PageQuery {
	
	  private String[] fields;
	  
	  private String[] features;
	  
	  private String[] rules;
	  
	  private String[] strategies;

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public String[] getFeatures() {
		return features;
	}

	public void setFeatures(String[] features) {
		this.features = features;
	}

	public String[] getRules() {
		return rules;
	}

	public void setRules(String[] rules) {
		this.rules = rules;
	}

	public String[] getStrategies() {
		return strategies;
	}

	public void setStrategies(String[] strategies) {
		this.strategies = strategies;
	}    
    
}
