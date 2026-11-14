import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;
const OptGroup = Select.OptGroup;
const InputGroup = Input.Group;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class EditDataListMeta extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,

			metaList:[],
			initialList:[]
		}
	}

	// 获取数据
    fetchData=()=>{
	    FetchUtil('/datalistmeta/list/'+this.props.row.id,'GET','',
	    	(data) => {
				let initialData = data.data.list.map((item)=>{
					return item.label
				});
				this.setState({
					metaList:data.data.list,
					initialList:initialData
				})
	        });
    }

    handleChange=(index,e)=>{
		var name = e.target.name;
		var value = e.target.value;

        var metaList=this.state.metaList;
        metaList[index][name]=trim(value);
        this.setState({
        	metaList:metaList
        });
    }

    addField=()=>{
    	let metaList=this.state.metaList;
    	metaList.push({
    		dataListId:this.props.row.id,
    		fieldName:'',
    		label:'',
    		seqNum:1
    	});
    	this.setState({
    		metaList:metaList
    	})
    }

    deleteField=(index)=>{
    	let metaList=this.state.metaList;
    	metaList.splice(index,1);
    	this.setState({
    		metaList:metaList
    	})
    }

	showModal=()=>{
		this.fetchData();
		this.setState({
			visible:true
		})
	}

	handleSubmit=()=>{
		let labelIsNull = this.state.metaList.some((item)=>{
			if(!item.label){
				return true;
			}
		});
		let labelIsChange = this.state.metaList.some((item,index,array)=>{
			if( (array.length > this.state.initialList.length) || (item.label !== this.state.initialList[index]) ){
				return true;
			}
		});

		let reg = /^[\u4e00-\u9fa5 \w]{2,10}$/;
		let labelReg = this.state.metaList.every((item,index,array)=>{
			if(reg.test(item.label)){
				return true;
			}
		});

		if(this.state.metaList.length==0){
			Modal.error({
		    	title: '提交失败',
		    	content: '请添加至少一个字段'
		  	});
		  	return false;
		}else if(labelIsNull){
			Modal.error({
				title: '提交失败',
				content: '字段名不能为空！'
			});
			return false;
		}else if(!labelReg){
			Modal.error({
				title: '提交失败',
				content: '字段名含有特殊字符，或者字符长度不符合！'
			});
			return false;
		}else if(labelIsChange){
			FetchUtil('/datalistmeta/','PUT',JSON.stringify(this.state.metaList),
				(data) => {
					this.setState({
						visible:false
					});
				});
		}else {
			this.setState({
				visible:false
			});
		}
	}

	handleCancel=()=>{
		this.setState({
			visible:false
		})
	}

	render(){
		return (
			<span>
				<Tooltip title="管理黑/白名单字段" onClick={this.showModal}><a>管理字段</a></Tooltip>
				<Modal title="编辑字段" visible={this.state.visible} onOk={this.handleSubmit} onCancel={this.handleCancel}>
					<Row>
						{this.state.initialList.length ?'':<Col span={6} offset={10}>
							<span className="addRule" style={{display:"block",marginBottom:10}} onClick={this.addField}><Icon type="plus" />&nbsp;添加字段</span>
						</Col>
						}
						{this.state.initialList.length ?<Col span={25} offset={2} style={{fontSize:14,marginBottom:10,color:'#f00'}}><span >现有字段不能删除，若需要删除字段，则建议直接删除列表！</span></Col>:<Col span={1} offset={1}>
							<Tooltip placement="right" title={'现有字段不能删除，若需要删除字段，则建议直接删除列表！'}>
								<Icon style={{fontSize:16,marginBottom:10}} type="question-circle-o" />
							</Tooltip>
						</Col>}
			      	</Row>
                    <Form horizontal form={this.props.form}>
	                    {this.state.metaList.map(function(info,i){
	                    	return (
	                    		<FormItem key={i+'meta'} label='字段名' labelCol={{span:10}}>
									<Col span={4} offset={1}>
										<Input name="label" value={info.label} placeholder={'字段名'} onChange={this.handleChange.bind(this,i)}/>
									</Col>
									<Col span={2} offset={1}>
										<Tooltip placement="right" title={'字段名，一般为中文，如"手机号码"，2-10位可由中文、英文字母、数字、下划线的组合'}>
											<Icon style={{fontSize:16}} type="question-circle-o" />
										</Tooltip>
									</Col>
									<Col span={1} offset={1}>
										<i onClick={this.deleteField.bind(this,i)} className="fa fa-trash" style={{fontSize:16}}/>
									</Col>
		                        </FormItem>
	                    	);
	                    }.bind(this))}	                                                                                      
                    </Form>
                </Modal>
            </span>    
		);
	}

}
