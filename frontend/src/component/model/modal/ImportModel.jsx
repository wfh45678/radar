import React from 'react';
import {Button, Form, Modal, Input, Upload, message} from 'antd';
import {UploadOutlined} from '@ant-design/icons';

const FormItem = Form.Item;

/**
 * 模型策略包导入（v3.2）：上传 json 策略包 → 校验报告 → 导入为新模型（INIT，构建后生效）。
 */
export default class ImportModel extends React.Component {

	constructor(props){
		super(props);

		this.state={
			open:false,
			submitting:false,

			file:null,
			label:'',
			merchantCode:'',
			modelName:'',
		}
	}

	showModal=()=>{
		this.setState({
			open:true,
			file:null,
			label:'',
			merchantCode:'',
			modelName:'',
		});
	}

	handleCancel=()=>{
		if(!this.state.submitting){
			this.setState({open:false});
		}
	}

	// beforeUpload 返回 false 阻止 antd 默认上传，文件暂存 state 提交时打包
	handleFile=(file)=>{
		if(!file.name.toLowerCase().endsWith('.json')){
			message.error('请选择 .json 策略包文件');
			return false;
		}
		this.setState({file:file});
		return false;
	}

	handleChange=(e)=>{
		var name = e.target.name;
		var state = this.state;
		state[name] = e.target.value;
		this.setState(state);
	}

	handleSubmit=()=>{
		const {file, label} = this.state;
		if(!file){
			message.error('请选择策略包文件');
			return;
		}
		if(!label || !label.trim()){
			message.error('请填写模型显示名称');
			return;
		}
		this.setState({submitting:true});
		var formData = new FormData();
		formData.append('file', file);
		formData.append('label', label.trim());
		formData.append('merchantCode', this.state.merchantCode.trim());
		formData.append('modelName', this.state.modelName.trim());
		// FetchUtil 只发 JSON，multipart 单独走 fetch（带同样的鉴权头）
		fetch('/services/v1/model/import', {
			method: 'POST',
			credentials: 'include',
			headers: {'x-auth-token': localStorage.getItem('x-auth-token')},
			body: formData
		}).then((res) => res.json()).then((data) => {
			this.setState({submitting:false});
			if(data.success){
				const warnings = (data.data && data.data.warnings) || [];
				this.setState({open:false});
				if(warnings.length > 0){
					Modal.warning({
						title: '导入成功，存在 ' + warnings.length + ' 条告警',
						content: (
							<div>
								<p>新模型 id：{data.data.id}，请构建模型后生效。</p>
								<ul style={{textAlign:'left', paddingLeft:20}}>
									{warnings.map((w,i)=><li key={i}>{w}</li>)}
								</ul>
							</div>
						),
						width: 560
					});
				}else{
					message.success('导入成功（新模型 id：' + data.data.id + '），请构建模型后生效');
				}
				this.props.reload();
			}else{
				message.error(data.msg || '导入失败');
			}
		}).catch((e) => {
			this.setState({submitting:false});
			message.error('导入失败：' + e.message);
		});
	}

	render(){
		const fileRow = this.state.file ? this.state.file.name : '未选择文件';
		return (
			<span>
				<Button onClick={this.showModal}>导入模型</Button>
				<Modal title="导入模型策略包" open={this.state.open} onOk={this.handleSubmit}
					onCancel={this.handleCancel} confirmLoading={this.state.submitting}
					okText="导入">
					<Form>
						<FormItem required {...{labelCol:{span:6},wrapperCol:{span:16}}} label="策略包：">
							<Upload beforeUpload={this.handleFile} accept=".json" showUploadList={false}
								disabled={this.state.submitting}>
								<Button icon={<UploadOutlined/>}>选择 .json 文件</Button>
							</Upload>
							<span style={{marginLeft:8, color:'#888'}}>{fileRow}</span>
						</FormItem>
						<FormItem required {...{labelCol:{span:6},wrapperCol:{span:16}}} label="显示名称：">
							<Input name="label" value={this.state.label} onChange={this.handleChange}
								placeholder="导入后的模型显示名（同商户码下唯一）"/>
						</FormItem>
						<FormItem {...{labelCol:{span:6},wrapperCol:{span:16}}} label="商户码：">
							<Input name="merchantCode" value={this.state.merchantCode} onChange={this.handleChange}
								placeholder="留空使用当前商户"/>
						</FormItem>
						<FormItem {...{labelCol:{span:6},wrapperCol:{span:16}}} label="模型名：">
							<Input name="modelName" value={this.state.modelName} onChange={this.handleChange}
								placeholder="留空自动生成 model_id"/>
						</FormItem>
					</Form>
				</Modal>
			</span>
		);
	}
}
