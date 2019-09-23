import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class EditField extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,

			fieldName:'',
			label:'',
			fieldType:'',

			fieldTypes:[],
			indexed:false
		}

	    FetchUtil('/common/fieldtypes','GET','',
	    	(data) => {
	            this.setState({
	            	fieldTypes:data.data.fields
	            })
	        });
	}

	// 获取表格数据
    fetchData=()=>{
	    FetchUtil('/field/'+this.props.row.id,'GET','',
	    	(data) => {
	            const field=data.data.field;
	        	this.setState({
	        		fieldName:field.fieldName,
	        		label:field.label,
	        		fieldType:field.fieldType,
					indexed:field.indexed
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
        state[name] = trim(value);
        this.setState(state);
    }

	showModal=()=>{
		this.fetchData();
		this.setState({
			visible:true
		})
	}

	onCheck=(e)=>{
		if(e.target.checked && this.props.indexedAll>=8){
			Modal.warning({
				title: '提示信息',
				content: '索引已超过8项！',
			});
		}else {
			this.setState({
				indexed:e.target.checked
			});
		}		
	};

	handleSubmit=(validated)=>{
		if(!validated){
			Modal.error({
			    title: '提交失败',
			    content: '请确认表单内容输入正确',
			  });
		}
		else{
			var param = {};
			param.id = this.props.row.id;
			param.modelId = this.props.modelId;
			param.fieldName = this.state.fieldName;
			param.label = this.state.label;
			param.fieldType = this.state.fieldType;
			param.indexed = this.state.indexed;

			FetchUtil('/field/', 'PUT', JSON.stringify(param),
				(data) => {
					if (data.success) {
						message.success('修改成功');
					} else {
						message.error(data.msg);
					}
					this.setState({
						visible: false
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
        	fieldName:{
        		help:'',
        		status:'success'
        	},
			label:{
        		help:'',
        		status:'success'
        	},
        	fieldType:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;

		if(!this.state.fieldName){
			validate.fieldName.help='请输入字段名';
			validate.fieldName.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}$/;
			let fieldName = this.state.fieldName;
			if(!reg.test(fieldName)){
				validate.fieldName.help='按照提示输入正确的字段名';
				validate.fieldName.status='error';
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
        if(!this.state.fieldType){
        	validate.fieldType.help='请选择字段类型';
        	validate.fieldType.status='warning';
        	isValidated=false;
        }

		return (
			<span>
				<Tooltip title="编辑" onClick={this.showModal}><a>编辑</a></Tooltip>
				<Modal title="编辑字段" visible={this.state.visible} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form horizontal form={this.props.form}>
                        <FormItem required={true} {...formItemLayout} label="字段名：" help={validate.fieldName.help} validateStatus={validate.fieldName.status}>
							<Row>
                        		<Col span={20}>
									<Input type="text" name="fieldName" value={this.state.fieldName} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'2-30位英文字母、数字、下划线的组合，以英文字母开头，如"deviceId"'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
						<FormItem required={true} {...formItemLayout} label="显示名称：" help={validate.label.help} validateStatus={validate.label.status}>
							<Row>
								<Col span={20}>
									<Input type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'字段显示名称，一般为中文，如"设备ID"'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="字段类型：" help={validate.fieldType.help} validateStatus={validate.fieldType.status}>
							<Row>
								<Col span={10}>
									<Select value={this.state.fieldType} onChange={this.handleSelect.bind(this,'fieldType')}>
										<Option value="">请选择</Option>
											{
												this.state.fieldTypes.map(x=><Option key={x.name} value={x.name}>{x.desc}</Option>)
											}
									</Select>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'字段类型，目前有四种类型，分别为字符串（如"你好"，"abc"等），整数（其范围为 -2147483648 到 2147483647 之间），长整数（其范围为 -9223372036854775808 到 9223372036854775807 之间），浮点数（如 3.14）'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
						<FormItem {...formItemLayout} label="是否索引：">
                        	<Row>
								<Col span={1}>
                            		<Checkbox checked={this.state.indexed} onChange={this.onCheck}></Checkbox>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'如果勾选，则为该字段创建索引'}>
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
