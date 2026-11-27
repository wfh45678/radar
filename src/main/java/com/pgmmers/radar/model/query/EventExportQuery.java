package com.pgmmers.radar.model.query;

public class EventExportQuery extends PageQuery {
	
	  private String[] fields;
	  
	  private String[] preItems;
	  
	  private String[] rules;
	  
	  private String[] strategies;

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public String[] getPreItems() {
		return preItems;
	}

	public void setPreItems(String[] preItems) {
		this.preItems = preItems;
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
