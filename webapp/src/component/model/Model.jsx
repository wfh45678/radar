import React from 'react';
import {Breadcrumb,Form,Row,Col,Switch,Modal,Input,Button,Table,Tooltip,Pagination,Select,Popconfirm,message,Menu,Icon} from 'antd';
import { Link } from 'react-router';
import moment from 'moment';
const FormItem = Form.Item;
const Option = Select.Option;
const SubMenu = Menu.SubMenu;
const MenuItemGroup = Menu.ItemGroup;
const confirm = Modal.confirm;

import {FetchUtil} from '../utils/fetchUtil';

export default class Model extends React.Component{
    constructor(props){
        super(props);

        this.state={
        	model:null,

        	current:'',
            disable: false,
            visible: false


        }

    }

    // 获取表格数据
    fetchData=()=>{

        FetchUtil('/model/'+this.props.params.id,'GET','',
            (data)=>{
                if(data.data.model.status === 1){
                    this.setState({
                        checked:true,
                        disable:true
                    });
                }else if(data.data.model.status === 2){
                    this.setState({
                        checked:false,
                        disable:true
                    });
                }else if(data.data.model.status === 0){
                    this.setState({
                        checked:false,
                        disable:false
                    });
                }
                this.setState({loading:false});
                this.setState({
                	model:data.data.model
                });
            });
    };


    componentDidMount() {
        let key='';
        switch(this.props.location.pathname.split('/')[1]){
            case 'field':
                key='field';break;
            case 'preItem':
                key='preItem';break;
            case 'datalist':
            case 'datalistRecord':
                key='datalist';break;
            case 'abstractionList':
                key='abstractionList';break;
            case 'activation':
            case 'ruleList':
            case 'historyRecordList':
                key='activation';break;
            case 'modelConfig':
                key='modelConfig';break;
        }
        this.setState({
            current:key
        });
        this.fetchData();
    }

    handleClick=(e)=>{
    	window.location.href='/#/'+e.key+'/'+this.props.params.id;
    	this.setState({
    		current:e.key
    	})
    };

    handleBuild=()=>{
        this.setState({
            visible: true
        });
        var _self = this;
        let paramsId = this.props.params.id;
        //var success = false;
        confirm({
            title: '是否重新构建模型？',
            content: '确认重新构建模型将清空历史数据！',
            onOk() {
                FetchUtil('/model/build/'+paramsId,'POST','',
                    (data)=>{
                        console.log(data);
                        if(!data.success){
                            Modal.error({
                                title: '构建状态',
                                content: data.msg,
                            });
                            //alert(data.msg);
                        }else {
                            Modal.success({
                                title: '构建状态',
                                content: '构建成功！',
                            });
                            _self.setState({
                                checked:true,
                                disable:true
                            });

                            console.log(_self);
                        }
                    });
            },
            onCancel() {

            }
        });



    };

    onChange=()=>{
        this.setState({
            checked: !this.state.checked,
        });

        if(!this.state.checked){
            FetchUtil('/model/enable/'+this.props.params.id,'POST','',
                (data)=>{
                    console.log(data);
                    if(!data.success){
                        alert(data.msg);
                    }
                });
        }else {
            FetchUtil('/model/disable/'+this.props.params.id,'POST','',
                (data)=>{
                    console.log(data);
                    if(!data.success){
                        alert(data.msg);
                    }
                });
        }
    };

    render(){
        return (
            <div className="ant-layout-wrapper"> 
                <div className="ant-layout-breadcrumb">
                    <Breadcrumb>
                    <Breadcrumb.Item>首页</Breadcrumb.Item>
                    <Breadcrumb.Item>{this.state.model==null?'':<Link to={"/model/"+this.state.model.id}>模型"{this.state.model.label}"</Link>}</Breadcrumb.Item>
                    </Breadcrumb>
                </div>
                <div className="ant-layout-container">
                    <div style={{lineHeight:"46px",padding:"0 20px 0",margin:"0 24px",borderBottom:"1px solid #e9e9e9"}}>
                        {this.state.model==null?'':<span style={{marginRight:20}}>模型："{this.state.model.label}"</span>}
                        {this.state.model==null?'':<span style={{marginRight:20}}>模型创建时间：{moment(this.state.model.createTime).format('YYYY-MM-DD HH:mm:ss')}</span>}
                        {this.state.disable===false?'':<Switch checkedChildren={'开'} unCheckedChildren={'关'} defaultChecked={this.state.checked} checked={this.state.checked} disabled={this.state.disabled} onChange={this.onChange} />}
                        <Button type="primary" onClick={this.handleBuild}  style={{float:"right",marginTop:9}}>构建模型</Button>
                    </div>
                    <div className="ant-layout-header" style={{padding:"0 24px 24px"}}>

                    	<Menu onClick={this.handleClick}
					        selectedKeys={[this.state.current]}
					        mode="horizontal"
					      >
					        <Menu.Item key="field">
					          <Icon type="file-text" />字段管理
					        </Menu.Item>
					        <Menu.Item key="preItem">
					          <Icon type="copy" />预处理管理
					        </Menu.Item>
					        <Menu.Item key="datalist">
					          <Icon type="appstore" />黑/白名单管理
					        </Menu.Item>
					        <Menu.Item key="abstractionList">
					          <Icon type="picture" />抽象处理
					        </Menu.Item>
					        
						        <Menu.Item key="modelConfig">
						           <Icon type="setting" />机器学习配置
						        </Menu.Item>
					        
					        <Menu.Item key="activation">
					          <Icon type="solution" />策略管理
					        </Menu.Item>
					      </Menu>
                    </div>
                    {this.props.children}
                </div>
          </div>
        );
    }
}