import React from 'react';
import {Breadcrumb, Menu, Form, Select} from 'antd';
import {FileTextOutlined, PushpinOutlined} from '@ant-design/icons';

const FormItem=Form.Item;

import {FetchUtil} from '../utils/fetchUtil';
import {Outlet} from 'react-router';
import {withLocation} from '../utils/routerUtil';

class Report extends React.Component{
	constructor(props){
		super(props);

		this.state={
			current:'',

            scenarioList:[],
            scenarioId:'',
            fieldList:[],
            eventFieldList:[],
            strategyList:[]
		}
	}

	handleClick=(e)=>{
		window.location.href='/#/'+e.key;
		this.setState({
			current:e.key
		})
	}

    handleSelect=(name,value)=>{
        let state=this.state;
        state[name]=value;
        this.setState(state);

        FetchUtil('/feature/columns/'+value,'GET','',
        (data) => {
            this.setState({
                fieldList:data.data.list
            });
        });
        FetchUtil('/event/datacolumns/'+value,'GET','',
        (data) => {
            this.setState({
                eventFieldList:data.data.list
            });
        });
        FetchUtil('/strategy/rule/columns/'+value,'GET','',
        (data) => {
            this.setState({
                strategyList:data.data.list,
            });
        });
    }

    componentDidMount(){
        let key='';
        let scenarioId='';
        switch(this.props.location.pathname.split('/')[1]){
            case 'event':
                key='event';break;
            case 'graph':
                key='graph';break;
            case 'ruleid':
                key='event';
                scenarioId=this.props.location.pathname.split('/')[2];
                break;
            case 'dashboard':
                key='dashboard';break;
        }
        this.setState({
            current:key
        });

        FetchUtil('/scenario/list','GET',{},
            (data) => {
                this.setState({
                    scenarioList:data.data.scenarioList
                },()=>{
                    if(this.state.scenarioList.length>0&&scenarioId==''){
                        this.handleSelect('scenarioId',this.state.scenarioList[0].id+'');
                    }
                    else{
                        this.handleSelect('scenarioId',scenarioId);
                    }
                });
            });       
    }

    getItems=()=>{
        if(this.state.scenarioId==''){
            return '请选择场景！';
        }
        // 原 rr3 cloneElement 注入子页 props，router 6/7 下走 Outlet context
        const ctx={
            scenarioId:this.state.scenarioId,
            model:this.state.scenarioList.filter(x=>x.id==this.state.scenarioId)[0],
            fieldList:this.state.fieldList,
            eventFieldList:this.state.eventFieldList,
            strategyList:this.state.strategyList
        }
        return <Outlet context={ctx}/>;
    }

	render() {
        return (
            <div className="ant-layout-wrapper">
                <div className="ant-layout-breadcrumb">
                    <Breadcrumb items={['首页', '报表查询']}/>
                </div>
                <div className="ant-layout-container">
                    <div style={{lineHeight:"46px",padding:"0 20px 0",margin:"0 24px",borderBottom:"1px solid #e9e9e9"}}>
                        <Form layout="inline">
                            <FormItem label="场景：">
                                <Select dropdownMatchSelectWidth={false} value={this.state.scenarioId} onChange={this.handleSelect.bind(this,'scenarioId')} style={{width:100}}
                                    options={this.state.scenarioList.map((info)=>({label:info.label, value:info.id+''}))}
                                />
                            </FormItem>
                        </Form>
                    </div>
                    <div className="ant-layout-header" style={{padding:"0 24px 24px"}}>
                        <Menu onClick={this.handleClick} selectedKeys={[this.state.current]} mode="horizontal"
                            items={[
                              { key:'event', label:<><FileTextOutlined />调用查询</> },
                              { key:'graph', label:<><PushpinOutlined />规则命中</> },
                              { key:'dashboard', label:<><FileTextOutlined />数据看板</> },
                            ]}
                        />
                    </div>
                    {this.getItems()}
                </div>
            </div>
        );
    }
}

export default withLocation(Report);
