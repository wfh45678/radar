package com.pgmmers.radar.model.vo.model;

import java.io.Serializable;

public class RuleHitsVO implements Comparable<RuleHitsVO> ,Serializable{
    static final long serialVersionUID = 542326574L;

    private long id;

    private String strategyName;
    
    private String strategyLable;
    
    private String ruleLable;

    private long count;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    @Override
    public int compareTo(RuleHitsVO obj) {
        if (this.count > obj.getCount()) {
            return 1;
        } else {
            return -1;
        }
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuleHitsVO) {
            RuleHitsVO tmp = (RuleHitsVO) obj;
            if (tmp.getId() == this.id) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getStrategyLable() {
        return strategyLable;
    }

    public void setStrategyLable(String strategyLable) {
        this.strategyLable = strategyLable;
    }

    public String getRuleLable() {
        return ruleLable;
    }

    public void setRuleLable(String ruleLable) {
        this.ruleLable = ruleLable;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    
}
