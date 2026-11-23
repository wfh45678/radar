import React from 'react';
import {Button, Modal, Tag, Tooltip, message, Spin} from 'antd';
import {ApartmentOutlined} from '@ant-design/icons';

import {FetchUtil} from '../../utils/fetchUtil';
import ImportResultContent from './ImportResultContent';

/**
 * 官方模板库（v3.4）：浏览内置+在线模板卡片，一键创建为可编辑副本。
 * 顶部细字标注在线源状态；卡片含来源徽标与规则/抽象/字段统计。
 */
export default class TemplateLibrary extends React.Component {

	constructor(props){
		super(props);

		this.state={
			open:false,
			loading:false,
			creating:null,

			templates:[],
			sourceStatus:'',
			createdLabel:'',
			createdId:null,
			createdWarnings:[]
		}
	}

	showModal=()=>{
		this.setState({
			open:true,
			loading:true,
			creating:null,
			createdId:null,
			createdWarnings:[]
		});
		FetchUtil('/model/templates','GET','',
			(data) => {
				this.setState({
					templates:data.data.templates||[],
					sourceStatus:data.data.sourceStatus||'DISABLED',
					loading:false
				});
			},
			() => this.setState({loading:false}));
	}

	handleCancel=()=>{
		this.setState({open:false});
	}

	sourceBadge=(status)=>{
		if(status==='CONNECTED'){
			return <Tag color="green">在线源已连接</Tag>;
		}
		if(status==='UNAVAILABLE'){
			return <Tag color="orange">在线源不可用·仅内置</Tag>;
		}
		return <Tag>在线源未配置·仅内置</Tag>;
	}

	sourceTagOf=(source)=>{
		return source==='REMOTE' ? <Tag color="blue">在线</Tag> : <Tag color="default">内置</Tag>;
	}

	handleCreate=(tpl, label)=>{
		if(!label || !label.trim()){
			message.error('请填写模型显示名称');
			return;
		}
		const key = tpl.key;
		this.setState({creating:key});
		var param=new URLSearchParams();
		param.append('label', label.trim());
		FetchUtil('/model/templates/'+encodeURIComponent(key)+'/import?'+param.toString(),'POST','',
			(data) => {
				this.setState({creating:null});
				const warnings=(data.data&&data.data.warnings)||[];
				this.setState({
					createdId:data.data.id,
					createdWarnings:warnings,
					createdLabel:label.trim()
				});
				this.props.reload();
			},
			() => this.setState({creating:null}));
	}

	render(){
		return (
			<span>
				<Button onClick={this.showModal} icon={<ApartmentOutlined />}>模板库</Button>
				<Modal title="官方模板库" open={this.state.open} onCancel={this.handleCancel}
					footer={null} width={680}>
					<div style={{marginBottom:10, color:'#999', fontSize:12}}>
						{this.sourceBadge(this.state.sourceStatus)}
						<span style={{marginLeft:8}}>选择场景模板一键创建为可编辑副本，构建后即生效。</span>
					</div>
					{this.state.createdId ? (
						<Modal title={'创建成功：' + this.state.createdLabel} open={true}
							onOk={()=>this.setState({createdId:null})} onCancel={()=>this.setState({createdId:null})}
							okText="知道了">
							<ImportResultContent id={this.state.createdId} warnings={this.state.createdWarnings}/>
						</Modal>
					) : null}
					<Spin spinning={this.state.loading}>
						{this.state.templates.map((tpl)=>{
							return (
								<div key={tpl.key} style={{border:'1px solid #eee', borderRadius:6, padding:'10px 12px', marginBottom:10}}>
									<div>
										<span style={{fontWeight:'bold', marginRight:8}}>{tpl.name}</span>
										{this.sourceTagOf(tpl.source)}
										{(tpl.tags||[]).map((t,i)=><Tag key={i} color="purple">{t}</Tag>)}
										<Tooltip title={'模板 key: ' + tpl.key + ' · 最低引擎版本: ' + (tpl.minRadarVersion || '3.2.0')}>
											<span style={{color:'#bbb', fontSize:12}}>{tpl.key}</span>
										</Tooltip>
									</div>
									<div style={{color:'#666', margin:'6px 0', fontSize:12, lineHeight:'18px'}}>{tpl.description}</div>
									<div style={{display:'flex', alignItems:'center'}}>
										<span style={{color:'#888', fontSize:12, flex:1}}>
											规则 {tpl.stats?.rules || 0} · 抽象 {tpl.stats?.abstractions || 0} · 字段 {tpl.stats?.fields || 0}
										</span>
										<span style={{width:260, display:'inline-flex', alignItems:'center'}}>
											<span style={{color:'#999', marginRight:4}}>显示名:</span>
											<input id={'tpl-label-' + tpl.key} className="ant-input" style={{height:24, flex:1}} maxLength={32}
												placeholder={tpl.name}/>
											<Button size="small" type="primary" style={{marginLeft:6}}
												loading={this.state.creating===tpl.key}
												onClick={()=>{
													const input=document.getElementById('tpl-label-'+tpl.key);
													this.handleCreate(tpl, input?input.value:'');
												}}>一键创建</Button>
										</span>
									</div>
								</div>
							);
						})}
						{!this.state.loading && this.state.templates.length===0 ? (
							<div style={{color:'#999', textAlign:'center', padding:'30px 0'}}>暂无可用模板</div>
						) : null}
					</Spin>
				</Modal>
			</span>
		);
	}
}
