package com.pgmmers.radar.model.query;

import com.pgmmers.radar.model.query.PageQuery;

public class RuleHistoryQuery extends PageQuery {

	private Long ruleId;

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
}
