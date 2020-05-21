package com.pgmmers.radar.dal.bean;

public class EventExportQuery extends PageQuery {
	
	  private String[] fields;
	  
	  private String[] preItems;
	  
	  private String[] rules;
	  
	  private String[] activations;

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

	public String[] getActivations() {
		return activations;
	}

	public void setActivations(String[] activations) {
		this.activations = activations;
	}    
    
}
