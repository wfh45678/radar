import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class EditDataListRecord extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,

			dataRecord:this.props.row.dataRecord,
			fieldNum:this.props.metaList.length
		}
	}

    handleChange=(index,e)=>{
    	var value=e.target.value;
    	var valueArr=this.state.dataRecord.split(',');
    	if(valueArr.length<this.state.fieldNum){
    		var newArr=new Array(this.state.fieldNum);
    		for(var i=0;i<this.state.fieldNum;i++){
    			newArr[i]=valueArr[i]!=undefined?valueArr[i]:'';
    		}

    		valueArr=newArr;
    	}

    	valueArr[index]=trim(value);
    	this.setState({
    		dataRecord:valueArr.join(',')
    	});
    }

    handleSelect=(name,value)=>{
    	var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

	showModal=()=>{
		this.setState({
			visible:true
		})
	}

	handleSubmit=()=>{
		var param={};
		param.id=this.props.row.id;
		param.dataListId=this.props.dataListId;
		param.dataRecord=this.state.dataRecord;

	    FetchUtil('/datalistrecord/','PUT',JSON.stringify(param),
	    	(data) => {
	            this.setState({
	            	visible:false
	            });
	            this.props.reload();
	        });
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
        let valueArr=this.state.dataRecord.split(',');
		return (
			<span>
				<Tooltip title="编辑" onClick={this.showModal}><a>编辑</a></Tooltip>
				<Modal title="编辑记录" visible={this.state.visible} onOk={this.handleSubmit} onCancel={this.handleCancel}>
                    <Form horizontal form={this.props.form}>
                    	{this.props.metaList.map(function(info,i){
                    		return (
                    			<FormItem {...formItemLayout} key={'meta'+info.id} label={info.label}>
		                            <Input type="text" value={valueArr[i]} onChange={this.handleChange.bind(this,i)}/>
		                        </FormItem>
                    		);
                    	}.bind(this))}                                                                
                    </Form>
                </Modal>
            </span>    
		);
	}

}
