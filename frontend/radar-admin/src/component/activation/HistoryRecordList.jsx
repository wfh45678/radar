import React,{Component} from 'react';
import {Breadcrumb,Form,Row,Col,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message,Timeline,Icon} from 'antd';
import CollapseGroup from '../common/CollapseGroup';
import Collapse from '../common/Collapse';
import { Link } from 'react-router';
import moment from 'moment';
const FormItem = Form.Item;
const Option = Select.Option;

import HistoryRecord from './HistoryRecord';
import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class HistoryRecordList extends Component{
    constructor(props){
        super(props);

        this.state={
            label:'',

            tData:[],
            pageNo:1,
            rowCount:0,

            loading:true,

            model:null,
            fieldList:[],
            dataList:[]
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
        param.ruleId=this.props.params.ruleId;

        //此处为单条策略历史记录接口的获取，参数中应该有本条策略的id，否则无法区分
        FetchUtil('/rule/ruleHistory','POST',JSON.stringify(param),
            (data) => {
                this.setState({loading:false});
                this.setState({
                    label:data.data.page.list[0].label,
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
    componentWillMount() {
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
                <div id="header" style={{fontSize:'16px',marginBottom:'20px'}}>
                    <Link to={"/ruleList/"+this.props.params.id+"/"+this.props.params.activationId}><Icon type="left-circle" style={{marginRight:'10px'}} />{this.state.label}</Link><span style={{marginLeft:'20px'}}>历史记录</span>
                </div>

                <Timeline style={{marginLeft:'150px'}}>
                    {this.state.tData.map((info,index)=>{
                        return (
                            <Timeline.Item key={index}>
                                <span style={{position:'absolute',left:'-120px'}}>{moment(info.updateTime).format('YYYY-MM-DD HH:mm:ss')}</span>
                                <HistoryRecord ruleHistory={info} modelId={this.props.params.id} activationId={this.props.params.activationId} fieldList={this.state.fieldList} dataList={this.state.dataList} reload={this.fetchTableData}/>
                            </Timeline.Item>
                            );
                        })
                    }
                </Timeline>

            </div>
        );
    }
}