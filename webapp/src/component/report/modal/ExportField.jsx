import React from 'react';
import {Button,Modal,Tree,Tooltip} from 'antd';

const TreeNode = Tree.TreeNode;

import {FetchUtil,fetchVersion} from '../../utils/fetchUtil';

export default class ExportField extends React.Component{

	constructor(props){
		super(props);

		this.state={
			loading:false,
			visible:false,

			selectedKeys:[]
		};

		console.log(this.props.eventFieldList);
	}
	

	showModal=()=>{
		this.setState({
			visible:true
		});
	}

	handleOk=()=>{
		if(this.state.selectedKeys.length==0){
			Modal.error({
				title: '请选择需要导出的字段'
			});
		}

		let param={
			fields:[],
			preItems:[],
			activations:[],
			rules:[]
		};
		this.state.selectedKeys.forEach((info)=>{
			if(info&&info.indexOf('.')!=-1){
				let arr=info.split('.');
				param[arr[0]].push(arr[1]);
			}
		});
		FetchUtil('/event/export','POST',JSON.stringify(param),
	    	(data) => {
	    		var url=fetchVersion+"/event/download";
	    		var a = document.createElement('a');
				var filename = 'download.xlsx';
				a.href = url;
				a.download = filename;
				a.click();
	        });
	}

	handleCancel=()=>{
		this.setState({
			selectedKeys:[],
			visible:false
		})
	}

	onSelect=(selectedKeys)=>{
		this.setState({ selectedKeys });
	}

	render(){
		return (
			<span>
				{this.props.disabled?
				<Tooltip title="导出前请先执行查询！">
				<Button disabled type="primary" loading={this.state.loading} onClick={this.showModal}>导出</Button>
				</Tooltip>
				:<Button type="primary" loading={this.state.loading} onClick={this.showModal}>导出</Button>
				}
				<Modal title="选择需要导出的字段" visible={this.state.visible} maskClosable={false} closable={false} onOk={this.handleOk} okText={'导出报表'} onCancel={this.handleCancel}>
					{this.props.eventFieldList.length==0?'该模型无字段可选':
					<Tree checkable onCheck={this.onCheck} onCheck={this.onSelect} selectedKeys={this.state.selectedKeys}>					
						{this.props.eventFieldList.map((info,i)=>{
							return (
								<TreeNode key={info.value} title={info.label}>
									{info.children.map((child,i)=>{
										return <TreeNode key={info.value+'.'+child.value} title={child.label}/>
									})}
								</TreeNode>
							);						
						})}
					</Tree>
					}
                </Modal>
			</span>
		)
	}

}