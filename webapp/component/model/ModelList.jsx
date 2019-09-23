import React from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message,Modal,Icon} from 'antd';
import { Link } from 'react-router';
const FormItem = Form.Item;
const Option = Select.Option;

import AddModel from './modal/AddModel';
import EditModel from './modal/EditModel';
import StaticField from './modal/StaticField';
import CopyModel from './modal/CopyModel';

import './Model.less';

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class ModelList extends React.Component{
    constructor(props){
        super(props);

        this.state={
            modelName:'',
            status:"1",

            tData:[],
            pageNo:1,
            rowCount:0,

            loading:true
        }

    }

    // 获取表格数据
    fetchTableData=()=>{
        const pageSize=1000;
        this.setState({loading:true});

        var param={};
        param.pageNo=this.state.pageNo;
        param.pageSize=pageSize;
        param.label=this.state.modelName;
        //param.status=this.state.status;   默认查询不传递该参数，否则只有单一数据，不完整

        FetchUtil('/model','POST',JSON.stringify(param),
            (data)=>{
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
        state[name] = trim(value);
        this.setState(state);
    }

    handleSearch=()=>{
        this.setState({
            pageNo:1
        },()=>{this.fetchTableData()});
    }

    copyModel=(r)=>{
        var param={};
        param.id=r.id;
        param.modelName=r.modelName;
        param.label=r.label;
        param.entityName=r.entityName;
        param.entryName=r.entryName;
        param.guid=r.guid;
        param.referenceDate=r.referenceDate;
        param.status=r.status;
        FetchUtil('/model/copy','POST',JSON.stringify(param),
            (data) => {
                if(data.success){
                    message.success('复制成功!');
                }
                else{
                    message.error(data.msg);
                }
                this.fetchTableData();
            })
    }

    componentDidMount() {
        this.fetchTableData();
    }

    deleteModel=(id)=>{
        FetchUtil('/model/','DELETE','['+id+']',
            (data) => {
                if(data.success){
                    message.success('删除成功!');
                }
                else{
                    message.error(data.msg);
                }
                this.fetchTableData();
            })
    }   

    render(){
        let showGuid=function(guid){
            Modal.info({
                title: '通过接口上传请携带此参数',
                content: 'guid:'+guid,
            });
        }
        /*定义表格列*/
        const columns = [
        {
            title: '序号',
            dataIndex: 'id',
            render:(t,r,i)=>{
                return i+1;
            }
        },
        {
            title: '模型名',
            dataIndex: 'label'
        },
        {
            title: '实体名',
            dataIndex: 'entityName'
        }, {
            title: '事件ID',
            dataIndex: 'entryName'
        }, {
            title: '唯一标识',
            dataIndex: 'guid',
            render:(t,r,i)=>{
                return <a onClick={showGuid.bind(this,r.guid)}>查看</a>;
            }
        },{
            title: '事件时间',
            dataIndex: 'referenceDate'
        },{
            title: '操作',
            dataIndex: 'handle',
            render: 
                (t,r,i) => {
                    return(
                    <span>
                        {r.entityName?
                        <Tooltip title="进入模型"><Link to={"/field/"+r.id}>进入模型</Link></Tooltip>                        
                        :
                        <StaticField row={r} reload={this.fetchTableData}/>
                        }
                        <span className="ant-divider"></span>
                        {r.entityName?
                            <CopyModel tData={this.state.tData} row={r} reload={this.fetchTableData}/>                       
                        :
                        ''
                        }
                        {r.entityName?<span className="ant-divider"></span>:''}

                        <EditModel tData={this.state.tData} row={r} reload={this.fetchTableData}/><span className="ant-divider"></span>
                        <Popconfirm placement="bottomRight" title={'确认删除该模型吗？'} onConfirm={this.deleteModel.bind(this,r.id)}>
                            <Tooltip title="删除"><a style={{color:'#FD5B5B'}}>删除</a></Tooltip>
                        </Popconfirm>
                    </span>
                );
                }
        }];

        return (
            <div className="ant-layout-wrapper"> 
                <div className="ant-layout-breadcrumb">
                    <Breadcrumb>
                    <Breadcrumb.Item>首页</Breadcrumb.Item>
                    <Breadcrumb.Item>模型列表</Breadcrumb.Item>
                    </Breadcrumb>
                </div>
                <div className="ant-layout-container">
                    <div className="ant-layout-content">
                        <div id="header">
                            <Form inline>
                                        <FormItem label="模型名：">
                                            <Input value={this.state.modelName} name="modelName" id="blue" onChange={this.handleChange}/>
                                        </FormItem>
                                        {' '}
                                        <AddModel tData={this.state.tData} reload={this.fetchTableData} />
                            </Form>
                        </div>

                        <div id="table">
                            <Table
                                dataSource={this.state.tData.filter((item,index,array)=>{
                                    let reg = new RegExp('('+ this.state.modelName+')','gi');
                                    if(this.state.modelName){
                                        return (reg.test(item.label));
                                    }else {
                                        return true;
                                    }
                                })}
                                columns={columns} 
                                size="middle" 
                                pagination={false}
                                loading={this.state.loading}
                            />
                            <div style={{display:"none",width:"100%",marginTop:16,height:40}}>
                                <div style={{float:"right"}}>
                                <Pagination onChange={this.selectPage} defaultCurrent={this.state.pageNo} total={this.state.rowCount} />
                                </div>
                            </div>                   
                        </div>
                    </div>
                </div>
          </div>
        );
    }
}