import React,{Component} from 'react';
import {Row,Col,Input,Button,Card,Tooltip,Select,InputNumber,Tag,Popconfirm,message,Spin,Tree,Empty,Divider} from 'antd';
import {PlusOutlined,QuestionCircleOutlined,DeleteOutlined,SaveOutlined,ArrowLeftOutlined,BranchesOutlined,FlagOutlined,ApartmentOutlined,HistoryOutlined} from '@ant-design/icons';
import {withParams} from '../utils/routerUtil';
import { Link } from 'react-router';
import {FetchUtil} from '../utils/fetchUtil';
import {strategyTypeOf} from './strategyTypes';
import {validateRules} from '../utils/groovyUtil';
import ComplexCondition from '../abstraction/ComplexCondition';
import DefinitionHistoryModal from './DefinitionHistoryModal';

const DECISIONS = [
    {value:'pass', label:'通过(pass)', color:'green'},
    {value:'review', label:'审核(review)', color:'orange'},
    {value:'reject', label:'拒绝(reject)', color:'red'}
];

/**
 * 决策树编辑器（v4.0）：编辑策略 DEFINITION JSON（rootId + nodes 扁平节点表），
 * 保存 PUT /strategy/；执行由后端 DecisionTreeExecutor 解释（叶节点直出决策）。
 *
 * 布局：左侧 antd Tree 结构预览（画布版列入后续规划），右侧选中节点编辑面板；
 * 条件编辑复用 ComplexCondition/SimpleCondition 条件树组件（与规则编辑器同源）。
 */
class DecisionTreeEditor extends Component{

    constructor(props){
        super(props);

        this.state={
            loading:true,
            saving:false,

            strategy:null,
            rootId:null,
            nodes:[],          // 扁平节点表：CONDITION {id,type,label,condition,trueChild,falseChild} / LEAF {id,type,label,decision,score,reason}
            selectedId:null,

            fieldList:[],
            dataList:[],

            historyOpen:false  // 定义变更历史弹窗
        }
    }

    componentDidMount() {
        FetchUtil('/strategy/'+this.props.params.strategyId,'GET','',
            (data) => {
                const strategy=data.data.strategy;
                const definition=strategy.definition||{};
                this.setState({
                    strategy:strategy,
                    rootId:definition.rootId||null,
                    nodes:definition.nodes||[],
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

    // ===== 节点增删改 =====
    genId=()=>{
        let id='n'+Date.now();
        while(this.state.nodes.some(n=>n.id===id)){ id+='x'; }
        return id;
    }

    addNode=(type)=>{
        const node=type==='LEAF'
            ? {id:this.genId(),type:'LEAF',label:'',decision:'pass',score:0,reason:''}
            : {id:this.genId(),type:'CONDITION',label:'',condition:null,trueChild:'',falseChild:''};
        this.setState({nodes:this.state.nodes.concat([node]),selectedId:node.id});
    }

    // 将新节点挂接到选中节点的空分支
    addChildToSelected=(type)=>{
        const selected=this.state.nodes.find(n=>n.id===this.state.selectedId);
        if(!selected||selected.type!=='CONDITION'){
            message.warning('请先选择一个条件节点作为父节点');
            return;
        }
        const branch=!selected.trueChild?'trueChild':(!selected.falseChild?'falseChild':null);
        if(!branch){
            message.warning('该节点两个分支均已挂接子节点');
            return;
        }
        const node=type==='LEAF'
            ? {id:this.genId(),type:'LEAF',label:'',decision:'pass',score:0,reason:''}
            : {id:this.genId(),type:'CONDITION',label:'',condition:null,trueChild:'',falseChild:''};
        this.setState({
            nodes:this.state.nodes.concat([node]).map(n=>{
                if(n.id===selected.id){ n[branch]=node.id; }
                return n;
            }),
            selectedId:node.id
        });
    }

    removeNode=(id)=>{
        // 清除父节点对该节点的引用与根引用
        const nodes=this.state.nodes
            .filter(n=>n.id!==id)
            .map(n=>{
                if(n.trueChild===id){ n.trueChild=''; }
                if(n.falseChild===id){ n.falseChild=''; }
                return n;
            });
        const rootId=this.state.rootId===id?null:this.state.rootId;
        this.setState({nodes:nodes,rootId:rootId,selectedId:null});
    }

    updateNode=(id,key,value)=>{
        const nodes=this.state.nodes.map(n=>{
            if(n.id===id){ n[key]=value; }
            return n;
        });
        this.setState({nodes:nodes});
    }

    handleChangeCondition=(condition)=>{
        this.updateNode(this.state.selectedId,'condition',condition);
    }

    // ===== 结构工具 =====
    findParent=(id)=>{
        return this.state.nodes.find(n=>n.trueChild===id||n.falseChild===id)||null;
    }

    nodeById=(id)=>{
        return this.state.nodes.find(n=>n.id===id)||null;
    }

    buildTreeData=()=>{
        const title=(n)=>{
            const isRoot=n.id===this.state.rootId;
            const color=n.type==='LEAF'?(DECISIONS.find(d=>d.value===n.decision)||{color:'green'}).color:'blue';
            return (
                <span onClick={()=>this.setState({selectedId:n.id})}>
                    {n.type==='LEAF'?<FlagOutlined style={{color:color}}/>:<BranchesOutlined style={{color:'#1890ff'}}/>}
                    {' '}<Tag color={color}>{n.type==='LEAF'?(DECISIONS.find(d=>d.value===n.decision)||{label:'叶'}).label:'条件'}</Tag>
                    {n.label||'（未命名）'}
                    {isRoot?<Tag color="purple">根</Tag>:null}
                    <span style={{color:'#bbb',fontSize:12,marginLeft:4}}>{n.id}</span>
                </span>
            );
        };
        const toTreeData=(n)=>{
            const children=[];
            if(n.type==='CONDITION'){
                const t=this.nodeById(n.trueChild);
                const f=this.nodeById(n.falseChild);
                if(t){ children.push({key:t.id,title:title(t),children:toTreeData(t)}); }
                if(f){ children.push({key:f.id,title:title(f),children:toTreeData(f)}); }
            }
            return children;
        };
        const root=this.nodeById(this.state.rootId);
        const treeData=[];
        if(root){
            treeData.push({key:root.id,title:title(root),children:toTreeData(root)});
        }
        // 未挂接节点分组
        const attached=new Set();
        const walk=(n)=>{ attached.add(n.id); if(n.type==='CONDITION'){ const t=this.nodeById(n.trueChild); const f=this.nodeById(n.falseChild); if(t){walk(t);} if(f){walk(f);} } };
        if(root){ walk(root); }
        const orphans=this.state.nodes.filter(n=>!attached.has(n.id));
        if(orphans.length>0){
            treeData.push({key:'__orphans__',title:<span><Tag color="orange">未挂接</Tag>游离节点</span>,
                children:orphans.map(n=>({key:n.id,title:title(n),children:[]}))});
        }
        return treeData;
    }

    // ===== 校验与保存 =====
    validate=()=>{
        const nodes=this.state.nodes;
        if(nodes.length===0){ return '决策树为空，请先添加节点'; }
        if(!this.state.rootId||!this.nodeById(this.state.rootId)){ return '请设置根节点'; }
        if(!nodes.some(n=>n.type==='LEAF')){ return '至少需要一个叶节点'; }
        for(const n of nodes){
            if(n.type==='CONDITION'){
                if(!n.condition||!validateRules(n.condition)){ return '条件节点「'+(n.label||n.id)+'」条件不完整'; }
                if(!n.trueChild||!this.nodeById(n.trueChild)){ return '条件节点「'+(n.label||n.id)+'」缺少"是"分支'; }
                if(!n.falseChild||!this.nodeById(n.falseChild)){ return '条件节点「'+(n.label||n.id)+'」缺少"否"分支'; }
            }else{
                if(DECISIONS.every(d=>d.value!==n.decision)){ return '叶节点「'+(n.label||n.id)+'」决策非法'; }
            }
        }
        // 无环 + 全部可达（从根递归走查，"是/否"分支均遍历；visited 同时作环防护）
        const visited=new Set();
        let cycle=false;
        const walkCheck=(n)=>{
            if(!n){ return; }
            if(visited.has(n.id)){ cycle=true; return; }
            visited.add(n.id);
            if(n.type==='CONDITION'){
                walkCheck(this.nodeById(n.trueChild));
                walkCheck(this.nodeById(n.falseChild));
            }
        };
        walkCheck(this.nodeById(this.state.rootId));
        if(cycle){ return '决策树存在环路或重复挂接的节点'; }
        const unreachable=nodes.filter(n=>!visited.has(n.id));
        if(unreachable.length>0){
            return '存在从根不可达的节点：'+unreachable.map(n=>n.label||n.id).join('、');
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
            rootId:this.state.rootId,
            nodes:this.state.nodes.map(n=>n.type==='LEAF'
                ? {id:n.id,type:'LEAF',label:n.label,decision:n.decision,score:Number(n.score)||0,reason:n.reason}
                : {id:n.id,type:'CONDITION',label:n.label,condition:n.condition,trueChild:n.trueChild,falseChild:n.falseChild})
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
                    message.success('决策树已保存，保存即生效');
                }else{
                    message.error(data.msg);
                }
            });
    }

    // ===== 渲染 =====
    renderNodePanel=()=>{
        const node=this.nodeById(this.state.selectedId);
        if(!node){
            return <Empty description="从左侧选择节点，或添加新节点"/>
        }
        const parent=this.findParent(node.id);
        return (
            <div>
                <Divider plain style={{margin:'4px 0 12px'}}>{node.id}</Divider>
                <Row gutter={8} style={{marginBottom:10}}>
                    <Col span={12}>
                        <Input addonBefore="节点名" value={node.label}
                            onChange={(e)=>this.updateNode(node.id,'label',e.target.value)}/>
                    </Col>
                    <Col span={12} style={{textAlign:'right'}}>
                        {this.state.rootId!==node.id
                            ? <Button size="small" onClick={()=>this.setState({rootId:node.id})}>设为根节点</Button>
                            : <Tag color="purple">根节点</Tag>}
                        <Popconfirm title="确认删除该节点？" onConfirm={this.removeNode.bind(this,node.id)}>
                            <Button size="small" danger style={{marginLeft:8}}><DeleteOutlined/></Button>
                        </Popconfirm>
                    </Col>
                </Row>

                {node.type==='CONDITION'?(
                    <div>
                        <Card size="small" title="条件" style={{marginBottom:10}}>
                            {node.condition
                                ? <ComplexCondition fieldList={this.state.fieldList} dataList={this.state.dataList}
                                    condition={node.condition} changeParentCondition={this.handleChangeCondition} index={0}/>
                                : <Empty description={<span>尚未配置条件</span>}>
                                    <Button type="primary" onClick={()=>{
                                        this.updateNode(node.id,'condition',{class:'PDCT',enabled:true,linking:'All',conditions:[]});
                                    }}>添加条件</Button>
                                </Empty>}
                        </Card>
                        <Row gutter={8}>
                            <Col span={12}>
                                <span>满足时（是）→ </span>
                                <Select style={{width:'60%'}} allowClear placeholder="选择子节点"
                                    value={node.trueChild||undefined}
                                    onChange={(v)=>this.updateNode(node.id,'trueChild',v||'')}
                                    options={this.state.nodes.filter(n=>n.id!==node.id).map(n=>({label:(n.label||n.id)+'（'+(n.type==='LEAF'?'叶':'条件')+'）',value:n.id}))}/>
                            </Col>
                            <Col span={12}>
                                <span>不满足时（否）→ </span>
                                <Select style={{width:'60%'}} allowClear placeholder="选择子节点"
                                    value={node.falseChild||undefined}
                                    onChange={(v)=>this.updateNode(node.id,'falseChild',v||'')}
                                    options={this.state.nodes.filter(n=>n.id!==node.id).map(n=>({label:(n.label||n.id)+'（'+(n.type==='LEAF'?'叶':'条件')+'）',value:n.id}))}/>
                            </Col>
                        </Row>
                        <div style={{marginTop:10}}>
                            <Button size="small" type="dashed" icon={<PlusOutlined/>} onClick={this.addChildToSelected.bind(this,'CONDITION')}>添加条件子节点</Button>
                            {' '}<Button size="small" type="dashed" icon={<PlusOutlined/>} onClick={this.addChildToSelected.bind(this,'LEAF')}>添加叶子子节点</Button>
                        </div>
                    </div>
                ):(
                    <div>
                        <Row gutter={8} style={{marginBottom:10}}>
                            <Col span={12}>
                                <span>决策：</span>
                                <Select style={{width:'60%'}} value={node.decision}
                                    onChange={(v)=>this.updateNode(node.id,'decision',v)}
                                    options={DECISIONS.map(d=>({label:d.label,value:d.value}))}/>
                            </Col>
                            <Col span={12}>
                                <span>分值：</span>
                                <InputNumber value={node.score} onChange={(v)=>this.updateNode(node.id,'score',v)}/>
                            </Col>
                        </Row>
                        <Input addonBefore="理由" value={node.reason}
                            onChange={(e)=>this.updateNode(node.id,'reason',e.target.value)}
                            placeholder="写入命中明细 desc，如：命中白名单"/>
                        <div style={{marginTop:10,color:'#999'}}>
                            父节点：{parent?(parent.label||parent.id):'（未挂接，游离节点）'}
                        </div>
                    </div>
                )}
            </div>
        );
    }

    render(){
        const typeInfo=strategyTypeOf(this.state.strategy?this.state.strategy.type:null);
        return (
            <div className="ant-layout-content">
                <div id="header">
                    <Row>
                        <Col span={16}>
                            <Link to={"/strategy/"+this.props.params.id}><ArrowLeftOutlined/> 返回策略列表</Link>
                            {' '}<Tag color={typeInfo.color}>决策树</Tag>
                            <span style={{fontWeight:'bold',fontSize:15}}>{this.state.strategy?this.state.strategy.label:''}</span>
                            <Tooltip placement="right" title={'从根节点按条件走路径到叶节点，叶节点直接决定 通过/审核/拒绝'}>
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
                    <Row gutter={12}>
                        <Col span={10}>
                            <Card size="small" title={<span><ApartmentOutlined/> 树结构</span>}
                                extra={
                                    <span>
                                        <Button size="small" type="dashed" icon={<PlusOutlined/>} onClick={this.addNode.bind(this,'CONDITION')}>条件节点</Button>
                                        {' '}<Button size="small" type="dashed" icon={<PlusOutlined/>} onClick={this.addNode.bind(this,'LEAF')}>叶节点</Button>
                                    </span>
                                }>
                                {this.buildTreeData().length>0
                                    ? <Tree treeData={this.buildTreeData()}
                                        selectedKeys={this.state.selectedId?[this.state.selectedId]:[]}
                                        onSelect={(keys)=>{ if(keys.length>0){ this.setState({selectedId:keys[0]}); } }}
                                        defaultExpandAll/>
                                    : <Empty description="空树：添加条件节点作为起点"/>
                                }
                            </Card>
                        </Col>
                        <Col span={14}>
                            <Card size="small" title="节点编辑">
                                {this.renderNodePanel()}
                            </Card>
                        </Col>
                    </Row>
                </Spin>
            </div>
        );
    }
}

export default withParams(DecisionTreeEditor);
