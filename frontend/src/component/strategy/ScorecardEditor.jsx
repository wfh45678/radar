import React,{Component} from 'react';
import {Row,Col,Input,Button,Card,Table,Tooltip,Select,InputNumber,Cascader,Tag,Popconfirm,message,Spin,Empty} from 'antd';
import {PlusOutlined,QuestionCircleOutlined,DeleteOutlined,SaveOutlined,ArrowLeftOutlined,HistoryOutlined} from '@ant-design/icons';
import {withParams} from '../utils/routerUtil';
import { Link } from 'react-router';
import {FetchUtil} from '../utils/fetchUtil';
import {strategyTypeOf} from './strategyTypes';
import DefinitionHistoryModal from './DefinitionHistoryModal';

// 评分卡分箱可用操作符（value 为名单名的走名单下拉，其余走文本输入）
const BIN_OPERATORS = ['Less','Less_Equal','Greater','Greater_Equal','Equal','NotEqual','InList','NotInList','Contains','StartsWith','IsNull','IsNotNull'];

/**
 * 评分卡编辑器（v4.0）：编辑策略 DEFINITION JSON（initialScore + features/bins），
 * 保存 PUT /strategy/；执行由后端 ScoreCardExecutor 解释。
 * definition 结构见后端 ScoreCardExecutor javadoc。
 */
class ScorecardEditor extends Component{

    constructor(props){
        super(props);

        this.state={
            loading:true,
            saving:false,

            strategy:null,       // 后端 VO（label/median/high/status 等）
            initialScore:0,
            features:[],         // [{name,label,column,weight,defaultScore,bins:[{label,operator,value,score}]}]

            fieldList:[],        // 变量级联树（fields/preItems/abstractions）
            dataList:[],         // 名单列表（InList 分箱下拉）

            historyOpen:false    // 定义变更历史弹窗
        }
    }

    componentDidMount() {
        FetchUtil('/strategy/'+this.props.params.strategyId,'GET','',
            (data) => {
                const strategy=data.data.strategy;
                const definition=strategy.definition||{};
                this.setState({
                    strategy:strategy,
                    initialScore:definition.initialScore||0,
                    features:definition.features||[],
                    loading:false
                });
            });
        FetchUtil('/feature/columns/'+this.props.params.id,'GET','',
            (data) => {
                this.setState({
                    fieldList:data.data.list
                });
            });
        FetchUtil('/datalist/list/'+this.props.params.id,'GET','',
            (data) => {
                this.setState({
                    dataList:data.data.list||[]
                });
            });
    }

    displayRender=(label)=>{
        return label.join('.');
    }

    // ===== 特征编辑 =====
    addFeature=()=>{
        const features=this.state.features.concat([{
            name:'feature_'+(this.state.features.length+1)+'_'+Date.now()%1000,
            label:'',
            column:'',
            weight:1,
            defaultScore:0,
            bins:[]
        }]);
        this.setState({features:features});
    }

    removeFeature=(index)=>{
        const features=this.state.features.filter((f,i)=>i!=index);
        this.setState({features:features});
    }

    updateFeature=(index,key,value)=>{
        const features=this.state.features.map((f,i)=>{
            if(i==index){ f[key]=value; }
            return f;
        });
        this.setState({features:features});
    }

    // ===== 分箱编辑 =====
    addBin=(featureIndex)=>{
        const features=this.state.features.map((f,i)=>{
            if(i==featureIndex){
                f.bins=f.bins.concat([{label:'',operator:'Greater_Equal',value:'',score:0}]);
            }
            return f;
        });
        this.setState({features:features});
    }

    removeBin=(featureIndex,binIndex)=>{
        const features=this.state.features.map((f,i)=>{
            if(i==featureIndex){
                f.bins=f.bins.filter((b,j)=>j!=binIndex);
            }
            return f;
        });
        this.setState({features:features});
    }

    updateBin=(featureIndex,binIndex,key,value)=>{
        const features=this.state.features.map((f,i)=>{
            if(i==featureIndex){
                f.bins=f.bins.map((b,j)=>{
                    if(j==binIndex){ b[key]=value; }
                    return b;
                });
            }
            return f;
        });
        this.setState({features:features});
    }

    // ===== 校验与保存 =====
    validate=()=>{
        for(let i=0;i<this.state.features.length;i++){
            const f=this.state.features[i];
            if(!f.label){ return '第'+(i+1)+'个特征请填写特征名'; }
            if(!f.name){ return '第'+(i+1)+'个特征请填写特征标识'; }
            if(!f.column){ return '特征「'+f.label+'」请选择变量'; }
            for(let j=0;j<f.bins.length;j++){
                const b=f.bins[j];
                if(!b.operator){ return '特征「'+f.label+'」分箱'+(j+1)+'请选择操作符'; }
                if(b.value===undefined||b.value===null||b.value===''){ return '特征「'+f.label+'」分箱'+(j+1)+'请填写阈值/名单'; }
                if(b.score===undefined||b.score===null||b.score===''){ return '特征「'+f.label+'」分箱'+(j+1)+'请填写分值'; }
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
            initialScore:Number(this.state.initialScore)||0,
            features:this.state.features.map(f=>({
                name:f.name,
                label:f.label,
                column:f.column,
                weight:Number(f.weight)||1,
                defaultScore:Number(f.defaultScore)||0,
                bins:f.bins.map(b=>({
                    label:b.label,
                    operator:b.operator,
                    value:String(b.value),
                    score:Number(b.score)||0
                }))
            }))
        };
        const param={
            id:strategy.id,
            modelId:strategy.modelId,
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
                    message.success('评分卡已保存，保存即生效');
                }else{
                    message.error(data.msg);
                }
            });
    }

    render(){
        const typeInfo=strategyTypeOf(this.state.strategy?this.state.strategy.type:null);
        const binValueInput=(fi,b,bi)=>{
            const isListOp=b.operator==='InList'||b.operator==='NotInList';
            if(isListOp){
                return (
                    <Select style={{width:150}} dropdownMatchSelectWidth={false}
                        value={b.value||undefined}
                        placeholder="选择名单"
                        onChange={this.updateBin.bind(this,fi,bi,'value')}
                        options={this.state.dataList.map(op=>({label:op.label,value:op.name}))}/>
                );
            }
            return <Input style={{width:150}} value={b.value} onChange={(e)=>this.updateBin(fi,bi,'value',e.target.value)} placeholder="阈值"/>;
        };

        const binColumns=(featureIndex)=>[
            {
                title:'分箱名', dataIndex:'label', width:140,
                render:(t,b,bi)=><Input value={b.label} onChange={(e)=>this.updateBin(featureIndex,bi,'label',e.target.value)} placeholder="如 18-25"/>
            },
            {
                title:'操作符', dataIndex:'operator', width:150,
                render:(t,b,bi)=>(
                    <Select style={{width:130}} value={b.operator}
                        onChange={this.updateBin.bind(this,featureIndex,bi,'operator')}
                        options={BIN_OPERATORS.map(op=>({label:op,value:op}))}/>
                )
            },
            {
                title:'阈值/名单', dataIndex:'value',
                render:(t,b,bi)=>binValueInput(featureIndex,b,bi)
            },
            {
                title:'分值', dataIndex:'score', width:120,
                render:(t,b,bi)=><InputNumber value={b.score} onChange={this.updateBin.bind(this,featureIndex,bi,'score')}/>
            },
            {
                title:'操作', width:60,
                render:(t,b,bi)=>(
                    <Popconfirm title="确认删除该分箱？" onConfirm={this.removeBin.bind(this,featureIndex,bi)}>
                        <a style={{color:'#FD5B5B'}}><DeleteOutlined/></a>
                    </Popconfirm>
                )
            }
        ];

        return (
            <div className="ant-layout-content">
                <div id="header">
                    <Row>
                        <Col span={16}>
                            <Link to={"/strategy/"+this.props.params.id}><ArrowLeftOutlined/> 返回策略列表</Link>
                            {' '}<Tag color={typeInfo.color}>评分卡</Tag>
                            <span style={{fontWeight:'bold',fontSize:15}}>{this.state.strategy?this.state.strategy.label:''}</span>
                            <Tooltip placement="right" title={'总分 = 初始分 + Σ(特征首个命中分箱分 × 权重)；总分与策略警戒值/拒绝值比较得出决策'}>
                                <QuestionCircleOutlined style={{fontSize:15,marginLeft:8}}/>
                            </Tooltip>
                        </Col>
                        <Col span={8} style={{textAlign:'right'}}>
                            <Button icon={<HistoryOutlined/>} style={{marginRight:8}}
                                onClick={()=>this.setState({historyOpen:true})}>变更历史</Button>
                            <Button type="primary" icon={<SaveOutlined/>} loading={this.state.saving} onClick={this.handleSubmit}>保存</Button>
                        </Col>
                    </Row>
                </div>

                {this.state.historyOpen
                    ? <DefinitionHistoryModal strategyId={this.props.params.strategyId}
                        open={this.state.historyOpen} onClose={()=>this.setState({historyOpen:false})}/>
                    : null}

                <Spin size="large" spinning={this.state.loading}>
                    <Card size="small" title="初始分" style={{marginBottom:12}}>
                        <InputNumber value={this.state.initialScore} onChange={(v)=>this.setState({initialScore:v})}/>
                        <span style={{color:'#999',marginLeft:8}}>评分卡基础分（如信用分离典型案例 600）</span>
                    </Card>

                    {this.state.features.map((f,fi)=>(
                        <Card size="small" style={{marginBottom:12}}
                            title={<span>特征 {fi+1}：{f.label||'（未命名）'}</span>}
                            extra={
                                <Popconfirm title="确认删除该特征？" onConfirm={this.removeFeature.bind(this,fi)}>
                                    <a style={{color:'#FD5B5B'}}><DeleteOutlined/> 删除特征</a>
                                </Popconfirm>
                            }
                            key={fi}>
                            <Row gutter={12}>
                                <Col span={5}>
                                    <Input addonBefore="特征名" value={f.label}
                                        onChange={(e)=>this.updateFeature(fi,'label',e.target.value)} placeholder="中文名"/>
                                </Col>
                                <Col span={5}>
                                    <Input addonBefore="标识" value={f.name}
                                        onChange={(e)=>this.updateFeature(fi,'name',e.target.value)} placeholder="英文标识"/>
                                </Col>
                                <Col span={7}>
                                    <span style={{marginRight:4}}>变量：</span>
                                    <Cascader style={{width:'80%'}} options={this.state.fieldList}
                                        allowClear={false}
                                        value={f.column?f.column.split('.'):[]}
                                        displayRender={this.displayRender}
                                        onChange={(v)=>this.updateFeature(fi,'column',v.join('.'))}
                                        placeholder="选择字段/指标"/>
                                </Col>
                                <Col span={3}>
                                    <span style={{marginRight:4}}>权重：</span>
                                    <InputNumber step={0.1} min={0} value={f.weight} onChange={this.updateFeature.bind(this,fi,'weight')}/>
                                </Col>
                                <Col span={4}>
                                    <span style={{marginRight:4}}>默认分：</span>
                                    <InputNumber value={f.defaultScore} onChange={this.updateFeature.bind(this,fi,'defaultScore')}/>
                                </Col>
                            </Row>
                            <div style={{marginTop:10}}>
                                <Table size="small" rowKey={(b,bi)=>bi} columns={binColumns(fi)}
                                    dataSource={f.bins} pagination={false}
                                    locale={{emptyText:<Empty description="暂无分箱，无命中时取默认分"/>}}/>
                                <Button size="small" type="dashed" icon={<PlusOutlined/>}
                                    style={{marginTop:8}} onClick={this.addBin.bind(this,fi)}>添加分箱</Button>
                                <span style={{color:'#999',marginLeft:8}}>分箱按顺序求值，首个命中生效，建议区间互斥</span>
                            </div>
                        </Card>
                    ))}

                    <Button type="dashed" block icon={<PlusOutlined/>} onClick={this.addFeature}>添加特征</Button>
                </Spin>
            </div>
        );
    }
}

export default withParams(ScorecardEditor);
