import React from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message,Modal} from 'antd';
import { Link } from 'react-router';
const FormItem = Form.Item;
const Option = Select.Option;

import {FetchUtil} from '../utils/fetchUtil';

import AddDataListRecord from './modal/AddDataListRecord';
import EditDataListRecord from './modal/EditDataListRecord';

export default class DatalistRecord extends React.Component{
    constructor(props){
        super(props);

        this.state={
            tData:[],
            pageNo:1,
            rowCount:0,
            pageSize:30,

            metaList:[],

            loading:true,

        }

        FetchUtil('/datalistmeta/list/'+this.props.params.datalistId,'GET','',
            (data) => {
                if(data.data.list.length==0){
                    Modal.warning({
                        title: '警告',
                        content: '黑/白名单字段未定义，请前往上级菜单点击管理字段按钮进行管理。点击按钮返回',
                        maskClosable:false,
                        onOk:this.handleRedirect
                      });
                }
                this.setState({
                    metaList:data.data.list
                });
            });

    }

    // 获取表格数据
    fetchTableData=()=>{
        const pageSize=20;
        this.setState({loading:true});

        var param={};
        param.pageNo=this.state.pageNo;
        param.pageSize=pageSize;
        param.dataListId=this.props.params.datalistId;

        FetchUtil('/datalistrecord','POST',JSON.stringify(param),
            (data) => {
                this.setState({
                    loading:false,
                    tData:data.data.page.list,
                    pageNo:data.data.page.pageNum
                });
                if(data.data.page.rowCount > 9990){
                    this.setState({
                        rowCount:9990
                    });
                }else {
                    this.setState({
                        rowCount:data.data.page.rowCount
                    });
                }
            });
    }

    selectPage=(page)=>{
        this.setState({
            pageNo:page
        },()=>{this.fetchTableData()});
    }

    componentDidMount() {
        this.fetchTableData();
    }

    deleteModel=(id)=>{
        FetchUtil('/datalistrecord/','DELETE','['+id+']',
            (data) => {
                message.info('删除成功!');
                this.fetchTableData();
            });
    }

    handleRedirect=()=>{
        window.history.back();
    }

    render(){
        /*定义表格列*/
        const columns = [
        {
            title: 'No.',
            dataIndex: 'id',
            render:(t,r,i)=>{
                return i+1;
            }
        },
        {
            title: 'Data Record',
            dataIndex: 'dataRecord'  
        },{
            title: '操作',
            dataIndex: 'handle',
            render: 
                (t,r,i) => {
                    return(
                    <span>
                        <EditDataListRecord metaList={this.state.metaList} dataListId={this.props.params.datalistId} row={r} reload={this.fetchTableData}/>
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
                        <AddDataListRecord metaList={this.state.metaList} dataListId={this.props.params.datalistId} reload={this.fetchTableData} />
                    </Form>                            
                </div>

                <div id="table">
                    <Table 
                        dataSource={this.state.tData} 
                        columns={columns} 
                        size="middle" 
                        pagination={false}
                        loading={this.state.loading}
                    />
                    <div style={{width:"100%",marginTop:16,height:40}}>
                        <div style={{float:"right"}}>
                        <Pagination onChange={this.selectPage} defaultCurrent={this.state.pageNo} defaultPageSize={this.state.pageSize} total={this.state.rowCount} />
                        </div>
                    </div>                   
                </div>
            </div>
        );
    }
}