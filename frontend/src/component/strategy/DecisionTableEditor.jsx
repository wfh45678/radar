import React,{Component} from 'react';
import {Row,Col,Input,Button,Card,Select,InputNumber,Cascader,Tag,Tooltip,message,Spin} from 'antd';
import {SaveOutlined,ArrowLeftOutlined,HistoryOutlined,PlusOutlined,DeleteOutlined,QuestionCircleOutlined} from "@ant-design/icons";
import {withParams} from '../utils/routerUtil';
import { Link } from 'react-router';
import {FetchUtil} from '../utils/fetchUtil';
import {strategyTypeOf} from './strategyTypes';
import DefinitionHistoryModal from './DefinitionHistoryModal';

const DECISIONS = [{label:'通过(pass)',value:'pass'},{label:'人工审核(review)',value:'review'},{label:'拒绝(reject)',value:'reject'}];
const ROW_OPERATORS = ['Less','Less_Equal','Greater','Greater_Equal','Equal','NotEqual','Contains','StartsWith','EndsWith','InList','NotInList','IsNull','IsNotNull'];

/**
 * 决策表编辑器（v5.1）：编辑策略 DEFINITION JSON（rows 条件矩阵，行优先首中直出决策，
 * 无命中走缺省决策），保存 PUT /strategy/；执行由后端 DecisionTableExecutor 解释。
 * definition 结构见后端 DecisionTableExecutor javadoc。
 */
class DecisionTableEditor extends Component{

    constructor(props){
        super(props);
        this.state={
            loading:true,
            saving:false,
            strategy:null,
            defaultDecision:'pass',
            defaultScore:0,
            rows:[],
            fieldList:[],
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
                    defaultDecision:definition.defaultDecision||'pass',
                    defaultScore:definition.defaultScore===undefined?0:definition.defaultScore,
                    rows:definition.rows||[],
                    loading:false
                });
            });
        FetchUtil('/feature/columns/'+this.props.params.id,'GET','',
            (data) => {
                this.setState({fieldList:data.data.list});
            });
    }

    updateState=(key,value)=>{
        this.setState({[key]:value});
    }

    addRow=()=>{
        const rows=[...this.state.rows];
        rows.push({label:'行'+(rows.length+1),conditions:[{column:'',operator:'Greater',value:''}],decision:'review',score:50,reason:''});
        this.setState({rows});
    }

    removeRow=(ri)=>{
        const rows=[...this.state.rows];
        rows.splice(ri,1);
        this.setState({rows});
    }

    updateRow=(ri,key,value)=>{
        const rows=[...this.state.rows];
        rows[ri]={...rows[ri],[key]:value};
        this.setState({rows});
    }

    addCondition=(ri)=>{
        const rows=[...this.state.rows];
        rows[ri]={...rows[ri],conditions:[...(rows[ri].conditions||[]),{column:'',operator:'Greater',value:''}]};
        this.setState({rows});
    }

    removeCondition=(ri,ci)=>{
        const rows=[...this.state.rows];
        const conditions=[...(rows[ri].conditions||[])];
        conditions.splice(ci,1);
        rows[ri]={...rows[ri],conditions};
        this.setState({rows});
    }

    updateCondition=(ri,ci,key,value)=>{
        const rows=[...this.state.rows];
        const conditions=[...(rows[ri].conditions||[])];
        conditions[ci]={...conditions[ci],[key]:value};
        rows[ri]={...rows[ri],conditions};
        this.setState({rows});
    }

    validate=()=>{
        const rows=this.state.rows;
        if(rows.length===0){ return '决策表为空，请至少添加一行'; }
        for(let ri=0;ri<rows.length;ri++){
            const row=rows[ri];
            if(!row.conditions||row.conditions.length===0){ return '行「'+(row.label||ri+1)+'」缺少条件'; }
            for(let ci=0;ci<row.conditions.length;ci++){
                const c=row.conditions[ci];
                if(!c.column){ return '行「'+(row.label||ri+1)+'」第'+(ci+1)+'个条件缺少变量'; }
            }
        }
        return null;
    }

    handleSubmit=()=>{
        const error=this.validate();
        if(error){
            message.warning(error);
            return;
        }
        const strategy=this.state.strategy;
        const definition={
            defaultDecision:this.state.defaultDecision,
            defaultScore:Number(this.state.defaultScore)||0,
            rows:this.state.rows.map(r=>({
                label:r.label,
                conditions:(r.conditions||[]).map(c=>({
                    column:c.column,
                    operator:c.operator,
                    value:String(c.value)
                })),
                decision:r.decision,
                score:Number(r.score)||0,
                reason:r.reason
            }))
        };
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
            definition:definition
        };
        this.setState({saving:true});
        FetchUtil('/strategy/','PUT',JSON.stringify(param),
            (data) => {
                this.setState({saving:false});
                if(data.success){
                    message.success('决策表已保存，保存即生效');
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
        return (
            <div className="ant-layout-content">
                <div id="header">
                    <Link to={'/strategyList/'+this.props.params.id}><ArrowLeftOutlined /> 返回策略列表</Link>
                    <Tag color={typeInfo.color} style={{marginLeft:12}}>{typeInfo.label}</Tag>
                    <span style={{fontSize:18,fontWeight:'bold',marginLeft:8}}>{this.state.strategy.strategyName}</span>
                    <Tooltip title={<span>决策行按顺序求值，行内条件全部成立即命中该行并直出决策；无行命中走缺省决策。</span>}>
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
                    <Card size="small" title="缺省决策（无行命中时）" style={{marginBottom:12}}>
                        <span style={{marginRight:4}}>决策：</span>
                        <Select style={{width:160}} value={this.state.defaultDecision}
                            onChange={(v)=>this.updateState('defaultDecision',v)}
                            options={DECISIONS}/>
                        <span style={{margin:'0 4px 0 16px'}}>得分：</span>
                        <InputNumber value={this.state.defaultScore} onChange={(v)=>this.updateState('defaultScore',v)}/>
                    </Card>

                    {this.state.rows.map((row,ri)=>(
                        <Card size="small"
                            title={'第 '+(ri+1)+' 行：'+(row.label||'未命名')}
                            extra={<a onClick={()=>this.removeRow(ri)}><DeleteOutlined /> 删除行</a>}
                            style={{marginBottom:12}} key={ri}>
                            <Row style={{marginBottom:8}}>
                                <Col span={6}><span style={{marginRight:4}}>行名：</span>
                                    <Input style={{width:'70%'}} value={row.label}
                                        onChange={(e)=>this.updateRow(ri,'label',e.target.value)}/></Col>
                                <Col span={5}><span style={{marginRight:4}}>决策：</span>
                                    <Select style={{width:'65%'}} value={row.decision||'review'}
                                        onChange={(v)=>this.updateRow(ri,'decision',v)} options={DECISIONS}/></Col>
                                <Col span={5}><span style={{marginRight:4}}>得分：</span>
                                    <InputNumber value={row.score} onChange={(v)=>this.updateRow(ri,'score',v)}/></Col>
                                <Col span={8}><span style={{marginRight:4}}>理由：</span>
                                    <Input style={{width:'80%'}} value={row.reason}
                                        onChange={(e)=>this.updateRow(ri,'reason',e.target.value)} placeholder="写入命中明细"/></Col>
                            </Row>
                            <div style={{marginBottom:8}}>
                                <span style={{marginRight:8}}>命中条件（AND）：</span>
                                <Button size="small" icon={<PlusOutlined />} onClick={()=>this.addCondition(ri)}>加条件</Button>
                            </div>
                            {(row.conditions||[]).map((c,ci)=>(
                                <Row key={ci} style={{marginBottom:6}}>
                                    <Col span={10} offset={1}>
                                        <Cascader style={{width:'95%'}} options={this.state.fieldList}
                                            allowClear={false}
                                            value={c.column?c.column.split('.'):[]}
                                            displayRender={this.displayRender}
                                            onChange={(v)=>this.updateCondition(ri,ci,'column',v.join('.'))}
                                            placeholder="选择字段/特征"/>
                                    </Col>
                                    <Col span={5}>
                                        <Select style={{width:'95%'}} value={c.operator}
                                            onChange={(v)=>this.updateCondition(ri,ci,'operator',v)}
                                            options={ROW_OPERATORS.map(op=>({label:op,value:op}))}/>
                                    </Col>
                                    <Col span={6}>
                                        <Input value={c.value} onChange={(e)=>this.updateCondition(ri,ci,'value',e.target.value)} placeholder="阈值/比较值"/>
                                    </Col>
                                    <Col span={2}><a onClick={()=>this.removeCondition(ri,ci)}><DeleteOutlined /> 删除</a></Col>
                                </Row>
                            ))}
                        </Card>
                    ))}

                    <Button type="dashed" block icon={<PlusOutlined />} onClick={this.addRow}>添加决策行</Button>
                </Spin>
            </div>
        );
    }
}

export default withParams(DecisionTableEditor);
