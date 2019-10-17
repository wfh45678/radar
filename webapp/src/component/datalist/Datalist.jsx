import React from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message} from 'antd';
import { Link } from 'react-router';
const FormItem = Form.Item;
const Option = Select.Option;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

import AddDataList from './modal/AddDataList';
import EditDataList from './modal/EditDataList';
import EditDataListMeta from './modal/EditDataListMeta';

export default class Datalist extends React.Component{
    constructor(props){
        super(props);

        this.state={
		    name:'',
		    label:'',
		    listType:'',
		    status:1,

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
        param.modelId=this.props.params.id;
        param.name=this.state.name;
        param.label=this.state.label;
        param.listType=this.state.listType;
        param.status=this.state.status;

        FetchUtil('/datalist','POST',JSON.stringify(param),
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

    deleteModel=(id)=>{
        FetchUtil('/datalist/','DELETE','['+id+']',
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
        },
        {
            title: '列表名',
            dataIndex: 'label'
        }, {
            title: '备注',
            dataIndex: 'comment'
        },{
            title: '名单类型',
            dataIndex: 'listType'
        },{
            title: '操作',
            dataIndex: 'handle',
            render: 
                (t,r,i) => {
                    return(
                    <span>
                        <EditDataList modelId={this.props.params.id} row={r} reload={this.fetchTableData}/>
                        <span className="ant-divider"></span>
                        <EditDataListMeta modelId={this.props.params.id} row={r}/>
                        <span className="ant-divider"></span>
                        <Tooltip title="管理黑/白名单内容"><Link to={"/datalistRecord/"+this.props.params.id+"/"+r.id}>管理内容</Link></Tooltip>
                        <span className="ant-divider"></span>
                        <Popconfirm placement="bottomRight" title={'确认删除该模型吗？'} onConfirm={this.deleteModel.bind(this,r.id)}>
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
                                <FormItem label="列表名：">
                                    <Input value={this.state.name} name="name" id="blue" onChange={this.handleChange}/>
                                </FormItem>
                                {' '}
                                <AddDataList modelId={this.props.params.id} reload={this.fetchTableData} />
                    </Form>
                </div>

                <div id="table">
                    <Table
                        dataSource={this.state.tData.filter((item,index,array)=>{
                            var reg = new RegExp('('+ this.state.name+')','gi');
                            if(this.state.name){
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