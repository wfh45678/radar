import React,{Component} from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message,Icon,Spin} from 'antd';
import CollapseGroup from '../common/CollapseGroup';
import Collapse from '../common/Collapse';
const FormItem = Form.Item;
const Option = Select.Option;

import Rule from './Rule';
import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';
import {fetchVersion} from '../utils/fetchUtil';

export default class RuleList extends Component{
    constructor(props){
        super(props);

        this.state={
            label:'',
            status:"1",

            tData:[],
            pageNo:1,
            rowCount:0,

            loading:true,

            model:null,
            fieldList:[],
            dataList:[],

            activation:null,
            ruleOrder:[]
        }

        FetchUtil('/model/'+this.props.params.id,'GET','',
        	(data) => {
                const model=data.data.model;
                this.setState({
                    model:model
                });
            });
    }

    // 获取表格数据
    fetchTableData=()=>{
        const pageSize=1000;
        this.setState({loading:true});

        var param={};
        param.pageNo=this.state.pageNo;
        param.pageSize=pageSize;
        param.activationId=this.props.params.activationId;
        param.label=this.state.label;

        FetchUtil('/rule','POST',JSON.stringify(param),
        	(data) => {
                this.setState({loading:false});
                let ruleOrder=data.data.ruleOrder?data.data.ruleOrder.split(','):[];
                let ruleList=this.getOrderedRules(ruleOrder,data.data.page.list);
                if(data.data.page.list.length>0||ruleOrder.length>ruleList.length){
                    let unOrderedList=data.data.page.list;
                    ruleList=ruleList.concat(unOrderedList);
                    ruleOrder=ruleList.map(x=>x.id+'');
                    this.handleReOrder(ruleOrder);
                }
                this.setState({
                    tData:ruleList,
                    ruleOrder:ruleOrder
                }) 
            });
    }

    selectPage=(page)=>{
        this.setState({
            pageNo:page
        },()=>{this.fetchTableData()});
    }

    handleChange=(e)=>{
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

    handleSearch=()=>{
        this.setState({
            pageNo:1
        },()=>{this.fetchTableData()});
    }

    handleAdd=()=>{
        let tData=this.state.tData;

        tData.push({
            id:0,
            label:'',
			initScore:'0',
			baseNum:'0',
			operator:'NONE',
			abstractionName:'',
			rate:'100',
			ruleDefinition:null,
			scripts:''
        });

        this.setState({
            tData:tData
        })
    }

    handleDelete=(index)=>{
        let tData=this.state.tData;
        let id=tData[index].id;
        if(id!=0){
            FetchUtil('/rule/','DELETE','['+id+']',
            	(data) => {
                	if(data.success==true){
                    	message.success('删除成功!');
                	}
                	else{
                		message.error('删除失败！');
                	}
                    this.fetchTableData();
                });
        }
        else{
            tData.splice(index,1);
            this.setState({
                tData:tData
            });
        }
    }

    handleSwitch=(rule)=>{
        rule.status=(rule.status==0?1:0);

        FetchUtil('/rule/','PUT',JSON.stringify(rule),
            (data) => {
                if(data.success==true){
                    if(rule.status==1){
                        message.success('启用成功!');
                    }
                    else{
                        message.success('禁用成功!');
                    }
                }
                else{
                    message.error(data.msg);
                }
                this.setState({});
            });
    }

    handleReOrder=(ruleOrder=this.state.ruleOrder,showMessage=false)=>{
        let formData = new FormData(); 
        formData.append("activationId",this.props.params.activationId);
        formData.append("ruleOrder",ruleOrder.join(','));
        fetch(fetchVersion+'/activation/updateOrder',{credentials: 'include',method: 'POST',
            body:formData})
            .then((res) => {
                if(res.ok){ 
                    return res.json();
                }
                else{
                    Modal.error({
                        title: '系统错误',
                        content: '请检查是否有参数配置错误',
                    });
                } 
            })
            .then((data)=>{
                if(showMessage){
                    message.success('排序成功！');
                }
            })
            .catch((e) => { 
                console.log(e.message);
            });
    }

    getOrderedRules=(ruleOrder,ruleData)=>{
        let resultList=[];
        for(let i=0;i<ruleOrder.length;i++){
            for(let j=0;j<ruleData.length;j++){
                if(ruleOrder[i]==ruleData[j].id){
                    resultList.push(ruleData.splice(j,1)[0]);
                    break;
                }
            }
        }
        return resultList;
    }

    handleDrag=(ruleId,pos)=>{
        let ruleOrder=this.state.ruleOrder;
        let ruleList=this.state.tData;

        let index=ruleOrder.indexOf(ruleId+'');
        if(index==pos){
            return;
        }
        let order=ruleOrder.splice(index,1);
        let rule=ruleList.splice(index,1);
        ruleOrder.splice(pos,0,order[0]);
        ruleList.splice(pos,0,rule[0]);
        this.state.ruleOrder=ruleOrder;
        this.state.tData=ruleList;

        this.setState({
        });

        // let moveY=0;
        // if(distance>=0){
        //     moveY=Math.floor(distance/48);
        // }
        // else{
        //     moveY=Math.ceil(distance/48);
        // }
        // if(moveY==0){return;}
        // if(moveY<0){
        //     let order=ruleOrder.splice(index,1);
        //     let rule=ruleList.splice(index,1);
        //     ruleOrder.splice(index+moveY,0,order[0]);
        //     ruleList.splice(index+moveY,0,rule[0]);
        //     this.setState({
        //         ruleOrder:ruleOrder,
        //         tData:ruleList
        //     });
        // }
        // else{
        //     let order=ruleOrder.splice(index,1);
        //     let rule=ruleList.splice(index,1);
        //     ruleOrder.splice(index+moveY,0,order[0]);
        //     ruleList.splice(index+moveY,0,rule[0]);
        //     this.setState({
        //         ruleOrder:ruleOrder,
        //         tData:ruleList
        //     });
        // }
    }

    componentDidMount() {
        this.fetchTableData();
        FetchUtil('/activation/datacolumns/'+this.props.params.id,'GET','',
        	(data) => {
                this.setState({
                    fieldList:data.data.list
                });
            });
        FetchUtil('/datalist/list/'+this.props.params.id,'GET','',
        	(data) => {
                this.setState({
                    dataList:data.data.list
                }); 
            });
    }

    render(){
        return (
                <div className="ant-layout-content">
                    <div id="header">
                        <Form inline>
                            <FormItem label="显示名称：">
                                <Input value={this.state.label} name="label" id="blue" onChange={this.handleChange}/>
                            </FormItem>
                            {' '}
                            <Button type="primary" onClick={this.handleAdd}>新增</Button>
                        </Form>
                    </div>

                    <Spin size="large" spinning={this.state.loading}>
                    <CollapseGroup handleDrag={this.handleDrag} handleReOrder={this.handleReOrder} draggable={!this.state.label}>
                    {this.state.tData.filter((info,index)=>{
                        if(this.state.label){
                            var reg = new RegExp('('+ this.state.label+')','gi');
                            return reg.test(info.label);
                        }else {
                            return  true; 
                        }
                    }).map((info,index)=>{
                        return (<Collapse ruleId={info.id} ruleOrder={this.state.ruleOrder} switcher={info.status=='1'} modelId={this.props.params.id} activationId={this.props.params.activationId} type="calendar" onSwitch={this.handleSwitch.bind(this,info)} key={info.id?info.id:index} title={info.label?info.label:'新增（请保存）'}>
                            <Rule rule={info} modelId={this.props.params.id} activationId={this.props.params.activationId} fieldList={this.state.fieldList} dataList={this.state.dataList} reload={this.fetchTableData} delete={this.handleDelete.bind(this,index)}/>
                        </Collapse>);
                    })
                    }
                        
                    </CollapseGroup>
                    </Spin>

                    {/*
                    <div>
                        <div style={{display:"none",width:"100%",marginTop:16,height:40}}>
                            <div style={{float:"right"}}>
                            <Pagination onChange={this.selectPage} defaultCurrent={this.state.pageNo} total={this.state.rowCount} />
                            </div>
                        </div>                   
                    </div>
                    */}
                </div>
        );
    }
}