import React from 'react';

import {Form,Input,Row,Col,Card,Select,Button,Cascader,Tooltip,Icon} from 'antd';

const FormItem = Form.Item;
const Option = Select.Option;
const OptGroup = Select.OptGroup;

import {Operator,operatorMap} from '../utils/operatorUtil';

export default class SimpleCondition extends React.Component{

	constructor(props){
		super(props);
	}

	handleConditionColumn=(name,value,selectedOptions)=>{
		let type=selectedOptions[selectedOptions.length-1].type;
		let condition=this.props.condition;
		let index=this.props.index;
		if(name=='expression'){
			condition.operator='';
			condition.expressions=condition.expressions.slice(0,1);
			condition.expressions[0]={
				class: "ENTATTR", 
				type: type, 
				column: value.join('.')
			}
		}
		else if(name=='expressionOption'){
			if(condition.expressions.length<=1){
				condition.expressions.push({
					class: "ENTATTR", 
					type: type, 
					column: value.join('.')
				})
			}
			else{
				condition.expressions[1]={
					class: "ENTATTR", 
					type: type, 
					column: value.join('.')
				}
			}
		}

		this.props.changeParentCondition(condition,index);
	}

	handleOperator=(value)=>{
		let condition=this.props.condition;
		let index=this.props.index;

		condition.operator=value;
		condition.expressions=condition.expressions.slice(0,1);

		this.props.changeParentCondition(condition,index);
	}

	handleDataList=(value)=>{
		let condition=this.props.condition;
		let index=this.props.index;

		if(condition.expressions.length<=1){
			condition.expressions.push({
				class: "CONST", 
				type: "LIST", 
				value: value
			})
		}
		else{
			condition.expressions[1]={
				class: "CONST", 
				type: "LIST", 
				value: value
			}
		}
		this.props.changeParentCondition(condition,index);
	}

	handleInput=(e)=>{
		var name = e.target.name;
        var value = e.target.value;

        let condition=this.props.condition;
		let index=this.props.index;
		const type=condition.expressions[0].type;
		if(condition.expressions.length<=1){
			condition.expressions.push({
				class: "CONST", 
				type: type, 
				value: value
			})
		}
		else{
			condition.expressions[1]={
				class: "CONST", 
				type: type, 
				value: value
			}
		}
		this.props.changeParentCondition(condition,index);
	}

	handleDelete=()=>{
		this.props.changeParentCondition(null,this.props.index);
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
	})

	render(){
		let condition=this.props.condition;
		let expression=condition.expressions[0];
		let expressionOption=condition.expressions[1];
		return (
			<div className="condition-row" style={{marginLeft:20}}>
  					<Cascader
					    options={this.props.fieldList}
					    allowClear={false}
					    value={expression.column.split('.')}
					    displayRender={this.displayRender}
					    onChange={this.handleConditionColumn.bind(this,'expression')}
						readOnly={this.props.readOnly}

					  />
					<Select dropdownMatchSelectWidth={false} readOnly={this.props.readOnly} value={condition.operator} onChange={this.handleOperator}>
                    	{operatorMap[expression.type].map(op=>
                    		<Option key={op.value} value={op.value}>{op.label}</Option>
                    	)}    	                        
                    </Select>
				{condition.operator==''?'':
					Operator[condition.operator].nextType=='input'?(
						<Input style={{width:200}} type="text" readOnly={this.props.readOnly} value={expressionOption==undefined?'':expressionOption.value} onChange={this.handleInput}/>):
					Operator[condition.operator].nextType=='list'?(
						<Select dropdownMatchSelectWidth={false} readOnly={this.props.readOnly} value={expressionOption==undefined?'':expressionOption.value} onChange={this.handleDataList}>
                        	{this.props.dataList.map(op=>
                        		<Option key={op.id} value={op.name}>{op.label}</Option>
                        	)}    	                        
                        </Select>
					):
					Operator[condition.operator].nextType=='field'?(
						<Cascader
						    options={this.props.fieldList}
						    allowClear={false}
						    value={expressionOption==undefined?'':expressionOption.column.split('.')}
						    displayRender={this.displayRender}
						    onChange={this.handleConditionColumn.bind(this,'expressionOption')}
							readOnly={this.props.readOnly}
						  />
					):''					
				}
				{this.props.readOnly!=true?<Tooltip title="删除" onClick={this.handleDelete}><Icon type="delete" style={{fontSize:16,lineHeight:"22px"}}/></Tooltip>:''}
			</div>
		);
	}
}