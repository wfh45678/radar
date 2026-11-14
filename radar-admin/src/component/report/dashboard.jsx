import React from 'react';
import {Form,Button,Table,Pagination,Input,Select,Modal,DatePicker,Cascader} from 'antd';
import moment from 'moment';

const FormItem=Form.Item;
const Option = Select.Option;
const RangePicker = DatePicker.RangePicker;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

import './ListEvent.less';

export default class DashBoard extends React.Component{

    constructor(props){
        super(props);

        this.state={
            modelId:'',
            loading:true,
            modelList:[],
            dashboardUrl:''
        }

    }

    componentDidMount(){
        if(!this.props.model.dashboardUrl){
            Modal.warning({
                title: '信息提醒',
                content: '该模型统计报表未初始化！',
            });
        }
    }

    componentWillReceiveProps(nextProps){
        if(this.props.modelId!=nextProps.modelId&&!nextProps.model.dashboardUrl){
            Modal.warning({
                title: '信息提醒',
                content: '该模型统计报表未初始化！',
            });
        }
    }

    render(){
        return (
            <div className="ant-layout-content">
                <iframe ref="dashBoard" name="dashBoard" src={this.props.model.dashboardUrl} style={{minHeight:'800px',width:'100%',border:'0px'}}></iframe>
            </div>);
    }
}