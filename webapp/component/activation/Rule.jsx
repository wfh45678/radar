import React from 'react';

import {Form,Input,InputNumber,Breadcrumb,Row,Col,Icon,Card,Select,Button,Cascader,Tooltip,message,Modal} from 'antd';

const FormItem = Form.Item;
const Option = Select.Option;

import ComplexCondition from '../abstraction/ComplexCondition';

import {generateScript,validateRules} from '../utils/groovyUtil';
import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class Rule extends React.Component{

	constructor(props){
		super(props);

		if(props.rule!=undefined){
			const rule=props.rule;

			this.state={
				label:rule.label,
				initScore:rule.initScore,
				baseNum:rule.baseNum,
				operator:rule.operator,
				abstractionName:rule.abstractionName,
				rate:rule.rate,
				ruleDefinition:rule.ruleDefinition==undefined?null:JSON.parse(rule.ruleDefinition),
				scripts:rule.scripts
			}
		}
		else{
			this.state={
				label:'',
				initScore:'0',
				baseNum:'0',
				operator:'NONE',
				abstractionName:'',
				rate:'100',
				ruleDefinition:null,
				scripts:''
			}
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
		//state[name] = trim(value);
        state[name] = value;

        if(name=='operator'&&value=='NONE'){
        	state['abstractionName']='';
        }
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

		this.setState({
			ruleDefinition:ruleDefinition
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
			if(!this.state.ruleDefinition){
				Modal.error({
					title:'请至少配置一条过滤规则'
				});
				return false;
			}
			if(!validateRules(this.state.ruleDefinition)){
				Modal.error({
					title:'请检查过滤条件是否配置完整'
				});
				return false;
			}

			var param={};
			if(this.props.rule.id!=0){
				param.id=this.props.rule.id;
			}
			param.modelId=this.props.modelId;
			param.activationId=this.props.activationId;
			param.label=this.state.label;
			param.initScore=this.state.initScore,
			param.baseNum=this.state.baseNum,
			param.operator=this.state.operator,
			param.abstractionName=this.state.abstractionName,
			param.rate=this.state.rate,
			param.ruleDefinition=this.state.ruleDefinition;
			param.scripts=generateScript(this.state.ruleDefinition,"Activation");
			param.status=1;

		    FetchUtil('/rule/','PUT',JSON.stringify(param),
		    	(data) => {
		            if(data.success==true){
		            	if(this.props.rule.id==0){
		            		message.success('添加成功！');
		            	}
		            	else{
	                		message.success('修改成功!');
	                	}
	            	}
	            	else{
	            		message.error('修改失败！');	            		
	            	}
	            	this.props.reload();		            
		        });
		}
	}

	render(){
		const formItemLayout = {
            labelCol: { span: 4 },
            wrapperCol: { span: 18 },
        };

        let abstractionList=[];
        if(this.props.fieldList.length>0){
	        abstractionList=this.props.fieldList.filter(x=>x.value=='abstractions')[0].children;
	    }

        let validate={
        	label:{
        		help:'',
        		status:'success'
        	},
        	initScore:{
        		help:'',
        		status:'success'
        	},
        	baseNum:{
        		help:'',
        		status:'success'
        	},
        	abstractionName:{
        		help:'',
        		status:'success'
        	},
        	rate:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;

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
        if(!this.state.initScore){
        	validate.initScore.help='请输入初始得分';
        	validate.initScore.status='warning';
        	isValidated=false;
        }
        if(this.state.baseNum < 0){
        	validate.baseNum.help='请输入基数';
        	validate.baseNum.status='warning';
        	isValidated=false;
        }
        if(this.state.operator!='NONE'&&!this.state.abstractionName){
        	validate.abstractionName.help='请选择抽象字段';
        	validate.abstractionName.status='warning';
        	isValidated=false;
        }
        if(this.state.rate < 0 ){
        	validate.rate.help='请输入rate';
        	validate.rate.status='warning';
        	isValidated=false;
        }
        // if(!/^[0-9]+$/.test(this.state.initScore)){
        // 	validate.initScore.help='initScore必须为数字';
        // 	validate.initScore.status='error';
        // 	isValidated=false;
        // }
        // if(!/^[0-9]+$/.test(this.state.baseNum)){
        // 	validate.baseNum.help='baseNum必须为数字';
        // 	validate.baseNum.status='error';
        // 	isValidated=false;
        // }
        // if(!/^[0-9]+$/.test(this.state.rate)){
        // 	validate.rate.help='rate必须为数字';
        // 	validate.rate.status='error';
        // 	isValidated=false;
        // }

		return (
			<div>
				<div style={{width:750}}>	                
				<Form horizontal form={this.props.form}>
                    <FormItem required={true} {...formItemLayout} label="显示名称：" help={validate.label.help} validateStatus={validate.label.status}>
						<Row>
							<Col span={20}>
								<Input type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
							</Col>
							<Col span={2} offset={1}>
								<Tooltip placement="right" title={'规则名称，一般为中文，如"1天内设备注册次数过多或注册时间间隔过短"'}>
									<Icon style={{fontSize:16}} type="question-circle-o" />
								</Tooltip>
							</Col>
						</Row>
                    </FormItem>                 

                    <FormItem required={true} {...formItemLayout} label="命中初始得分：" help={validate.initScore.help} validateStatus={validate.initScore.status}>
						<Row>
							<Col span={4}>
								<InputNumber name="initScore" value={this.state.initScore} onChange={this.handleSelect.bind(this,'initScore')}/>
							</Col>
							<Col span={2} offset={1}>
								<Tooltip placement="right" title={'初始得分，在此基础上进行累加计算'}>
									<Icon style={{fontSize:16}} type="question-circle-o" />
								</Tooltip>
							</Col>
						</Row>
                    </FormItem>

                    <FormItem required={true} {...formItemLayout} label="命中基数：" help={validate.baseNum.help} validateStatus={validate.baseNum.status}>
						<Row>
							<Col span={4}>
								<InputNumber name="baseNum" value={this.state.baseNum} onChange={this.handleSelect.bind(this,'baseNum')}/>
							</Col>
							<Col span={2} offset={1}>
								<Tooltip placement="right" title={'配合操作符，与指标字段进行运算'}>
									<Icon style={{fontSize:16}} type="question-circle-o" />
								</Tooltip>
							</Col>
						</Row>
                    </FormItem>

                    <FormItem {...formItemLayout} label="操作符：">
                        <Select value={this.state.operator} onChange={this.handleSelect.bind(this,'operator')}>
                            <Option value="NONE">无</Option>
                            <Option value="ADD">加</Option>
                            <Option value="DEC">减</Option>
                            <Option value="MUL">乘</Option>
                            <Option value="DIV">除</Option>
                        </Select>
                    </FormItem>

                    <FormItem {...formItemLayout} label="指标字段：" help={validate.abstractionName.help} validateStatus={validate.abstractionName.status}>
                        <Select disabled={this.state.operator=='NONE'} value={this.state.abstractionName} onChange={this.handleSelect.bind(this,'abstractionName')}>
                            {abstractionList==undefined?null:abstractionList.map((x,index)=><Option key={x.value+index} value={x.value}>{x.label}</Option>)}
                        </Select>
                    </FormItem>

                    <FormItem required={true} {...formItemLayout} label="比率：" help={validate.rate.help} validateStatus={validate.rate.status}>
						<Row>
							<Col span={4}>
								<InputNumber name="rate" value={this.state.rate} onChange={this.handleSelect.bind(this,'rate')}/>
							</Col>
							<Col span={2} offset={1}>
								<Tooltip placement="right" title={'当指标字段值过大或者过小时，对指标字段进行放大或者缩小，使命中分数更加合理'}>
									<Icon style={{fontSize:16}} type="question-circle-o" />
								</Tooltip>
							</Col>
						</Row>
                    </FormItem>
                </Form>
                </div>
                <div>
                	<div>
				    <Tooltip title="添加过滤条件" onClick={this.handleAddCondition}><span className="addRule"><Icon type="plus" />&nbsp;添加过滤条件</span></Tooltip>
                    </div>
                    <ComplexCondition fieldList={this.props.fieldList} dataList={this.props.dataList} condition={this.state.ruleDefinition} changeParentCondition={this.handleChangeCondition} index={0}/>

					<div className="separate"></div>
					<Row>
				      	<Col span={4} offset={4}>
                        	<Button type="primary" onClick={this.handleSubmit.bind(this,isValidated)}>保存</Button>{' '}
                        	<Button type="primary" onClick={this.props.delete}>删除</Button>
                        </Col>
                    </Row>
                </div>
        	</div>
		);
	}
}