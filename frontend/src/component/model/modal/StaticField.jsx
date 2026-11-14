import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class StaticField extends React.Component{
	constructor(props){
		super(props);

		this.state={
			entityName:'',
			entityType:'',
			entityLabel:'',

			entryName:'',
			entryType:'',
			entryLabel:'',

			referenceDate:'',
			referenceDateType:'LONG',
			referenceDateLabel:'',

			visible:false,
			fieldTypes:[]
		}

		FetchUtil('/common/fieldtypes','GET','',
	    	(data) => {
	            this.setState({
	            	fieldTypes:data.data.fields
	            })
	        });
	}

	showModal=()=>{
		this.setState({
			visible:true,

			entityName:'',
			entityType:'',
			entityLabel:'',
			entryName:'',
			entryType:'',
			entryLabel:'',
			referenceDate:'',
			referenceDateType:'LONG',
			referenceDateLabel:'',
		})
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

    handleSubmit=(validated)=>{
		if(!validated){
			Modal.error({
			    title: '提交失败',
			    content: '请确认表单内容输入正确',
			  });
		}
		else{
            let model=this.props.row;
            model.entityName=this.state.entityName;
            model.entryName=this.state.entryName;
            model.referenceDate=this.state.referenceDate;
	        
			FetchUtil('/model/','PUT',JSON.stringify(model),
	    	(data) => {
                this.setState({
                    visible:false
                });
                this.props.reload();
            });

            var entity={};
            entity.modelId=this.props.row.id;
            entity.fieldName=this.state.entityName;
            entity.label=this.state.entityLabel;
            entity.fieldType=this.state.entityType;
            entity.indexed=true;
            FetchUtil('/field/','PUT',JSON.stringify(entity),() => {});

            var entry={};
            entry.modelId=this.props.row.id;
            entry.fieldName=this.state.entryName;
            entry.label=this.state.entryLabel;
            entry.fieldType=this.state.entryType;
            entry.indexed=true;
            FetchUtil('/field/','PUT',JSON.stringify(entry),() => {});

            var referenceDate={};
            referenceDate.modelId=this.props.row.id;
            referenceDate.fieldName=this.state.referenceDate;
            referenceDate.label=this.state.referenceDateLabel;
            referenceDate.fieldType=this.state.referenceDateType;
            referenceDate.indexed=true;
            FetchUtil('/field/','PUT',JSON.stringify(referenceDate),() => {});
		}
	}

	handleCancel=()=>{
		this.setState({
			visible:false
		})
	}

	render(){
		const formItemLayout = {
            labelCol: { span: 5 },
            wrapperCol: { span: 19 },
        };

        let validate={
        	entityName:{
        		help:'',
        		status:'success'
        	},
        	entityLabel:{
        		help:'',
        		status:'success'
        	},
        	entityType:{
        		help:'',
        		status:'success'
        	},
        	entryName:{
        		help:'',
        		status:'success'
        	},
        	entryLabel:{
        		help:'',
        		status:'success'
        	},
        	entryType:{
        		help:'',
        		status:'success'
        	},
        	referenceDate:{
        		help:'',
        		status:'success'
        	},
        	referenceDateLabel:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;

		if(!this.state.entityName){
			validate.entityName.help='请输入实体名';
			validate.entityName.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}$/;
			let entityName = this.state.entityName;
			if(!reg.test(entityName)){
				validate.entityName.help='按照提示输入正确的实体名';
				validate.entityName.status='error';
				isValidated=false;
			}
		}
		if(!this.state.entityLabel){
			validate.entityLabel.help='请输入实体显示名';
			validate.entityLabel.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let entityLabel = this.state.entityLabel;
			if(!reg.test(entityLabel)){
				validate.entityLabel.help='按照提示输入正确的实体显示名';
				validate.entityLabel.status='error';
				isValidated=false;

			}
		}
        if(!this.state.entityType){
            validate.entityType.help='请选择实体类型';
            validate.entityType.status='warning';
            isValidated=false;
        }
		if(!this.state.entryName){
			validate.entryName.help='请输入事件ID';
			validate.entryName.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}$/;
			let entryName = this.state.entryName;
			if(!reg.test(entryName)){
				validate.entryName.help='按照提示输入正确的事件ID';
				validate.entryName.status='error';
				isValidated=false;
			}
		}
        if(this.state.entityName&&(this.state.entityName==this.state.entryName)){
            validate.entryName.help='事件ID不能与实体名相同!';
            validate.entryName.status='error';
            isValidated=false;
        }
		if(!this.state.entryLabel){
			validate.entryLabel.help='请输入事件ID显示名';
			validate.entryLabel.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let entryLabel = this.state.entryLabel;
			if(!reg.test(entryLabel)){
				validate.entryLabel.help='按照提示输入正确的事件ID显示名';
				validate.entryLabel.status='error';
				isValidated=false;

			}
		}
        if(!this.state.entryType){
            validate.entryType.help='请选择事件ID类型';
            validate.entryType.status='warning';
            isValidated=false;
        }
        if(this.state.entityName&&(this.state.entityName==this.state.referenceDate)){
            validate.referenceDate.help='事件时间不能与实体名相同!';
            validate.referenceDate.status='error';
            isValidated=false;
        }
        if(this.state.entryName&&(this.state.entryName==this.state.referenceDate)){
            validate.referenceDate.help='事件时间不能与事件ID相同!';
            validate.referenceDate.status='error';
            isValidated=false;
        }
		if(!this.state.referenceDate){
			validate.referenceDate.help='请输入事件时间';
			validate.referenceDate.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}$/;
			let referenceDate = this.state.referenceDate;
			if(!reg.test(referenceDate)){
				validate.referenceDate.help='按照提示输入正确的事件时间';
				validate.referenceDate.status='error';
				isValidated=false;
			}
		}
		if(!this.state.referenceDateLabel){
			validate.referenceDateLabel.help='请输入事件时间显示名';
			validate.referenceDateLabel.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let referenceDateLabel = this.state.referenceDateLabel;
			if(!reg.test(referenceDateLabel)){
				validate.referenceDateLabel.help='按照提示输入正确的事件时间显示名';
				validate.referenceDateLabel.status='error';
				isValidated=false;

			}
		}

		return (
			<span>
				<Tooltip title="创建必备字段"><a onClick={this.showModal}>创建必备字段</a></Tooltip>
				<Modal title="创建必备字段" visible={this.state.visible} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form horizontal form={this.props.form}>
                        <FormItem required={true} {...formItemLayout} label="实体名：" help={validate.entityName.help} validateStatus={validate.entityName.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Input size="large" style={{marginTop:2}} type="text" name="entityName" value={this.state.entityName} onChange={this.handleChange}/>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型目标实体字段，如"deviceId"(2-30位英文字母、数字、下划线的组合，以英文字母开头)'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="实体显示名：" help={validate.entityLabel.help} validateStatus={validate.entityLabel.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Input size="large" style={{marginTop:2}} type="text" name="entityLabel" value={this.state.entityLabel} onChange={this.handleChange}/>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型目标实体显示名称，可以输入中文'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="实体类型：" help={validate.entityType.help} validateStatus={validate.entityType.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Select size="large" name="entityType" value={this.state.entityType} onChange={this.handleSelect.bind(this,'entityType')}>
		                            	<Option value="">请选择</Option>
		                                {
		                                	this.state.fieldTypes.map(x=><Option key={x.name} value={x.name}>{x.desc}</Option>)
		                                }
		                            </Select>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型目标实体类型，请选择类型'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="事件ID：" help={validate.entryName.help} validateStatus={validate.entryName.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Input size="large" style={{marginTop:2}} type="text" name="entryName" value={this.state.entryName} onChange={this.handleChange}/>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型时间ID字段，一般为模型主键，如"id"(2-30位英文字母、数字、下划线的组合，以英文字母开头)'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="事件ID显示：" help={validate.entryLabel.help} validateStatus={validate.entryLabel.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Input size="large" style={{marginTop:2}} type="text" name="entryLabel" value={this.state.entryLabel} onChange={this.handleChange}/>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型目标实体显示名称，可以输入中文'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="事件ID类型：" help={validate.entryType.help} validateStatus={validate.entryType.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Select size="large" name="entryType" value={this.state.entryType} onChange={this.handleSelect.bind(this,'entryType')}>
		                            	<Option value="">请选择</Option>
		                                {
		                                	this.state.fieldTypes.map(x=><Option key={x.name} value={x.name}>{x.desc}</Option>)
		                                }
		                            </Select>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型目标实体类型，请选择类型'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="事件时间：" help={validate.referenceDate.help} validateStatus={validate.referenceDate.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Input size="large" style={{marginTop:2}} type="text" name="referenceDate" value={this.state.referenceDate} onChange={this.handleChange}/>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型事件时间字段，如"accessTime"(2-30位英文字母、数字、下划线的组合，以英文字母开头)'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="事件时间显示：" help={validate.referenceDateLabel.help} validateStatus={validate.referenceDateLabel.status}>
                        	<Row>
                        		<Col span={20}>
                            		<Input size="large" style={{marginTop:2}} type="text" name="referenceDateLabel" value={this.state.referenceDateLabel} onChange={this.handleChange}/>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型事件时间显示名称，可以输入中文'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="事件时间类型：">
                        	<Row>
                        		<Col span={20}>
                            		<Select disabled size="large" value={this.state.referenceDateType}>
                            			<Option value="LONG">长整数</Option>
		                            </Select>
                            	</Col>                           
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'事件时间类型，固定为长整数'}>
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