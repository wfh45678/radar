import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,Upload, message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class AddDataListRecord extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,
			importVisible: false,
			dataRecord:'',
			fieldNum:this.props.metaList.length
		}

	}

	componentDidMount() {
		console.log(this.props);
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
			dataRecord: '',
			visible: true
		})
	}

	showModal2=()=>{
		this.setState({
			
			importVisible:true
		})
	}

	handleSubmit=()=>{
		var param={};
		param.dataListId=this.props.dataListId;
		param.dataRecord=this.state.dataRecord;

	    FetchUtil('/datalistrecord/','PUT', JSON.stringify(param),
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

	handleCancel2=()=>{
		this.setState({
			importVisible:false
		})
	}
	render(){
		const formItemLayout = {
            labelCol: { span: 6 },
            wrapperCol: { span: 16 },
        };
        const uploadProps = {
			  name: 'file',
			  data: {"dataListId": this.props.dataListId},
			  action: '/services/v1/datalistrecord/batchImportDataRecord',
			  headers: {
			    "x-auth-token": localStorage.getItem('x-auth-token'),
			  },
			  onChange(info) {
			    if (info.file.status !== 'uploading') {
			      console.log(info.file, info.fileList);
			    }
			    if (info.file.status === 'done') {
			      message.success(`${info.file.name} file uploaded successfully`);
			    } else if (info.file.status === 'error') {
			      message.error(`${info.file.name} file upload failed.`);
			    }
			  },
		};
        let valueArr=this.state.dataRecord.split(',');
		return (
			<span>
				<Button onClick={this.showModal} type="primary">新增</Button> &nbsp;&nbsp;
				<Button onClick={this.showModal2} type="primary">导入数据</Button>
				<Modal title="新增记录" visible={this.state.visible} onOk={this.handleSubmit} onCancel={this.handleCancel}>
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
                <Modal title="导入数据" visible={this.state.importVisible} onCancel={this.handleCancel2} onOk={this.handleCancel2}>
					<Upload {...uploadProps}>
					    <Button>
					      <Icon type="upload" /> Click to Upload
					    </Button>
				  	</Upload>
                </Modal>
            </span>    
		);
	}

}
