import React from 'react';
import {Button,Select,Form,Row,Col,Modal,Input,Tooltip,message } from 'antd';
import {QuestionCircleOutlined} from '@ant-design/icons';

const FormItem = Form.Item;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class AddModel extends React.Component{

	constructor(props){
		super(props);

		this.state={
			open:false,

			label:'',

			templateList:[],
			templateId:''
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
			open:true,
			label:'',
		});
		FetchUtil('/model/list/template','GET','',
	    	(data) => {
	            this.setState({
	            	templateList:data.data.modelList
	            })
	        });
	}

	handleSubmit=(validated)=>{
		if(!validated){
			Modal.error({
			    title: '提交失败',
			    content: '请确认表单内容输入正确',
			  });
		}
		else if(this.state.templateId !="-999"){
			var param={};
			param.id=this.state.templateId;
	        param.modelName='';
	        param.label=this.state.label;
	        param.entityName='';
	        param.entryName='';
	        param.referenceDate='';

			FetchUtil('/model/copy','POST',JSON.stringify(param),
		    	(data) => {
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
	else{
			var param={};
	        param.modelName='';
	        param.label=this.state.label;
	        param.entityName='';
	        param.entryName='';
	        param.referenceDate='';
	        
			FetchUtil('/model/','PUT',JSON.stringify(param),
		    	(data) => {
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
				<Button onClick={this.showModal} type="primary">新建模型</Button>
				<Modal title="编辑模型" open={this.state.open} onOk={this.handleSubmit.bind(this,isValidated)} onCancel={this.handleCancel}>
                    <Form>
						<FormItem required={true} {...formItemLayout} label="模型名：" help={validate.label.help} validateStatus={validate.label.status}>
							<Row>
								<Col span={20}>
                            		<Input size="large" type="text" name="label" value={this.state.label} onChange={this.handleChange}/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'模型显示名称，一般为中文，如"注册模型"'}>
		                            	<QuestionCircleOutlined style={{fontSize:16}} />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>

                        <FormItem required={true} {...formItemLayout} label="模板：">
                        	<Row>
								<Col span={20}>
                            		<Select value={this.state.templateId} onChange={this.handleSelect.bind(this,'templateId')}
                            			options={[
                            				...this.state.templateList.map((info)=>({label:'[系统]'+info.label, value:info.id+''})),
                            				{label:'新建模型', value:'-999'}
                            			]}
                            		/>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'建议使用模板一键创建模型，熟悉后可自行创建模型'}>
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
