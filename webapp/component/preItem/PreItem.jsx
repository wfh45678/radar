import React from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message} from 'antd';
const FormItem = Form.Item;
const Option = Select.Option;

import AddPreItem from './modal/AddPreItem';
import EditPreItem from './modal/EditPreItem';

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class PreItem extends React.Component{
    constructor(props){
        super(props);

        this.state={
            destField:'',
            label:'',
        	plugin:"",
    		status:1,

            tData:[],
            pageNo:1,
            rowCount:0,

            loading:true,

            model:null,
            filedList:[],
            plugins:[]
        }

        FetchUtil('/model/'+this.props.params.id,'GET','',
            (data) => {
                const model=data.data.model;
                this.setState({
                    model:model
                });
            });
        FetchUtil('/field/list/'+this.props.params.id,'GET','',
            (data) => {
                this.setState({
                    fieldList:data.data.field
                });
            });

        FetchUtil('/common/plugins','GET','',
            (data) => {
                this.setState({
                    plugins:data.data.plugins
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
        param.destField=this.state.destField;
        param.label=this.state.label;
        param.plugin=this.state.plugin;
        param.status=this.state.status;

        FetchUtil('/preitem','POST',JSON.stringify(param),
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
        state[name] = trim(value);
        this.setState(state);
    }

    handleSearch=()=>{
        this.setState({
            pageNo:1
        },()=>{this.fetchTableData()});
    }

    componentDidMount() {
        this.fetchTableData();
    }

    deletePreItem=(id)=>{
        FetchUtil('/preitem/','DELETE','['+id+']',
            (data) => {
                message.info('删除成功!');
                this.fetchTableData();
            });
    }   

    render(){
        /*定义表格列*/
        const columns = [
        {
            title: '序号',
            dataIndex: 'id',
            render:(t,r,i)=>{
                return i+1;
            }
        },{
        	title: '字段名',
        	dataIndex: 'label'
        },{
            title: '来源字段名',
            dataIndex: 'sourceLabel'
        },{
            title: '插件',
            dataIndex: 'plugin',
            render:
                (t) =>{
                    let plugin=this.state.plugins.filter(x=>x.method==t);
                    return plugin.length!=0?plugin[0].desc:'';
                }  
        },{
            title: '插件参数（可选）',
            dataIndex: 'args'  
        },{
            title: '操作',
            dataIndex: 'handle',
            render: 
                (t,r,i) => {
                    return(
                    <span>
                    	<EditPreItem modelId={this.props.params.id} row={r} fieldList={this.state.fieldList} plugins={this.state.plugins} reload={this.fetchTableData}/>
                        <span className="ant-divider"></span>
                        <Popconfirm placement="bottomRight" title={'确认删除该字段吗？'} onConfirm={this.deletePreItem.bind(this,r.id)}>
                            <Tooltip title="删除"><a style={{color:'#FD5B5B'}}>删除</a></Tooltip>
                        </Popconfirm>
                    </span>
                );
                }
        }];

        return (
            <div className="ant-layout-content">
                <div id="header">
                    <Form inline>
                        <FormItem label="字段名：">
                            <Input value={this.state.destField} name="destField" id="blue" onChange={this.handleChange}/>
                        </FormItem>                                
                        {' '}
                        <AddPreItem modelId={this.props.params.id} fieldList={this.state.fieldList} plugins={this.state.plugins} reload={this.fetchTableData} />
                    </Form>
                </div>

                <div id="table">
                    <Table
                        dataSource={this.state.tData.filter((item,index,array)=>{
                            var reg = new RegExp('('+ this.state.destField+')','gi');
                            if(this.state.destField){
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
        );
    }
}