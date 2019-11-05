import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class AddPreItem extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,

			destField:'',
			label:'',
			sourceField:'',
			sourceLabel:'',
			plugin:'',
			status:1,
			args:''
		}
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
    	if(name=='plugin'){
    		state['sourceField']='';
    		state['sourceLabel']='';
    		state['args']='';
    	}   	
        state[name] = trim(value);

        if(name=='sourceField'){
        	state['sourceLabel']=this.props.fieldList.filter(x=>x.fieldName==value)[0].label;
        }

        this.setState(state);
    }

    handleMultiSelect=(name,value)=>{
    	var state = this.state;
    	if(value==''){
    		state[name]='';
    	}
    	else{
	        state[name] = trim(value.join(','));
	        state['sourceLabel'] = value.map((info)=>{
	        	return this.props.fieldList.filter(x=>x.fieldName==info)[0].label;
	        }).join(',');
	    }

        this.setState(state);
    }

	showModal=()=>{
		this.setState({
			destField:'',
			label:'',
			sourceField:'',
			sourceLabel:'',
			plugin:'',
			status:1,
			args:'',
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
			param.modelId=this.props.modelId;
			param.destField=this.state.destField;
			param.label=this.state.label;
	        param.sourceField=this.state.sourceField;
	        param.sourceLabel=this.state.sourceLabel;
	        param.plugin=this.state.plugin;
	        param.status=this.state.status;
	        param.args=this.state.args;

			FetchUtil('/preitem/','PUT',JSON.stringify(param),
		    	(data) => {
		    		if(data.success){
		    			message.success('添加成功');
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
        	plugin:{
        		help:'',
        		status:'success'
        	},
        	label:{
        		help:'',
        		status:'success'
        	},
			sourceField:{
        		help:'',
        		status:'success'
        	},
        	args:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;

        if(!this.state.plugin){
        	validate.plugin.help='请选择插件';
        	validate.plugin.status='warning';
        	isValidated=false;
        }
		if(!this.state.label){
			validate.label.help='请输入目标字段名';
			validate.label.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let label = this.state.label;
			if(!reg.test(label)){
				validate.label.help='按照提示输入正确的目标显示名称';
				validate.label.status='error';
				isValidated=false;

			}
		}
        if(!this.state.sourceField){
        	validate.sourceField.help='请选择原始字段名';
        	validate.sourceField.status='warning';
        	isValidated=false;
        }
        if(!this.state.args){
        	validate.args.status='warning';
        	if(this.state.plugin=='SUBSTRING'){
        		validate.args.help='请输入截取字段位数';
	        	isValidated=false;
	        } else if (this.state.plugin=='DATEFORMAT') {
	        	validate.args.help='请输入日期格式化字符串';
	        	isValidated=false;
	        }
        }

        const plugin=this.state.plugin;
        let fieldArr=this.state.sourceField==''?[]:this.state.sourceField.split(',');
		return (
			<span>
				<Button onClick={this.showModal} type="primary">新增</Button>
				<Modal title="编辑字段" visible={this.state.visible} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form horizontal form={this.props.form}>
                    	<FormItem required={true} {...formItemLayout} label="插件种类：" help={validate.plugin.help} validateStatus={validate.plugin.status}>
							<Row>
								<Col span={20}>
									<Select value={this.state.plugin} onChange={this.handleSelect.bind(this,'plugin')}>
										<Option value="">请选择</Option>
											{
												this.props.plugins.map(x=><Option key={x.key} value={x.method}>{x.desc}</Option>)
											}
									</Select>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'插件种类，如 IP转换成地址（将IP地址转换成详细的实际地址），字段合并（将多个原始字段合并起来），字符串截短（例将手机号码截取部分进行筛选，如前七位0,7），等等'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
						<FormItem required={true} {...formItemLayout} label="目标字段名：" help={validate.label.help} validateStatus={validate.label.status}>
							<Row>
								<Col span={20}>
									<Input type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'字段显示名称，一般为中文，如"IP归属地"'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="原始字段名：" style={plugin!='ALLINONE'?{}:{display:"none"}} help={validate.sourceField.help} validateStatus={validate.sourceField.status}>
							<Row>
								<Col span={20}>
									<Select value={this.state.sourceField} onChange={this.handleSelect.bind(this,'sourceField')}>
										<Option value="">请选择</Option>
											{
												this.props.fieldList==null?'':this.props.fieldList.map(x=><Option key={x.id} value={x.fieldName}>{x.label}</Option>)
											}
									</Select>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'原始字段名，均为自己定义的字段名'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
	                    </FormItem>
                        <FormItem required={true} {...formItemLayout} label="原始字段名：" style={plugin=='ALLINONE'?{}:{display:"none"}} help={validate.sourceField.help} validateStatus={validate.sourceField.status}>
							<Row>
								<Col span={20}>
									<Select multiple placeholder="Please select" value={fieldArr} onChange={this.handleMultiSelect.bind(this,'sourceField')}>
										{
											this.props.fieldList==null?'':this.props.fieldList.map(x=><Option key={x.id} value={x.fieldName}>{x.label}</Option>)
										}
									</Select>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'原始字段名，均为自己定义的字段名'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
	                    </FormItem>
	                    <FormItem required={true} {...formItemLayout} label="截取位数" style={plugin=='SUBSTRING'?{}:{display:"none"}} help={validate.args.help} validateStatus={validate.args.status}>
							<Row>
								<Col span={20}>
									<Input type="text" name="args" value={this.state.args} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'截取位数，属于字符串截短插件的参数，如筛选手机号码前7位，填写 0,7'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
	                    </FormItem>    
	                    <FormItem required={true} {...formItemLayout} label="格式化字符串" style={plugin=='DATEFORMAT'?{}:{display:"none"}} help={validate.args.help} validateStatus={validate.args.status}>
							<Row>
								<Col span={20}>
									<Input type="text" name="args" value={this.state.args} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'日期格式化字符串，例如:yyyyMMdd HH:mm:ss'}>
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
