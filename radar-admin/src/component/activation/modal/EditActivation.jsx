import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class EditActivation extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,

			activationName:'',
			label:'',
			comment:'',
			bottom:'0',
			median:'',
			high:'',
		}

	}

	// 获取表格数据
    fetchData=()=>{
	    FetchUtil('/activation/'+this.props.row.id,'GET','',
	    	(data) => {
	            const activation=data.data.activation;
	        	this.setState({
	        		activationName:activation.activationName,
	        		label:activation.label,
					comment:activation.comment,
					bottom:activation.bottom,
					median:activation.median,
					high:activation.high,
	        	});
	        });
    }

    handleChange=(e)=>{
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

    handleSelect=(name,value)=>{
    	var state = this.state;	
        state[name] = value;
        this.setState(state);
    }

	showModal=()=>{
		this.fetchData();
		this.setState({
			visible:true
		})
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
			param.modelId=this.props.modelId;
			param.activationName=this.state.activationName;
			param.label=this.state.label;
			param.comment=this.state.comment;
			param.bottom='0';
			param.median=this.state.median;
			param.high=this.state.high;
			param.status=1;

		    FetchUtil('/activation/','PUT',JSON.stringify(param),
		    	(data) => {
		    		if(data.success){
		    			message.success('修改成功');
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
        	activationName:{
        		help:'',
        		status:'success'
        	},
			label:{
        		help:'',
        		status:'success'
        	},
        	median:{
        		help:'',
        		status:'success'
        	},
        	high:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;

		if(!this.state.activationName){
			validate.activationName.help='请输入策略名';
			validate.activationName.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}$/;
			let activationName = this.state.activationName;
			if(!reg.test(activationName)){
				validate.activationName.help='按照提示输入正确的策略名';
				validate.activationName.status='error';
				isValidated=false;
			}
		}
		if(!this.state.label){
			validate.label.help='请输入显示名称';
			validate.label.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let label = this.state.label;
			if(!reg.test(label)){
				validate.label.help='按照提示输入正确的显示名称';
				validate.label.status='error';
				isValidated=false;

			}
		}
        if(!this.state.median){
        	validate.median.help='请输入警戒值';
        	validate.median.status='warning';
        	isValidated=false;
        }else if(!/^\d{1,3}/.test(this.state.median)){
        	validate.median.help='警戒值必须为数字';
        	validate.median.status='error';
        	isValidated=false;
        }
        if(!this.state.high){
        	validate.high.help='请输入拒绝值';
        	validate.high.status='warning';
        	isValidated=false;
        }else if(!/^\d{1,3}/.test(this.state.high)){
        	validate.high.help='拒绝值必须为数字';
        	validate.high.status='error';
        	isValidated=false;
        }

		return (
			<span>
				<Tooltip title="编辑" onClick={this.showModal}><a>编辑</a></Tooltip>
				<Modal title="编辑字段" visible={this.state.visible} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form horizontal form={this.props.form}>
						<FormItem required={true} {...formItemLayout} label="策略名：" help={validate.label.help} validateStatus={validate.label.status}>
							<Row>
								<Col span={20}>
									<Input type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
								</Col>
								<Col span={2} offset={1}>
									<Tooltip placement="right" title={'显示名称，一般为中文，如"异常注册"'}>
										<Icon style={{fontSize:16}} type="question-circle-o" />
									</Tooltip>
								</Col>
							</Row>
                        </FormItem>
                        <FormItem {...formItemLayout} label="备注：">
                            <Input type="text" name="comment" value={this.state.comment} onChange={this.handleChange}/>
                        </FormItem> 
                        <FormItem required={true} {...formItemLayout} label="警戒值：" help={validate.median.help} validateStatus={validate.median.status}>
							<Row>
								<Col span={6}>
									<InputNumber min={1} max={100} name="median" value={this.state.median} onChange={this.handleSelect.bind(this,'median')}/>
								</Col>
								<Col span={2} offset={1}>
									<Tooltip placement="right" title={'如果实际风险分数大于此数字，此交易则需要人工审核'}>
										<Icon style={{fontSize:16}} type="question-circle-o" />
									</Tooltip>
								</Col>
							</Row>
                        </FormItem> 
                        <FormItem required={true} {...formItemLayout} label="拒绝值：" help={validate.high.help} validateStatus={validate.high.status}>
							<Row>
								<Col span={6}>
									<InputNumber min={1} max={100} name="high" value={this.state.high} onChange={this.handleSelect.bind(this,'high')}/>
								</Col>
								<Col span={2} offset={1}>
									<Tooltip placement="right" title={'如果实际风险分数大于此数字，此交易则直接拒绝'}>
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
