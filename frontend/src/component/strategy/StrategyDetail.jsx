import React,{Component} from 'react';
import {Spin} from 'antd';
import {withParams} from '../utils/routerUtil';
import {FetchUtil} from '../utils/fetchUtil';

import RuleList from './RuleList';
import ScorecardEditor from './ScorecardEditor';
import DecisionTreeEditor from './DecisionTreeEditor';

/**
 * 策略详情分发页（v4.0）：按策略 type 渲染对应编辑器——
 * RULE_SET → RuleList（规则列表，v3.x 界面）；SCORECARD → 评分卡编辑器；DECISION_TREE → 决策树编辑器。
 * 路由 #/strategyDetail/:id/:strategyId（:id 为场景 id，参数名与 ruleList 保持一致以复用子组件）。
 */
class StrategyDetail extends Component{

    constructor(props){
        super(props);
        this.state={
            loading:true,
            type:null,
            strategy:null
        }
    }

    componentDidMount() {
        FetchUtil('/strategy/'+this.props.params.strategyId,'GET','',
            (data) => {
                this.setState({
                    type: data.data.strategy.type || 'RULE_SET',
                    strategy: data.data.strategy,
                    loading: false
                });
            });
    }

    render(){
        if(this.state.loading){
            return (
                <div className="ant-layout-content" style={{textAlign:'center',paddingTop:80}}>
                    <Spin size="large"/>
                </div>
            );
        }
        switch(this.state.type){
            case 'SCORECARD':
                return <ScorecardEditor/>;
            case 'DECISION_TREE':
                return <DecisionTreeEditor/>;
            default:
                // RULE_SET 与缺省（存量数据）走规则列表
                return <RuleList/>;
        }
    }
}

export default withParams(StrategyDetail);
