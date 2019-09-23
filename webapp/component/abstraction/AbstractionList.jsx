import React,{Component} from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message,Spin} from 'antd';
import { Link } from 'react-router';
const FormItem = Form.Item;
const Option = Select.Option;

import CollapseGroup from '../common/CollapseGroup';
import Collapse from '../common/Collapse';

import Abstraction from './Abstraction';
import {FetchUtil} from '../utils/fetchUtil';

export default class AbstractionList extends Component{
    constructor(props){
        super(props);

        this.state={
            name:'',
            label:'',
            status:"1",

            tData:[],
            pageNo:1,
            rowCount:0,

            loading:true,

            model:null,
            fieldList:[],
            dataList:[],
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
        param.modelId=this.props.params.id;
        param.aggregateType=this.state.aggregateType;
        param.name=this.state.name;
        param.label=this.state.label;
        param.status=this.state.status;

        FetchUtil('/abstraction','POST',JSON.stringify(param),
            (data) => {
                this.setState({loading:false});
                this.setState({
                    tData:data.data.page.list,
                    pageNo:data.data.page.pageNum,
                    rowCount:data.data.page.rowCount
                }); 
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
        state[name] = value;
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
            name:'',
            label:'',
            aggregateType:'',
            searchField:'',
            searchIntervalType:'',
            searchIntervalValue:'',
            functionField:'',
            comment:'',
            ruleDefinition:null,
            ruleScript:''
        });

        this.setState({
            tData:tData
        })
    }

    handleDelete=(index)=>{
        let tData=this.state.tData;
        let id=tData[index].id;
        if(id!=0){
            FetchUtil('/abstraction/','DELETE','['+id+']',
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

    componentDidMount() {
        this.fetchTableData();

        FetchUtil('/abstraction/datacolumns/'+this.props.params.id,'GET','',
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
                        <FormItem label="指标名：">
                            <Input value={this.state.name} name="name" id="blue" onChange={this.handleChange}/>
                        </FormItem>
                        {' '}
                        <Button type="primary" onClick={this.handleAdd}>新增</Button>
                    </Form>
                </div>
                <Spin size="large" spinning={this.state.loading}>
                <CollapseGroup>
                {this.state.tData.filter((info,index)=>{
                    if(this.state.name){
                        var reg = new RegExp('('+ this.state.name+')','gi');
                        return reg.test(info.label);
                    }else {
                        return  true;
                    }
                }).map((info,index)=>{
                    return (<Collapse key={info.id+""+index} title={info.label?info.label:'新增（请保存）'}>
                        <Abstraction abstraction={info} modelId={this.props.params.id} fieldList={this.state.fieldList} dataList={this.state.dataList} reload={this.fetchTableData} delete={this.handleDelete.bind(this,index)}/>
                    </Collapse>);
                })
                }
                    
                </CollapseGroup>
                </Spin>

                <div>
                    <div style={{display:"none",width:"100%",marginTop:16,height:40}}>
                        <div style={{float:"right"}}>
                        <Pagination onChange={this.selectPage} defaultCurrent={this.state.pageNo} total={this.state.rowCount} />
                        </div>
                    </div>                   
                </div>
            </div>
        );
    }
}