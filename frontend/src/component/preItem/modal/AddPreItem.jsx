import React from 'react';
import {Button, Select, Form, Row, Col, Modal, Input, InputNumber, Switch, Tooltip, message} from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';

const FormItem = Form.Item;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

/**
 * preItem 新增弹窗（v3.0 起按 /common/plugins 返回的 paramSchema 动态渲染参数表单：
 * 新插件上架前端零改动；无 schema 的旧式插件回退 args 自由文本 + configJson 手填）。
 */
export default class AddPreItem extends React.Component{

	constructor(props){
		super(props);

		this.state={
			open:false,

			destField:'',
			label:'',
			sourceField:'',
			sourceLabel:'',
			plugin:'',
			status:1,
			args:'',
			reqType:'GET',
			configJson:JSON.stringify({}),
			paramValues:{}
		}

		this.submitting=false
	}

	pluginMeta=()=>{
		return (this.props.plugins||[]).find(x=>x.method===this.state.plugin);
	}

	isSchemaMode=()=>{
		const meta=this.pluginMeta();
		return !!(meta && meta.paramSchema && meta.paramSchema.length>0);
	}

	outputMeta=()=>{
		const meta=this.pluginMeta();
		return (meta && meta.outputMeta) || [];
	}

	// 多来源字段插件（与 2022 版行为一致；schema 化不改变源字段选择方式）
	isMultiSource=()=>{
		return this.state.plugin==='ALLINONE' || this.state.plugin==='HTTP_UTIL';
	}

    handleChange=(e)=>{
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = typeof value==='number'?value: trim(value);
        this.setState(state);
    }

	handleParam=(name,type,value)=>{
		const state=this.state;
		const values={...state.paramValues};
		if(value===''||value===undefined){
			delete values[name];
		}else{
			values[name]= type==='BOOLEAN' ? !!value : value;
		}
		state.paramValues=values;
		this.setState(state);
	}

    handleSelect=(name,value)=>{
    	var state = this.state;
    	if(name=='plugin'){
    		state['sourceField']='';
    		state['sourceLabel']='';
				state['args']='';
				state['status']=1;
				state['configJson']= JSON.stringify({});
				state['paramValues']={};
    	}
        state[name] = trim(value);

        if(name=='sourceField'){
        	state['sourceLabel']= this.props.fieldList.filter(x => x.fieldName==value)[0].label;
        }

        this.setState(state);
    }

    handleMultiSelect=(name,value)=>{
    	var state = this.state;
    	if(value==''){
    		state[name]='';
    	} else {
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
			reqType:'GET',
			open:true,
			configJson:JSON.stringify({}),
			paramValues:{}
		})
	}

	buildArgs=()=>{
		// schema 模式：参数序列化为 JSON 对象（引擎按 schema 解析为类型化值）
		const values={...this.state.paramValues};
		this.pluginMeta().paramSchema.forEach(d=>{
			if(values[d.name]===undefined && d.defaultValue!==undefined && d.defaultValue!==''){
				values[d.name]=d.defaultValue;
			}
		});
		return Object.keys(values).length===0 ? '' : JSON.stringify(values);
	}

	handleSubmit=()=>{
		if(this.submitting){return;}
		let configJson=this.state.configJson;
		let args=this.state.args;
		let argsValid=true;
		let argsHelp='';

		if(this.isSchemaMode()){
			// 必填校验（插件 paramSchema required）
			const values=this.state.paramValues;
			for(const d of this.pluginMeta().paramSchema){
				const v=values[d.name];
				if((v===undefined||v==='') && (d.defaultValue===undefined||d.defaultValue==='')){
					if(d.required){
						argsValid=false;
						argsHelp='请输入'+d.label;
						break;
					}
				}
				if(v!==undefined && d.type==='INTEGER' && !/^-?\d+$/.test(String(v))){
					argsValid=false;
					argsHelp=d.label+' 必须是整数';
					break;
				}
				if(v!==undefined && d.type==='DOUBLE' && isNaN(Number(v))){
					argsValid=false;
					argsHelp=d.label+' 必须是数字';
					break;
				}
			}
			args=this.buildArgs();
			const om=this.outputMeta();
			if(om.length>0){
				configJson=JSON.stringify(om.map(c=>({column:c.name,title:c.title,type:c.type})));
			}
		}else{
			if(typeof JSON.parse(this.state.configJson)!='object'){
				return message.error('多文本框json格式不对');
			}
		}

		if(!this.state.plugin || !this.state.label || !this.state.sourceField || !argsValid){
			Modal.error({
			    title: '提交失败',
			    content: argsValid ? '请确认表单内容输入正确' : argsHelp,
			  });
			return;
		}

		this.submitting=true;
		var param={};
		param.modelId=this.props.modelId;
		param.destField=this.state.destField;
		param.label=this.state.label;
        param.sourceField=this.state.sourceField;
        param.sourceLabel=this.state.sourceLabel;
        param.plugin=this.state.plugin;
		param.status=this.state.status;
		param.reqType=this.state.reqType;
		param.args=args;
		param.configJson=JSON.parse(configJson);
		FetchUtil('/preitem/','PUT', JSON.stringify(param),
	    	(data) => {
	    		this.submitting=false;
	    		if(data.success){
	    			message.success('添加成功');
	    		}else{
	    			message.error(data.msg);
	    		}
	            this.setState({
	            	open:false
	            });
	            this.props.reload();
	        });
	}

	handleCancel=()=>{
		this.setState({
			open:false
		})
	}

	renderParamField=(d)=>{
		// 按 ParamDescriptor 动态渲染：options 非空->Select（v3.1）/ BOOLEAN->Switch / INTEGER,DOUBLE->InputNumber / STRING->Input
		const value = this.state.paramValues[d.name] !== undefined
			? this.state.paramValues[d.name]
			: (d.defaultValue !== undefined ? d.defaultValue : (d.type==='BOOLEAN' ? false : ''));
		const commonProps = {
			placeholder: d.placeholder || (d.defaultValue !== undefined ? '默认: ' + d.defaultValue : ''),
			style:{width:'100%'}
		};
		let control;
		if(d.options && d.options.length>0){
			// v3.1 枚举选项：label 展示、value 入库（恒为字符串）
			control = <Select {...commonProps} value={value||''}
				onChange={(v)=>this.handleParam(d.name,'STRING',v)}
				options={[{label:'请选择', value:''}, ...d.options.map(o=>({label:o.label, value:o.value}))]} />;
		}else if(d.type==='BOOLEAN'){
			control = <Switch checked={!!value} onChange={(v)=>this.handleParam(d.name,'BOOLEAN',v)} />;
		}else if(d.type==='INTEGER' || d.type==='DOUBLE'){
			control = <InputNumber {...commonProps} value={value===undefined||value===''?null:value}
				onChange={(v)=>this.handleParam(d.name,d.type,v)} />;
		}else{
			control = <Input {...commonProps} value={value}
				onChange={(e)=>this.handleParam(d.name,'STRING',e.target.value)} />;
		}
		return (
			<FormItem key={d.name} required={d.required} {...{labelCol:{span:6},wrapperCol:{span:16}}} label={d.label+'：'}>
				<Row>
					<Col span={20}>{control}</Col>
					<Col span={2} offset={1}>
						<Tooltip placement="right" title={d.placeholder || d.label}>
							<QuestionCircleOutlined style={{fontSize:16}}/>
						</Tooltip>
					</Col>
				</Row>
			</FormItem>
		);
	}

	render(){
		const formItemLayout = {
            labelCol: { span: 6 },
            wrapperCol: { span: 16 },
        };

        let validate={
        	plugin:{help:'',status:'success'},
        	label:{help:'',status:'success'},
			sourceField:{help:'',status:'success'}
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

        const plugin=this.state.plugin;
        const schemaMode=this.isSchemaMode();
        const om=this.outputMeta();
        let fieldArr=this.state.sourceField==''?[]:this.state.sourceField.split(',');
		return (
			<span>
				<Button onClick={this.showModal} type="primary">新增</Button>
				<Modal title="编辑字段" open={this.state.open} onOk={this.handleSubmit} onCancel={this.handleCancel}>
                    <Form>
                    	<FormItem required={true} {...formItemLayout} label="插件种类：" help={validate.plugin.help} validateStatus={validate.plugin.status}>
							<Row>
								<Col span={20}>
									<Select value={this.state.plugin} onChange={this.handleSelect.bind(this,'plugin')} options={[{label:'请选择', value:''}, ...this.props.plugins.map(x=>({label:x.desc, value:x.method}))]}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'插件种类，由插件 jar 提供；参数表单按插件声明的参数描述自动生成'}>
		                            	<QuestionCircleOutlined style={{fontSize:16}}/>
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
		                            	<QuestionCircleOutlined style={{fontSize:16}}/>
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>
                        <FormItem required={true} {...formItemLayout} label="原始字段名：" style={!this.isMultiSource()?{}:{display:"none"}} help={validate.sourceField.help} validateStatus={validate.sourceField.status}>
							<Row>
								<Col span={20}>
									<Select value={this.state.sourceField} onChange={this.handleSelect.bind(this,'sourceField')} options={[{label:'请选择', value:''}, ...this.props.fieldList==null?'':this.props.fieldList.map(x=>({label:x.label, value:x.fieldName}))]}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'原始字段名，均为自己定义的字段名'}>
		                            	<QuestionCircleOutlined style={{fontSize:16}}/>
		                            </Tooltip>
                            	</Col>
                            </Row>
	                    </FormItem>
                        <FormItem required={true} {...formItemLayout} label="原始字段名：" style={this.isMultiSource()?{}:{display:"none"}} help={validate.sourceField.help} validateStatus={validate.sourceField.status}>
							<Row>
								<Col span={20}>
									<Select mode="multiple" placeholder="请选择" value={fieldArr} onChange={this.handleMultiSelect.bind(this,'sourceField')} options={[...this.props.fieldList==null?'':this.props.fieldList.map(x=>({label:x.label, value:x.fieldName}))]}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'原始字段名，均为自己定义的字段名'}>
		                            	<QuestionCircleOutlined style={{fontSize:16}}/>
		                            </Tooltip>
                            	</Col>
                            </Row>
	                    </FormItem>

						{schemaMode
							? this.pluginMeta().paramSchema.map(d=>this.renderParamField(d))
							: (plugin!=='' &&
								<FormItem {...formItemLayout} label="插件参数（可选）">
									<Row>
										<Col span={20}>
											<Input.TextArea name="args" value={this.state.args} onChange={this.handleChange} rows={2} placeholder="插件参数（该插件未声明参数描述，请按插件说明填写）" />
	                            		</Col>
	                            		<Col span={2} offset={1}>
				                            <Tooltip placement="right" title={'该插件未声明参数 schema，参数以自由文本传递'}>
				                            	<QuestionCircleOutlined style={{fontSize:16}}/>
				                            </Tooltip>
	                            		</Col>
	                            	</Row>
								</FormItem>)}

						{(!schemaMode || om.length===0) && plugin!==''
							? <FormItem {...formItemLayout} label="响应列描述：">
									<Row>
										<Col span={20}>
											<Input.TextArea name="configJson" value={this.state.configJson} onChange={(e)=>this.handleChange(e)} rows={4} placeholder='响应结果字段描叙信息，如：[{"column":"country","title":"国家","type":"STRING"}]' />
	                            		</Col>
	                            		<Col span={2} offset={1}>
				                            <Tooltip placement="right" title={'多列结果插件需声明响应列；声明了 outputMeta 的插件会自动生成，无需手填'}>
				                            	<QuestionCircleOutlined style={{fontSize:16}}/>
				                            </Tooltip>
	                            		</Col>
	                            	</Row>
								</FormItem>
							: (schemaMode && om.length>0 &&
								<FormItem {...formItemLayout} label="响应列描述：">
									<Row><Col span={20}>
										<span style={{color:'#999'}}>由插件自动生成（{om.length} 列：{om.map(c=>c.title).join(' / ')}）</span>
									</Col></Row>
								</FormItem>)}
                    </Form>
                </Modal>
            </span>
		);
	}

}
