import React from 'react';
import ReactDOM from 'react-dom';
import {Card,Form,Input,Button,Icon,Alert,Col,message,Modal} from 'antd';
const FormItem = Form.Item;
import './Login.less';
import {fetchVersion} from './utils/fetchUtil';
class Register extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      rd:Math.random(),
    };
  }
  handleOk = () => {

      const { validateFields,  } = this.props.form
      validateFields((err, values) => {
        if (!err) {
          const { loginName,passwd,verifyPasswd,captcha } = values;
          fetch(fetchVersion+`/merchant/regist?loginName=${loginName}&passwd=${passwd}&verifyPasswd=${verifyPasswd}&captcha=${captcha}`,{method: 'POST',
	       })
	        .then((res) => {
	        	if(res.ok){ 
              return res.json();
            }
          }).then((res) => {
            if(res.code==='100'&&res.success){
              message.success(res.msg)
              this.setState({
                visible: false
              });
            }else{
              message.error(res.msg)
            }
          })
	        .catch((e) => { 
	            console.log(e.msg);
	        });
        }
      })

  }
  onCancel = () => {
    this.setState({
      visible: false
    });
  }

  
  registerHandler = () => {
    this.setState({
      visible: true
    });
  }

  handleClick=(event)=>{
    this.setState({
        rd:Math.random()
    })
}

validator = (rule, value, callback) => {
  const { getFieldValue } = this.props.form
  if (value && value !== getFieldValue('passwd')) {
      callback('两次输入不一致！')
  }
  callback()
}

componentDidMount () {
  this.setState({
    rd:Math.random()
  })
}

  render() {
    const { visible} = this.state;
    const { getFieldDecorator } = this.props.form;
    const formItemLayout = {
      labelCol: {
        sm: { span: 8 }
      },
      wrapperCol: {
        sm: { span: 10 }
      }
    };
    return (
      <div style={{display: 'inline-block'}}>
        <Button
          className="ant-btn-lg"
          type="primary"
          style={{marginLeft: 10}}
          onClick={this.registerHandler}
        >
         注册
        </Button>
        <Modal
          title="注册账号"
          visible={visible}
          onOk={this.handleOk}
          onCancel={this.onCancel}
          maskClosable={false}
          destroyOnClose={true}
          centered={true}
          width="600px"
          okText="确认"
          cancelText="取消"
        >
        <FormItem label={'账号'} {...formItemLayout}>
            {getFieldDecorator('loginName', {
              rules: [{ required: true, message: '请输入账号' }],
            })(<Input placeholder={'请输入账号'} />)}
          </FormItem>
          <FormItem label={'密码'} {...formItemLayout}>
            {getFieldDecorator('passwd', {
              rules: [{ required: true, message: '请输入密码' }],
            })(<Input type="password"  placeholder={'请输入密码'} />)}
          </FormItem>
          <FormItem label={'确认密码'} {...formItemLayout}>
            {getFieldDecorator('verifyPasswd', {
              rules: [{ required: true, message: '请输入确认密码' },{validator:this.validator}],
            })(<Input type="password" placeholder={'请输入确认密码'} />)}
          </FormItem>
          <FormItem label={'验证码'} {...formItemLayout}>
            {getFieldDecorator('captcha', {
              rules: [{ required: true, message: '请输入验证码或者真确的位数！' ,max:4}],
            })(<Input  placeholder={'请输入验证码'} />)}
            <img  src={fetchVersion+"/common/getCaptcha?"+this.state.rd} onClick={this.handleClick}/>
          </FormItem>
        </Modal>
      </div>
    );
  }
}

export default Form.create()(Register)