import React from 'react';
import {Breadcrumb,Menu,Icon,Form,Select} from 'antd';

const FormItem=Form.Item;
const Option = Select.Option;

import {FetchUtil} from '../utils/fetchUtil';

export default class Report extends React.Component{
	constructor(props){
		super(props);

		this.state={
			current:'',

            modelList:[],
            modelId:'',
            fieldList:[],
            eventFieldList:[],
            activationList:[]
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

        FetchUtil('/abstraction/datacolumns/'+value,'GET','',
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
        FetchUtil('/activation/rulecolumns/'+value,'GET','',
        (data) => {
            this.setState({
                activationList:data.data.list,
            });
        });
    }

    componentDidMount(){
        let key='';
        let modelId='';
        switch(this.props.location.pathname.split('/')[1]){
            case 'event':
                key='event';break;
            case 'graph':
                key='graph';break;
            case 'ruleid':
                key='event';
                modelId=this.props.location.pathname.split('/')[2];
                break;
            case 'dashboard':
                key='dashboard';break;
        }
        this.setState({
            current:key
        });

        FetchUtil('/model/list','GET',{},
            (data) => {
                this.setState({
                    modelList:data.data.modelList
                },()=>{
                    if(this.state.modelList.length>0&&modelId==''){
                        this.handleSelect('modelId',this.state.modelList[0].id+'');
                    }
                    else{
                        this.handleSelect('modelId',modelId);
                    }
                });
            });       
    }

    getItems=()=>{
        if(this.state.modelId==''){
            return '请选择模型！';
        }
        const props={
            modelId:this.state.modelId,
            model:this.state.modelList.filter(x=>x.id==this.state.modelId)[0],
            fieldList:this.state.fieldList,
            eventFieldList:this.state.eventFieldList,
            activationList:this.state.activationList
        }
        return React.cloneElement(this.props.children,props);
    }

	render() {
        return (
            <div className="ant-layout-wrapper">
                <div className="ant-layout-breadcrumb">
                    <Breadcrumb>
                        <Breadcrumb.Item>首页</Breadcrumb.Item>
                        <Breadcrumb.Item>报表查询</Breadcrumb.Item>
                    </Breadcrumb>
                </div>
                <div className="ant-layout-container">
                    <div style={{lineHeight:"46px",padding:"0 20px 0",margin:"0 24px",borderBottom:"1px solid #e9e9e9"}}>
                        <Form inline>
                            <FormItem label="模型：">
                                <Select dropdownMatchSelectWidth={false} value={this.state.modelId} onChange={this.handleSelect.bind(this,'modelId')} style={{width:100}}>
                                    {this.state.modelList.map((info)=>{
                                        return <Option key={info.id} value={info.id+''}>{info.label}</Option>
                                    })}
                                </Select>
                            </FormItem>
                        </Form>
                    </div>
                    <div className="ant-layout-header" style={{padding:"0 24px 24px"}}>
                        <Menu onClick={this.handleClick} selectedKeys={[this.state.current]} mode="horizontal">
                            <Menu.Item key="event">
                                <Icon type="file-text"/>调用查询
                            </Menu.Item>
                            <Menu.Item key="graph">
                                <Icon type="pushpin-o"/>规则命中
                            </Menu.Item>
                            <Menu.Item key="dashboard">
                                <Icon type="file-text"/>指示板
                            </Menu.Item>
                        </Menu>
                    </div>
                    {this.getItems()}
                </div>
            </div>
        );
    }
}