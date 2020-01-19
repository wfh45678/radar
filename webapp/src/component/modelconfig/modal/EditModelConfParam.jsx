import React from 'react';
import {Button,Checkbox,Select,Radio,Switch,Form,Row,Col,Icon,Modal,Input,InputNumber,Cascader,Tooltip,message } from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

import {FetchUtil} from '../../utils/fetchUtil';
import {trim} from '../../utils/validateUtil';

export default class EditModelConfParam extends React.Component{

	constructor(props){
		super(props);

		this.state={
			visible:false,
			id :-1,
			feed:'',
			selectList:[],
			indexed:false
		}

	    FetchUtil('/modelConfigParam/' + this.props.paramId,'GET','',
	    	(data) => {
	    		let param = data.data.param;
	            this.setState({
	            	id:param.id,
	            	feed: param.feed,
	            	selectList: param.expressions.replace(/abstractions./g,"").split(","),
	            })
	    });

	   //  FetchUtil('/activation/absColumns/' + this.props.params.id,'GET','',
	   //  	(data) => {
	   //  		let absColumns =[];
	   //          let absDS= data.data.columns;
	   //      	for (let i = 0; i < absDS.length; i++) {
				// 	  absColumns.push(<Option key={absDS[i].value}>{absDS[i].label}</Option>);
				// }
				// //console.log(absColumns);
				// this.setState({
				// 	absColumns: absColumns,
				// });
	  	// });
	}

	// 获取表格数据
    // fetchData=()=>{
	   //  FetchUtil('/modelConfigParam/'+ this.state.id,'GET',null,
	   //  	(data) => {
	   //          const param=data.data.param;
	   //      	this.setState({
	   //            	id:param.id,
	   //           	feed: param.feed,
	   //           	expressions: param.expressions,
	   //      	});
	   //      }
	   //  )    
    // }

    handleChange=(e)=>{
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

    handleSelect=(value)=>{
        this.setState({selectList: value});
    }

	showModal=()=>{
		//this.fetchData();
		this.setState({
			visible:true
		})
	}

	onCheck=(e)=>{
		if(e.target.checked && this.props.indexedAll>=8){
			Modal.warning({
				title: '提示信息',
				content: '索引已超过8项！',
			});
		}else {
			this.setState({
				indexed:e.target.checked
			});
		}		
	};

	handleSubmit=(validated)=>{
		if(!validated){
			Modal.error({
			    title: '提交失败',
			    content: '请确认表单内容输入正确',
			  });
		}
		else{
			var param = {};
			param.id = this.state.id;
			param.feed = this.state.feed;
			param.expressions = this.state.selectList.map(item => 'abstractions.' + item).join();

			FetchUtil('/modelConfigParam/', 'PUT', JSON.stringify(param),
				(data) => {
					if (data.success) {
						message.success('修改成功');
					} else {
						message.error(data.msg);
					}
					this.setState({
						visible: false
					});
					this.props.reload();
				});
		}
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

		let validate={
        	feed:{
        		help:'',
        		status:'success'
        	},
			label:{
        		help:'',
        		status:'success'
        	},
        	fieldType:{
        		help:'',
        		status:'success'
        	}
        };
        let isValidated=true;

		if(!this.state.feed){
			validate.feed.help='请输入参数名称';
			validate.feed.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{2,29}$/;
			let feed = this.state.feed;
			if(!reg.test(feed)){
				validate.feed.help='按照提示输入正确的名称';
				validate.feed.status='error';
				isValidated=false;
			}
		}
		

		return (
			<span>
				<Tooltip title="编辑" onClick={this.showModal}><a>编辑</a></Tooltip>
				<Modal title="编辑参数" visible={this.state.visible} onOk={this.handleSubmit.bind(this, isValidated)} onCancel={this.handleCancel}>
                    <Form layout="horizontal" form={this.props.form}>
                        <FormItem required={true} {...formItemLayout} label="feed：" help={validate.feed.help} validateStatus={validate.feed.status}>
							<Row>
                        		<Col span={20}>
									<Input type="text" name="feed" value={this.state.feed} onChange={this.handleChange} />
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'2-30位英文字母、数字、下划线的组合，以英文字母开头：xyz001'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
		                            </Tooltip>
                            	</Col>
                            </Row>
                        </FormItem>

                        <FormItem required={true} {...formItemLayout} label="特征指标：">
							<Row>
								<Col span={10}>
	
									<Select
								          mode="tags"
								          size={'default'}
								          placeholder="Please select"
								          value={this.state.selectList}
								          onChange={this.handleSelect}
								          style={{ width: '100%' }}
								        >
								          {this.props.abstractions}
	        						</Select>
                            	</Col>
                            	<Col span={2} offset={1}>
		                            <Tooltip placement="right" title={'选择模型需要的特征指标'}>
		                            	<Icon style={{fontSize:16}} type="question-circle-o" />
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
