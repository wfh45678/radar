import React from 'react';
import {Card,Form,Input,Button,Icon,Alert,Col,message} from 'antd';

const FormItem = Form.Item;

import './Login.less';
import {fetchVersion} from './utils/fetchUtil';
import {trim} from './utils/validateUtil';

export default class Login extends React.Component{

    state={
        username:'',
        password:'',
        captcha:'',
        rd:Math.random(),

        showMsg:false,
        msg:''
    }

    handleChange=(e)=>{
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

    handleLogin=()=>{
        if(!this.state.username){
            this.setState({
                showMsg:true,
                msg:'请输入用户名'
            });
            return false;
        }
        if(!this.state.password){
            this.setState({
                showMsg:true,
                msg:'请输入密码'
            });
            return false;
        }
        if(!this.state.captcha){
            this.setState({
                showMsg:true,
                msg:'请输入验证码'
            });
            return false;
        }

        let formData = new FormData(); 
        formData.append("loginName",this.state.username);
        formData.append("passwd",this.state.password);
        formData.append("captcha",this.state.captcha);
        const hide = message.loading('正在执行中...', 0);
        fetch(fetchVersion+'/merchant/login',{credentials: 'include',method: 'POST',
	        body:formData})
	        .then((res) => {
	        	hide();
	        	if(res.ok){ 
		            return res.json();
	            }
	            else{
	            	Modal.error({
				    	title: '系统错误',
				    	content: '请检查是否有参数配置错误',
				  	});
	            } 
	        })
	        .then((data)=>{
                if(data.success){
                    window.location.href="#/modelList";
                }
                else{
                    this.refs.captcha.click();
                    this.setState({
                        msg:data.msg,
                        showMsg:true
                    }); 
                }
            })
	        .catch((e) => { 
	            console.log(e.message);
	        });
        
    }

    handleClick=(event)=>{
        this.setState({
            rd:Math.random()
        })
    }

    handleKeyDown=(event)=>{
        if(event.keyCode==13){
            this.handleLogin();
        }
    }

    render(){
        const formItemLayout = {
            labelCol: { span: 7 },
            wrapperCol: { span: 13 },
        };

        return (
            <div className="middleBox" onKeyDown={this.handleKeyDown}>
                <Card>
                    <h2 style={{textAlign:"center",paddingBottom:10,borderBottom:"1px dashed #ececec"}}><Icon type="lock" />&nbsp;&nbsp;风控引擎管理平台</h2>
                    <Form horizontal style={{marginTop:30}}>
                        <FormItem {...formItemLayout} label="用户名">
                            <Input size="large" type="text" name="username" value={this.state.username} onChange={this.handleChange}/>
                        </FormItem>
                        <FormItem {...formItemLayout} label="密码">
                            <Input type="password" name="password" value={this.state.password} onChange={this.handleChange}/>
                        </FormItem>

                        <FormItem {...formItemLayout} label="验证码" style={{marginBottom:12}}>
                            <Col span="8">
                            <Input style={{width:60}} size="large" type="text" name="captcha" value={this.state.captcha} onChange={this.handleChange}/>
                            </Col>
                            <Col span="12">
                            <span><img id="captcha" ref="captcha" src={fetchVersion+"/common/getCaptcha?"+this.state.rd} onClick={this.handleClick}/> </span>
                            </Col>                           
                        </FormItem>

                        <FormItem wrapperCol={{ span: 13, offset: 7 }}>
                            <div style={this.state.showMsg?{display:"block"}:{display:"none"}}><Alert message={this.state.msg} type="error" /></div>
                            <Button type="primary" onClick={this.handleLogin}>登录</Button> &nbsp;&nbsp;&nbsp;&nbsp;<Button type="primary">注册</Button>
                        </FormItem> 	                                      
                    </Form>
                </Card>
            </div>
        );
    }
}