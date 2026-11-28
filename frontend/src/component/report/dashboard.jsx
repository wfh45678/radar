import React from 'react';
import {Form,Button,Table,Pagination,Input,Select,Modal,DatePicker,Cascader} from 'antd';
import dayjs from 'dayjs';

const FormItem=Form.Item;
const RangePicker = DatePicker.RangePicker;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

import './ListEvent.less';
import {withOutletCtx} from '../utils/routerUtil';

class DashBoard extends React.Component{

    constructor(props){
        super(props);

        this.state={
            scenarioId:'',
            loading:true,
            scenarioList:[],
            dashboardUrl:''
        }

    }

    componentDidMount(){
        if(!this.props.model.dashboardUrl){
            Modal.warning({
                title: '信息提醒',
                content: '该场景统计报表未初始化！',
            });
        }
    }

    // 原 componentWillReceiveProps 在 React 17+ 已移除，等价迁移为 componentDidUpdate
    componentDidUpdate(prevProps){
        if(prevProps.scenarioId!=this.props.scenarioId&&!this.props.model.dashboardUrl){
            Modal.warning({
                title: '信息提醒',
                content: '该场景统计报表未初始化！',
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

export default withOutletCtx(DashBoard);
