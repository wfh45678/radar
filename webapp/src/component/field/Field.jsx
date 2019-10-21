import React from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message,Menu,Icon} from 'antd';
const FormItem = Form.Item;
const Option = Select.Option;
const SubMenu = Menu.SubMenu;
const MenuItemGroup = Menu.ItemGroup;

import AddField from './modal/AddField';
import EditField from './modal/EditField';

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class Field extends React.Component{
    constructor(props){
        super(props);

        this.state={
        	fieldName:'',
        	label:'',
        	fieldType:'',
            indexedState:'',

            tData:[],
            pageNo:1,
            rowCount:0,

            loading:true,

            model:null
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
        param.fieldName=this.state.fieldName;
        param.label=this.state.label;
        param.fieldType=this.state.fieldType;

        FetchUtil('/field','POST',JSON.stringify(param),
            (data) => {
                this.setState({loading:false});
                let indexedAll = data.data.page.list;
                let num = 0;
                let sum = indexedAll.reduce((pre,cur,index,array)=>{
                    let preNum =pre.indexed ? 1 : 0;
                    num += preNum ;
                    return (num += array[index].indexed);
                });
                //console.log(sum);
                this.setState({
                    tData:data.data.page.list,
                    pageNo:data.data.page.pageNum,
                    rowCount:data.data.page.rowCount,
                    indexedAll:sum
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
        FetchUtil('/field/','DELETE','['+id+']',
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
            title: '字段名',
            dataIndex: 'fieldName'
        },
        {
            title: '显示名称',
            dataIndex: 'label'
        },
        {
            title: '字段类型',
            dataIndex: 'fieldType',
            render:(t)=>{
                switch(t){
                    case 'STRING': return '字符串';
                    case 'INTEGER': return '整型';
                    case 'LONG': return '长整型';
                    case 'DOUBLE': return '浮点型';
                    default: return '';
                }
            }  
        },
        {
            title: '是否索引',
            dataIndex: 'indexed',
            render:(t)=>{
                if(t){
                    return '是';
                }else {
                    return '否'
                }
            }
        },
        {
            title: '操作',
            dataIndex: 'handle',
            render: 
                (t,r,i) => {
                    return(
                    <span>
                    	<EditField modelId={this.props.params.id} indexedAll={this.state.indexedAll} row={r} reload={this.fetchTableData}/>
                        <span className="ant-divider"></span>
                        <Popconfirm placement="bottomRight" title={'确认删除该字段吗？'} onConfirm={this.deleteModel.bind(this,r.id)}>
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
                                    <Input value={this.state.fieldName} name="fieldName" id="blue" onChange={this.handleChange}/>
                                </FormItem>                                
                                {' '}
                                <AddField modelId={this.props.params.id} indexedAll={this.state.indexedAll} reload={this.fetchTableData} />
                            </Form>
                        </div>

                        <div id="table">
                            <Table
                                dataSource={this.state.tData.filter((item,index,array)=>{
                                    var reg = new RegExp('('+ this.state.fieldName+')','gi');
                                    if(this.state.fieldName){
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