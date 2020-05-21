package com.pgmmers.radar.dal.bean;

import com.pgmmers.radar.dal.bean.PageQuery;

public class RuleHistoryQuery extends PageQuery {

	private Long ruleId;

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
}
