import React from 'react';
import {Button,Form,Row,Col,Modal,Input,Tooltip,message } from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';

const FormItem = Form.Item;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class CopyScenario extends React.Component{

	constructor(props){
		super(props);

		this.state={
			open:false,

			scenarioName:'',
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
				content: '场景名重复',
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
			open:true,

			scenarioName:'',
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
	        param.scenarioName='';
	        param.label=this.state.label;

			FetchUtil('/scenario/copy','POST',JSON.stringify(param),
		    	(data) => {
		    		if(data.success){
		    			message.success('复制成功');
		    		}else{
		    			message.error(data.msg);
		    		}
		            this.setState({
		            	open:false
		            });
		            this.props.reload();
		        });
		}
	}

	handleCancel=()=>{
		this.setState({
			open:false
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
			validate.label.help='请输入场景名';
			validate.label.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let label = this.state.label;
			if(!reg.test(label)){
				validate.label.help='按照提示输入正确的场景名';
				validate.label.status='error';
				isValidated=false;
			}
		}

		return (
			<span>
				<Tooltip title="复制场景" onClick={this.showModal}><a>复制</a></Tooltip>
				<Modal title="复制场景" open={this.state.open} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form>
                    	<FormItem {...formItemLayout} label="复制场景：">
                        	<label>{this.props.row.label}</label>
                        </FormItem>
						<FormItem required={true} {...formItemLayout} label="新场景名：" help={validate.label.help} validateStatus={validate.label.status}>
							<Row>
								<Col span={20}>
                            		<Input size="large" type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'场景显示名称，一般为中文，如"注册场景"'}>
		                            	<QuestionCircleOutlined style={{fontSize:16}} />
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
