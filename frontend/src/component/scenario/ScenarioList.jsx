import React from 'react';
import {Breadcrumb,Form,Input,Table,Tooltip,Pagination,Popconfirm,message,Modal} from 'antd';
import { Link } from 'react-router';
const FormItem = Form.Item;

import AddScenario from './modal/AddScenario';
import EditScenario from './modal/EditScenario';
import StaticField from './modal/StaticField';
import CopyScenario from './modal/CopyScenario';
import ImportScenario from './modal/ImportScenario';
import TemplateLibrary from './modal/TemplateLibrary';

import './Scenario.less';

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class ScenarioList extends React.Component{
    constructor(props){
        super(props);

        this.state={
            scenarioName:'',
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
        param.label=this.state.scenarioName;
        //param.status=this.state.status;   默认查询不传递该参数，否则只有单一数据，不完整

        FetchUtil('/scenario','POST',JSON.stringify(param),
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
        param.scenarioName=r.scenarioName;
        param.label=r.label;
        param.entityName=r.entityName;
        param.entryName=r.entryName;
        param.guid=r.guid;
        param.referenceDate=r.referenceDate;
        param.status=r.status;
        FetchUtil('/scenario/copy','POST',JSON.stringify(param),
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
        FetchUtil('/scenario/','DELETE','['+id+']',
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

    // 导出策略包（v3.2）：blob 下载（带鉴权头，Content-Disposition 同源可读）
    exportModel=(r)=>{
        fetch('/services/v1/scenario/export/' + r.id, {
            method: 'GET',
            credentials: 'include',
            headers: {'x-auth-token': localStorage.getItem('x-auth-token')}
        }).then((res) => {
            if(!res.ok){
                message.error('导出失败：场景不存在或服务异常');
                return null;
            }
            const disposition = res.headers.get('content-disposition') || '';
            let filename = 'radar-model-' + r.id + '.json';
            const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/);
            if(utf8Match){
                filename = decodeURIComponent(utf8Match[1]);
            }
            return res.blob().then((blob)=>({blob, filename}));
        }).then((payload) => {
            if(!payload){ return; }
            const url = URL.createObjectURL(payload.blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = payload.filename;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
            message.success('导出成功');
        }).catch((e) => {
            message.error('导出失败：' + e.message);
        });
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
            title: '场景名',
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
                        <Tooltip title="进入场景"><Link to={"/field/"+r.id}>进入场景</Link></Tooltip>                        
                        :
                        <StaticField row={r} reload={this.fetchTableData}/>
                        }
                        <span className="ant-divider"></span>
                        {r.entityName?
                            <CopyScenario tData={this.state.tData} row={r} reload={this.fetchTableData}/>
                        :
                        ''
                        }
                        {r.entityName?<span className="ant-divider"></span>:''}
                        <Tooltip title="导出策略包 json（v3.2）"><a onClick={this.exportModel.bind(this,r)}>导出</a></Tooltip>
                        <span className="ant-divider"></span>

                        <EditScenario tData={this.state.tData} row={r} reload={this.fetchTableData}/><span className="ant-divider"></span>
                        <Popconfirm placement="bottomRight" title={'确认删除该场景吗？'} onConfirm={this.deleteModel.bind(this,r.id)}>
                            <Tooltip title="删除"><a style={{color:'#FD5B5B'}}>删除</a></Tooltip>
                        </Popconfirm>
                    </span>
                );
                }
        }];

        return (
            <div className="ant-layout-wrapper"> 
                <div className="ant-layout-breadcrumb">
                    <Breadcrumb items={[{title:'首页'},{title:'场景列表'}]}/>
                </div>
                <div className="ant-layout-container">
                    <div className="ant-layout-content">
                        <div id="header">
                            <Form layout="inline">
                                        <FormItem label="场景名：">
                                            <Input value={this.state.scenarioName} name="scenarioName" id="blue" onChange={this.handleChange}/>
                                        </FormItem>
                                        {' '}
                                        <AddScenario tData={this.state.tData} reload={this.fetchTableData} />
                                        {' '}
                                        <ImportScenario reload={this.fetchTableData}/>
                                        {' '}
                                        <TemplateLibrary reload={this.fetchTableData}/>
                            </Form>
                        </div>

                        <div id="table">
                            <Table
                                dataSource={this.state.tData.filter((item,index,array)=>{
                                    let reg = new RegExp('('+ this.state.scenarioName+')','gi');
                                    if(this.state.scenarioName){
                                        return (reg.test(item.label));
                                    }else {
                                        return true;
                                    }
                                })}
                                columns={columns} 
                                size="middle" 
                                pagination={false}
                                loading={this.state.loading}
                                rowKey={record => record.id}
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