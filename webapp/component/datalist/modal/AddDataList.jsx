import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class AddDataList extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,

			name:'',
			label:'',
			comment:'',
			listType:'',
			status:"1",
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

	showModal=()=>{
		this.setState({
			visible:true,

			name:'',
			label:'',
			comment:'',
			listType:'',
			status:"1",
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
			param.name=this.state.name;
			param.label=this.state.label;
			param.comment=this.state.comment;
			param.listType=this.state.listType;
			param.status=this.state.status;

		    FetchUtil('/datalist/','PUT',JSON.stringify(param),
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
        	label:{
        		help:'',
        		status:'success'
        	},
        	listType:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;
		
		if(!this.state.label){
			validate.label.help='请输入列表名';
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
        if(!this.state.listType){
        	validate.listType.help='请选择名单类型';
        	validate.listType.status='warning';
        	isValidated=false;
        }

		return (
			<span>
				<Button onClick={this.showModal} type="primary">新增</Button>
				<Modal title="编辑字段" visible={this.state.visible} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form horizontal form={this.props.form}>
                        <FormItem {...formItemLayout} label="列表名：" help={validate.label.help} validateStatus={validate.label.status}>
							<Row>
								<Col span={20}>
									<Input type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'列表显示名称，一般为中文，如"注册手机黑名单"'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>

                        <FormItem {...formItemLayout} label="备注：">
                            <Input type="text" name="comment" value={this.state.comment} onChange={this.handleChange}/>
                        </FormItem>
                        <FormItem {...formItemLayout} label="名单类型：" help={validate.listType.help} validateStatus={validate.listType.status}>
                            <Select value={this.state.listType} style={{ width: 120 }} onChange={this.handleSelect.bind(this,'listType')}>
                                <Option value="">请选择</Option>
                                <Option value="BLACK">黑名单</Option>
                                <Option value="WHITE">白名单</Option>
                            </Select>
                        </FormItem>
                    </Form>
                </Modal>
            </span>    
		);
	}

}
