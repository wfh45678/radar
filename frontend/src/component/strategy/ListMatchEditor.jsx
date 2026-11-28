import React,{Component} from 'react';
import {Row,Col,Input,Button,Card,Select,InputNumber,Cascader,Tag,Tooltip,message,Spin} from 'antd';
import {SaveOutlined,ArrowLeftOutlined,HistoryOutlined,QuestionCircleOutlined} from '@ant-design/icons';
import {withParams} from '../utils/routerUtil';
import { Link } from 'react-router';
import {FetchUtil} from '../utils/fetchUtil';
import {strategyTypeOf} from './strategyTypes';
import DefinitionHistoryModal from './DefinitionHistoryModal';

const DECISIONS = [{label:'通过(pass)',value:'pass'},{label:'人工审核(review)',value:'review'},{label:'拒绝(reject)',value:'reject'}];

/**
 * 名单匹配编辑器（v5.1）：编辑策略 DEFINITION JSON（dataList/matchField/hitDecision/hitScore/hitReason），
 * 保存 PUT /strategy/；执行由后端 ListMatchExecutor 解释（命中直出决策，未命中放行）。
 */
class ListMatchEditor extends Component{

    constructor(props){
        super(props);
        this.state={
            loading:true,
            saving:false,
            strategy:null,
            definition:{
                dataList:'',
                matchField:'',
                hitDecision:'reject',
                hitScore:100,
                hitReason:'命中名单'
            },
            fieldList:[],
            dataList:[],
            historyOpen:false
        }
    }

    componentDidMount() {
        FetchUtil('/strategy/'+this.props.params.strategyId,'GET','',
            (data) => {
                const strategy=data.data.strategy;
                const definition=strategy.definition||{};
                this.setState({
                    strategy:strategy,
                    definition:{
                        dataList:definition.dataList||'',
                        matchField:definition.matchField||'',
                        hitDecision:definition.hitDecision||'reject',
                        hitScore:definition.hitScore===undefined?100:definition.hitScore,
                        hitReason:definition.hitReason||'命中名单',
                        missDecision:definition.missDecision||'pass'
                    },
                    loading:false
                });
            });
        FetchUtil('/feature/columns/'+this.props.params.id,'GET','',
            (data) => {
                this.setState({fieldList:data.data.list});
            });
        FetchUtil('/datalist/list/'+this.props.params.id,'GET','',
            (data) => {
                this.setState({dataList:data.data.list||[]});
            });
    }

    updateField=(key,value)=>{
        const definition={...this.state.definition};
        definition[key]=value;
        this.setState({definition});
    }

    validate=()=>{
        const d=this.state.definition;
        if(!d.dataList){ return '请选择数据名单'; }
        if(!d.matchField){ return '请选择匹配字段'; }
        return null;
    }

    handleSubmit=()=>{
        const error=this.validate();
        if(error){
            message.warning(error);
            return;
        }
        const strategy=this.state.strategy;
        const param={
            id:strategy.id,
            scenarioId:strategy.scenarioId,
            strategyName:strategy.strategyName,
            label:strategy.label,
            comment:strategy.comment,
            bottom:'0',
            median:strategy.median,
            high:strategy.high,
            type:strategy.type,
            status:strategy.status,
            definition:this.state.definition
        };
        this.setState({saving:true});
        FetchUtil('/strategy/','PUT',JSON.stringify(param),
            (data) => {
                this.setState({saving:false});
                if(data.success){
                    message.success('名单匹配已保存，保存即生效');
                }else{
                    message.error(data.msg);
                }
            });
    }

    displayRender=(labels)=>{
        return labels.join('.');
    }

    render(){
        if(this.state.loading){
            return (
                <div className="ant-layout-content" style={{textAlign:'center',paddingTop:80}}>
                    <Spin size="large"/>
                </div>
            );
        }
        const typeInfo=strategyTypeOf(this.state.strategy?this.state.strategy.type:null);
        const d=this.state.definition;
        return (
            <div className="ant-layout-content">
                <div id="header">
                    <Link to={'/strategyList/'+this.props.params.id}><ArrowLeftOutlined /> 返回策略列表</Link>
                    <Tag color={typeInfo.color} style={{marginLeft:12}}>{typeInfo.label}</Tag>
                    <span style={{fontSize:18,fontWeight:'bold',marginLeft:8}}>{this.state.strategy.strategyName}</span>
                    <Tooltip title={<span>事件字段值比对预加载数据名单：命中直出命中决策，未命中放行。</span>}>
                        <QuestionCircleOutlined style={{marginLeft:8,color:'#999'}}/>
                    </Tooltip>
                    <span style={{float:'right'}}>
                        <Button icon={<HistoryOutlined/>} onClick={()=>this.setState({historyOpen:true})}>变更历史</Button>
                        <Button type="primary" icon={<SaveOutlined/>} loading={this.state.saving} onClick={this.handleSubmit} style={{marginLeft:8}}>保存</Button>
                    </span>
                </div>

                {this.state.historyOpen
                    ? <DefinitionHistoryModal strategyId={this.props.params.strategyId}
                        open={this.state.historyOpen} onClose={()=>this.setState({historyOpen:false})}/>
                    : null}

                <Spin size="large" spinning={this.state.loading}>
                    <Card size="small" title="匹配规则" style={{marginBottom:12}}>
                        <Row style={{marginBottom:12}}>
                            <Col span={10}>
                                <span style={{marginRight:4}}>数据名单：</span>
                                <Select style={{width:'70%'}} placeholder="选择名单"
                                    value={d.dataList||undefined}
                                    onChange={(v)=>this.updateField('dataList',v)}
                                    options={this.state.dataList.map(op=>({label:op.label,value:op.name}))}/>
                            </Col>
                            <Col span={14}>
                                <span style={{marginRight:4}}>事件匹配字段：</span>
                                <Cascader style={{width:'70%'}} options={this.state.fieldList}
                                    allowClear={false}
                                    value={d.matchField?d.matchField.split('.'):[]}
                                    displayRender={this.displayRender}
                                    onChange={(v)=>this.updateField('matchField',v.join('.'))}
                                    placeholder="选择字段/特征"/>
                            </Col>
                        </Row>
                        <Row style={{marginBottom:8}}>
                            <Col span={5}>
                                <span style={{marginRight:4}}>命中决策：</span>
                                <Select style={{width:'60%'}} value={d.hitDecision}
                                    onChange={(v)=>this.updateField('hitDecision',v)}
                                    options={DECISIONS}/>
                            </Col>
                            <Col span={5}>
                                <span style={{marginRight:4}}>命中得分：</span>
                                <InputNumber value={d.hitScore} onChange={(v)=>this.updateField('hitScore',v)}/>
                            </Col>
                            <Col span={14}>
                                <span style={{marginRight:4}}>命中理由：</span>
                                <Input style={{width:'80%'}} value={d.hitReason}
                                    onChange={(e)=>this.updateField('hitReason',e.target.value)} placeholder="写入命中明细的理由"/>
                            </Col>
                        </Row>
                        <span style={{color:'#999'}}>未命中时放行（pass/0）。名单在黑/白名单管理页维护，保存即生效（缓存自动刷新）。</span>
                    </Card>
                </Spin>
            </div>
        );
    }
}

export default withParams(ListMatchEditor);
