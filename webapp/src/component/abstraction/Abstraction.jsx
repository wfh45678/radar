import React from 'react';

import {Form,Input,Breadcrumb,Row,Col,Icon,Card,Select,Button,Cascader,Tooltip,message,Modal} from 'antd';

const FormItem = Form.Item;
const Option = Select.Option;
const OptGroup = Select.OptGroup;

import './Abstraction.less';

import ComplexCondition from './ComplexCondition';

import {generateScript,validateRules} from '../utils/groovyUtil';
import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class Abstraction extends React.Component{

	constructor(props){
		super(props);
		
		if(props.abstraction!=undefined){
			const abstraction=props.abstraction;

			this.state={
				name:abstraction.name,
				label:abstraction.label,
				aggregateType:abstraction.aggregateType+'',
				searchField:abstraction.searchField,
				searchIntervalType:abstraction.searchIntervalType+'',
				searchIntervalValue:abstraction.searchIntervalValue,
				functionField:abstraction.functionField,
				comment:abstraction.comment,
				ruleDefinition:abstraction.ruleDefinition==undefined?null:JSON.parse(abstraction.ruleDefinition),
				ruleScript:abstraction.ruleScript
			}
		}
		else{
			this.state={
				name:'',
				label:'',
				aggregateType:'',
				searchField:'',
				searchIntervalType:'',
				searchIntervalValue:'',
				functionField:'',
				comment:'',
				ruleDefinition:null,
				ruleScript:''
			}
		}

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
			if(this.props.abstraction.id!=0){
				param.id=this.props.abstraction.id;
			}
			if(!this.state.ruleDefinition){
				let fieldList=this.props.fieldList;
				let fieldArr=this.state.searchField.split('.');
				let fieldType='STRING';
				for(let i=0;i<fieldList.length;i++){
					if(fieldList[i].value==fieldArr[0]){
						for(let j=0;j<fieldList[i].children.length;j++){
							if(fieldList[i].children[j].value==fieldArr[1]){
								if(fieldArr.length==2){
									fieldType=fieldList[i].children[j].type;
								}
								else{
									for(let k=0;k<fieldList[i].children[j].children.length;k++){
										if(fieldList[i].children[j].children[k].value==fieldArr[2]){
											fieldType=fieldList[i].children[j].children[k].type;
											break;
										}										
									}
								}
								break;
							}							
						}
						break;
					}					
				}

				this.state.ruleDefinition={
					"linking":"All",
					"conditions":[
						{
							"class":"SMPL",
							"expressions":[
							{
								"column":this.state.searchField,
								"type":fieldType,
								"class":"ENTATTR"
							}],
							"enabled":true,
							"operator":"IsNotNull"
						}],
					"class":"PDCT",
					"enabled":true
				}
			}
			if(!validateRules(this.state.ruleDefinition)){
				Modal.error({
					title:'请检查过滤条件是否配置完整'
				});
				return false;
			}

			param.modelId=this.props.modelId;
			param.name=this.state.name;
			param.label=this.state.label;
			param.aggregateType=this.state.aggregateType;
			param.searchField=this.state.searchField;
			param.searchIntervalType=this.state.searchIntervalType;
			param.searchIntervalValue=this.state.searchIntervalValue;
			param.functionField=this.state.functionField;
			param.comment=this.state.comment;
			param.ruleDefinition=this.state.ruleDefinition;
			param.ruleScript=generateScript(this.state.ruleDefinition,"Abstraction");
			param.status=1;

		    FetchUtil('/abstraction/','PUT',JSON.stringify(param),
		    	(data) => {
		            if(data.success==true){
	                	message.success('保存成功!');
	            	}
	            	else{
	            		message.error('保存失败！');
	            	}
		            this.props.reload();
		        });
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
        state[name] = trim(value);
        this.setState(state);
    }

    displayRender = (labels, selectedOptions) => labels.map((label, i) => {
	  const option = selectedOptions[i];
	  if (i === labels.length - 1) {
	    return (
	      <span key={option.value+i}>
	        {label}
	      </span>
	    );
	  }
	  return <span key={option.value+i}>{label} / </span>;
	});

	handleCascader=(name,value,selectedOptions)=>{
		var state = this.state;
        state[name] = trim(value.join('.'));
        this.setState(state);
	}

	handleChangeCondition=(condition,index)=>{
		let ruleDefinition=this.state.ruleDefinition;

		if(condition==null){
			ruleDefinition=null;
		}
		else{
			ruleDefinition=condition;
		}
		this.setState({
			ruleDefinition:ruleDefinition
		})
	}

	handleAddCondition=()=>{
		let ruleDefinition=this.state.ruleDefinition;

		if(ruleDefinition==null){
			ruleDefinition={
			    "class": "PDCT",
			    "enabled": true,
			    "linking": "All",
			    "conditions": [
			        {
			            "class": "SMPL",
			            "enabled": true,
			            "operator": "",
			            "expressions": [
			                {
			                    "class": "ENTATTR",
			                    "type": "",
			                    "column": ""
			                }
			            ]
			        }
			    ]
			}
		}
		else{
			ruleDefinition.conditions.push(
				{
			            "class": "SMPL",
			            "enabled": true,
			            "operator": "",
			            "expressions": [
			                {
			                    "class": "ENTATTR",
			                    "type": "",
			                    "column": ""
			                }
			            ]
			        }
			);
		}

		this.setState({
			ruleDefinition:ruleDefinition
		})
	}

	render(){
		const formItemLayout = {
            labelCol: { span: 4 },
            wrapperCol: { span: 18 },
        };

        let ruleDefinition=this.state.ruleDefinition;

        let validate={
			label:{
        		help:'',
        		status:'success'
        	},
        	aggregateType:{
        		help:'',
        		status:'success'
        	},
        	searchField:{
        		help:'',
        		status:'success'
        	},
        	searchInterval:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;

		if(!this.state.label){
			validate.label.help='请输入指标名';
			validate.label.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let label = this.state.label;
			if(!reg.test(label)){
				validate.label.help='按照提示输入正确的指标名';
				validate.label.status='error';
				isValidated=false;

			}
		}
        if(!this.state.aggregateType){
        	validate.aggregateType.help='请选择聚合条件';
        	validate.aggregateType.status='warning';
        	isValidated=false;
        }
        if(!this.state.searchField){
        	validate.searchField.help='请输入搜索字段';
        	validate.searchField.status='warning';
        	isValidated=false;
        }
        if(!this.state.searchIntervalValue){
        	validate.searchInterval.help='请输入时间片';
        	validate.searchInterval.status='warning';
        	isValidated=false;
        }else if(!/^[0-9]+$/.test(this.state.searchIntervalValue)){
        	validate.searchInterval.help='时间片必须为数字';
        	validate.searchInterval.status='error';
        	isValidated=false;
        }else if(!this.state.searchIntervalType){
        	validate.searchInterval.help='请选择时间单位';
        	validate.searchInterval.status='warning';
        	isValidated=false;
        }

		return (
			<div>
			<div style={{width:750}}>	                
				<Form horizontal form={this.props.form}>

					<FormItem required={true} {...formItemLayout} label="指标名：" help={validate.label.help} validateStatus={validate.label.status}>
						<Row>
							<Col span={20}>
								<Input type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
							</Col>
							<Col span={2} offset={1}>
								<Tooltip placement="right" title={'指标显示名称，一般为中文，如"100秒内设备注册数"'}>
									<Icon style={{fontSize:16}} type="question-circle-o" />
								</Tooltip>
							</Col>
						</Row>
					</FormItem>
                    
                    <FormItem required={true} {...formItemLayout} label="聚合条件：" help={validate.aggregateType.help} validateStatus={validate.aggregateType.status}>
                        <Select value={this.state.aggregateType} onChange={this.handleSelect.bind(this,'aggregateType')}>
                            <Option value="">请选择</Option>
                            <Option value="1">求总数</Option>
                            <Option value="2">求不同项总数</Option>
                            <Option value="3">求和</Option>
                            <Option value="4">求平均值</Option>
                            <Option value="5">求最大值</Option>
                            <Option value="6">求最小值</Option>
                        </Select>
                    </FormItem>

                    <FormItem required={true} {...formItemLayout} label="搜索字段：" help={validate.searchField.help} validateStatus={validate.searchField.status}>
                        <Cascader
                        	title={this.state.searchField.split(".")}
						    options={this.props.fieldList}
						    allowClear={false}
						    value={this.state.searchField.split(".")}
						    displayRender={this.displayRender}
						    onChange={this.handleCascader.bind(this,'searchField')}
						  />
                    </FormItem>

                    <FormItem {...formItemLayout} label="统计字段：">
                        <Cascader
                        	title={this.state.functionField.split(".")}
						    options={this.props.fieldList}
						    allowClear={false}
						    value={this.state.functionField.split(".")}
						    displayRender={this.displayRender}
						    onChange={this.handleCascader.bind(this,'functionField')}
						  />

                        <Button style={{marginTop:10}} onClick={()=>{this.setState({functionField:''})}}>清空统计字段</Button>
                    </FormItem>

                    <FormItem required={true} {...formItemLayout} label="时间片：" help={validate.searchInterval.help} validateStatus={validate.searchInterval.status}>
                    	<Row>
					      <Col span={4} style={{marginTop:1}}>
					      	<Input type="text" name="searchIntervalValue" value={this.state.searchIntervalValue} onChange={this.handleChange}/>
					      </Col>
					      <Col span={4} offset={1}>
					      	<Select value={this.state.searchIntervalType} onChange={this.handleSelect.bind(this,'searchIntervalType')}>
	                            <Option value="">请选择</Option>
	                            <Option value="1">年</Option>
	                            <Option value="2">月</Option>
	                            <Option value="5">日</Option>
	                            <Option value="11">时</Option>
	                            <Option value="12">分</Option>
	                            <Option value="13">秒</Option>
	                        </Select>
					      </Col>
							<Col span={2} offset={1}>
								<Tooltip placement="right" title={'搜索某段时间的数据，数值为正数，代表范围区间，如 3 ，再配合后面的时间单位，如 月，就表示3个月内的数据'}>
									<Icon style={{fontSize:16}} type="question-circle-o" />
								</Tooltip>
							</Col>
					    </Row>                                                
                    </FormItem>

                    <FormItem {...formItemLayout} label="备注：">
                        <Input type="textarea" name="comment" value={this.state.comment} onChange={this.handleChange}/>
                    </FormItem>
                </Form>
                </div>
                    <Row>
				      	<Col span={4} offset={4}>
				      		<Tooltip title="添加过滤条件" onClick={this.handleAddCondition}><span className="addRule"><Icon type="plus" />&nbsp;添加过滤条件</span></Tooltip>
				      		<br/>
				      		&nbsp;
				      	</Col>
				    </Row>
                    
                    <ComplexCondition fieldList={this.props.fieldList} dataList={this.props.dataList} condition={this.state.ruleDefinition} changeParentCondition={this.handleChangeCondition} index={0}/>

					<div className="separate"></div>
					<Row>
				      	<Col span={4} offset={4}>
                        	<Button type="primary" onClick={this.handleSubmit.bind(this,isValidated)}>保存</Button>{' '}
                        	<Button type="primary" onClick={this.props.delete}>删除</Button>
                        </Col>
                    </Row>
        	</div>
			);
	}
}