import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class CopyModel extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,

			modelName:'',
			label:'',
		}
	}

    handleChange=(e)=>{
		var tData = this.props.tData.some((item)=>{
			if(item.label===e.target.value){
				return true;
			};
		});
		if(tData){
			Modal.error({
				title: '信息提示',
				content: '模型名重复',
			});
		}
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

    handleSelect=(name,value)=>{
    	var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

	showModal=()=>{
		this.setState({
			visible:true,

			modelName:'',
			label:'',
		});
	}

	handleSubmit=(validated)=>{
		if(!validated){
			Modal.error({
			    title: '提交失败',
			    content: '请确认表单内容输入正确',
			  });
		}
		else{
			var param={};
			param.id=this.props.row.id;
	        param.modelName='';
	        param.label=this.state.label;

			FetchUtil('/model/copy','POST',JSON.stringify(param),
		    	(data) => {
		    		if(data.success){
		    			message.success('复制成功');
		    		}else{
		    			message.error(data.msg);
		    		}
		            this.setState({
		            	visible:false
		            });
		            this.props.reload();
		        });
		}
	}

	handleCancel=()=>{
		this.setState({
			visible:false
		})
	}

	render(){
		const formItemLayout = {
            labelCol: { span: 6 },
            wrapperCol: { span: 16 },
        };

        let validate={
			label:{
				help:'',
				status:'success'
			}
        };
        let isValidated=true;

		if(!this.state.label){
			validate.label.help='请输入模型名';
			validate.label.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let label = this.state.label;
			if(!reg.test(label)){
				validate.label.help='按照提示输入正确的模型名';
				validate.label.status='error';
				isValidated=false;
			}
		}

		return (
			<span>
				<Tooltip title="复制模型" onClick={this.showModal}><a>复制</a></Tooltip>
				<Modal title="复制模型" visible={this.state.visible} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form horizontal form={this.props.form}>
                    	<FormItem {...formItemLayout} label="复制模型：">
                        	<label>{this.props.row.label}</label>
                        </FormItem>
						<FormItem required={true} {...formItemLayout} label="新模型名：" help={validate.label.help} validateStatus={validate.label.status}>
							<Row>
								<Col span={20}>
                            		<Input size="large" type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型显示名称，一般为中文，如"注册模型"'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>                     
                    </Form>
                </Modal>
            </span>    
		);
	}

}
