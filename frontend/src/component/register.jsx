import React from 'react';
import {Form,Input,Button,Modal,message} from 'antd';

import './Login.less';
import {fetchVersion} from './utils/fetchUtil';

const FormItem = Form.Item;

class Register extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      open: false,
      rd:Math.random(),
    };
  }
  handleOk = () => {

      this.props.form.validateFields().then((values) => {
          const { loginName,passwd,verifyPasswd,captcha,giteeAccount } = values;
          fetch(fetchVersion+`/user/regist?loginName=${loginName}&passwd=${passwd}&verifyPasswd=${verifyPasswd}&captcha=${captcha}&giteeAccount=${giteeAccount}`,{method: 'POST',
	       })
	        .then((res) => {
	        	if(res.ok){
              return res.json();
            }
          }).then((res) => {
            if(res.code==='100'&&res.success){
              message.success(res.msg)
              this.setState({
                open: false
              });
            }else{
              message.error(res.msg)
            }
          })
	        .catch((e) => {
	            console.log(e.msg);
	        });
      })

  }
  onCancel = () => {
    this.setState({
      open: false
    });
  }


  registerHandler = () => {
    this.setState({
      open: true
    });
  }

  handleClick=(event)=>{
    this.setState({
        rd:Math.random()
    })
}

validator = (rule, value) => {
  const { getFieldValue } = this.props.form
  if (value && value !== getFieldValue('passwd')) {
      return Promise.reject('两次输入不一致！')
  }
  return Promise.resolve()
}

componentDidMount () {
  this.setState({
    rd:Math.random()
  })
}

  render() {
    const { open} = this.state;
    const formItemLayout = {
      labelCol: { span: 8 },
      wrapperCol: { span: 10 }
    };
    return (
      <div style={{display: 'inline-block'}}>
        <Button
          type="primary"
          style={{marginLeft: 10}}
          onClick={this.registerHandler}
        >
         注册
        </Button>
        <Modal
          title="注册账号"
          open={open}
          onOk={this.handleOk}
          onCancel={this.onCancel}
          maskClosable={false}
          destroyOnHidden={true}
          centered={true}
          width="600px"
          okText="确认"
          cancelText="取消"
        >
        <Form form={this.props.form}>
          <FormItem label={'账号'} name="loginName" rules={[{
              required: true,
              message: '请输入账号'
            }, {
              min: 5,
              max: 15,
              message: '长度不在范围内'
            }, {
              pattern: new RegExp('^\\w+$', 'g'),
              message: '用户名必须为字母或者数字'
            }]} {...formItemLayout}>
              <Input placeholder={'请输入账号'} />
          </FormItem>
          <FormItem label={'密码'} name="passwd" rules={[{ required: true, message: '请输入密码' }]} {...formItemLayout}>
              <Input type="password"  placeholder={'请输入密码'} />
          </FormItem>
          <FormItem label={'确认密码'} name="verifyPasswd" rules={[{ required: true, message: '请输入确认密码' },{validator:this.validator}]} {...formItemLayout}>
              <Input type="password" placeholder={'请输入确认密码'} />
          </FormItem>
          <FormItem label={'码云昵称'} name="giteeAccount" rules={[{ required: true, message: '请输入正确的码云昵称' }]} {...formItemLayout}>
              <Input  placeholder={'用于开通VIP权限'} />
          </FormItem>
          <FormItem label={'验证码'} name="captcha" rules={[{ required: true, message: '请输入验证码！' ,max:4}]} {...formItemLayout}>
              <Input  placeholder={'请输入验证码'} />
          </FormItem>
          <Form.Item colon={false} wrapperCol={{ span: 10, offset: 8 }} style={{marginTop:-8}}>
              <img  src={fetchVersion+"/common/getCaptcha?"+this.state.rd} onClick={this.handleClick}/>
          </Form.Item>
          <FormItem label={'Tips'} {...formItemLayout}>
              <span>如需开通VIP权限:请Star&Fork 本项目,然后在码云上留言"开通权限"</span>
          </FormItem>
        </Form>
        </Modal>
      </div>
    );
  }
}

// antd 5：Form.create 已移除，改用 Form.useForm 注入实例
export default (props) => {
  const [form] = Form.useForm();
  return <Register {...props} form={form} />;
}
