import React from 'react';

import {Form, Input, Row, Col, Card, Select, Button, Cascader, Tooltip} from 'antd';
import {PlusOutlined} from '@ant-design/icons';


import SimpleCondition from './SimpleCondition';

export default class ComplexCondition extends React.Component{

	constructor(props){
		super(props);
	}

	handleSelect=(name,value)=>{
		let condition=this.props.condition;
		let index=this.props.index;
		condition.linking=value;

		this.props.changeParentCondition(condition,index);
	}

	handleChangeCondition=(childCondition,childIndex)=>{
		let condition=this.props.condition;
		let index=this.props.index;

		if(childCondition==null){
			condition.conditions.splice(childIndex,1);
		}
		else{
			condition.conditions[childIndex]=childCondition;
		}

		if(condition.conditions.length==0){
			this.props.changeParentCondition(null,index);
		}
		else{
			this.props.changeParentCondition(condition,index);
		}
	}

	handleSimpleCondition=()=>{
		let condition=this.props.condition;
		let index=this.props.index;

		condition.conditions.push(
			{
	            "class": "SMPL",
	            "enabled": true,
	            "operator": "",
	            "expressions": [
	                {
	                    "class": "ENTATTR",
	                    "type": "STRING",
	                    "column": ""
	                }
	            ]
	        }
		)

		this.props.changeParentCondition(condition,index);
	}

	handleComplexCondition=()=>{
		let condition=this.props.condition;
		let index=this.props.index;

		condition.conditions.push(
			{
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
			                    "type": "STRING",
			                    "column": ""
			                }
			            ]
			        }
			    ]
			}
		);

		this.props.changeParentCondition(condition,index);
	}

	render(){
		if(this.props.condition==null){
			return (<div/>);
		}
		else{
			return (
				<div style={{marginLeft:20}}>
					<div className="condition-row">
	            		<Select style={{width:100}} readOnly={this.props.readOnly} value={this.props.condition.linking} defaultValue={'All'} onChange={this.handleSelect.bind(this,'linking')} options={[{label:'All', value:'All'}, {label:'Any', value:'Any'}, {label:'NotAll', value:'NotAll'}, {label:'None', value:'None'}]}/>条件成立 &nbsp;&nbsp;
						{this.props.readOnly!=true?<Tooltip title="添加简单条件" onClick={this.handleSimpleCondition}><span style={{cursor:"pointer",marginRight:"10px"}}><PlusOutlined />简单</span></Tooltip>:''}
						{this.props.readOnly!=true?<Tooltip title="添加复杂条件" onClick={this.handleComplexCondition}><span style={{cursor:"pointer"}}><PlusOutlined />复杂</span></Tooltip>:''}
	                </div>                    
		      		{this.props.condition.conditions.map((info,index)=>{
		      			if(info.class=='SMPL'){
			      			return (
			      				<SimpleCondition key={index} readOnly={this.props.readOnly} fieldList={this.props.fieldList} dataList={this.props.dataList} condition={info} changeParentCondition={this.handleChangeCondition} index={index} />
			      			);
			      		}
			      		else{
			      			return (
			      				<ComplexCondition key={index} readOnly={this.props.readOnly} fieldList={this.props.fieldList} dataList={this.props.dataList} condition={info} changeParentCondition={this.handleChangeCondition} index={index}/>
			      			);
			      		}
		      		})}				    
			    </div>
			);
		}
	}
}