import React from 'react';
import {Breadcrumb, Menu, Icon, Form, Upload, Input, Select, Card, Row, Col,Button, Tooltip, Tag, Modal, message} from 'antd';

const FormItem=Form.Item;
const Option = Select.Option;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

import EditModelConfParam from './modal/EditModelConfParam';

export default class ModelConfig extends React.Component{
	constructor(props){
		super(props);

		this.state={
			visible:false,
			id : -1,
			status:1,
			args:'',
			tags: ['argx=1', 'argy=2', 'argz=3'],
		    inputVisible: false,
		    inputValue: '',
            model: null,
            modelConfig: null, 
            name: "",
            type: "TENSOR_DNN",
            path: "",
            comment:"",
            tag: "",
            operation: "",
            absColumns: [],
            selectCols: [],
            fileList:[],
            paramsList:[],
            selectedKeys:[],
            feed: "",

		}

	    FetchUtil('/model/'+this.props.params.id,'GET','',
        (data) => {
            const model=data.data.model;
            this.setState({
                model:model
            });
        });

	    FetchUtil('/modelConfig/list/' + this.props.params.id,'GET','',
	    	(data) => {
	            const modelConfig=data.data.modelConfig;
	            let paramVO = modelConfig.params[0];
	            let selectCols = [];
	            selectCols = paramVO.expressions.replace(/abstractions./g,"").split(",");
	        	this.setState({
	        		modelConfig: modelConfig,
	        		id: modelConfig.id,
	        		name: modelConfig.name,
	        		type: modelConfig.type,
	        		path: modelConfig.path,
	        		comment: modelConfig.comment,
	        		tag: modelConfig.tag,
	        		operation: modelConfig.operation,
	        		selectCols: selectCols,
	        		paramsList: modelConfig.params,
	        		fileList: [{
					      uid: -1,
					      name: modelConfig.path,
					      status: 'done',
					 }],
					 feed: paramVO.feed,

	        	});
	  	});
	   
	    FetchUtil('/activation/absColumns/' + this.props.params.id,'GET','',
	    	(data) => {
	    		let absColumns =[];
	            let absDS= data.data.columns;
	        	for (let i = 0; i < absDS.length; i++) {
					  absColumns.push(<Option key={absDS[i].value}>{absDS[i].label}</Option>);
				}
				//console.log(absColumns);
				this.setState({
					absColumns: absColumns,
				});
	  	});
	}




  handleClose = (removedTag) => {
    const tags = this.state.tags.filter(tag => tag !== removedTag);
    console.log(tags);
    this.setState({ tags });
  }

  showInput = () => {
    this.setState({ inputVisible: true }, () => this.input.focus());
  }



  // handleInputChange = (e) => {
  //   this.setState({ inputValue: e.target.value });
  // }



  handleInputConfirm = () => {
    const state = this.state;
    const inputValue = state.inputValue;
    let tags = state.tags;
    if (inputValue && tags.indexOf(inputValue) === -1) {
      tags = [...tags, inputValue];
    }
    console.log(tags);
    this.setState({
      tags,
      inputVisible: false,
      inputValue: '',
    });
  }



  handleChange = (e) => {
  //console.log(`Selected: ${value}`);
  //var state = this.state;
  //state['selectedParams'] = trim(value);
  let selectedKeys = e;
  this.setState({selectedKeys});
 }

 handlInputChange=(e)=>{
    var name = e.target.name;
    var value = e.target.value;
    var state = this.state;
    state[name] = trim(value);
    this.setState(state);
}

  uploadHandleChange = (info) => {
    let fileList = info.fileList;
    fileList = fileList.slice(-1);
    this.setState({ fileList });
  }

  handleSubmit = (isValidated, e) => {
    e.preventDefault();
	//console.log(e, isValidated);
	if(!isValidated){
		Modal.error({
		    title: '提交失败',
		    content: '请确认表单内容输入正确',
		  });
	} else{ 
		var param={};
		var confParam = {};
		param.id= this.state.id;
	    param.name= this.state.name;
	    param.type= this.state.type;
	    param.path= this.state.fileList.map(item => item.name).join();
	    param.comment= this.state.comment;
	    param.tag= this.state.tag;
	    param.operation= this.state.operation;
	    param.status= this.state.status;
	    param.modelId = this.state.model.id;
	    confParam.feed = this.state.feed;
	    confParam.expressions= this.state.selectedKeys.map(item=> "abstractions." + item).join();
		param.confParam = confParam;

	    FetchUtil('/modelConfig/','PUT',JSON.stringify(param),
	    	(data) => {
	    		if(data.success){
	    			message.success('修改成功');
	    		}else{
	    			message.error(data.msg);
	    		}
	            this.setState({
	            	visible:false
	            });
	            //this.props.reload();
	        });
	}

  }

  saveInputRef = input => this.input = input

	render() {
		const plugin=this.state.plugin;
		const { tags,path, inputVisible, inputValue,  paramsList} = this.state;
		let isValidated = true;
		console.log("cols==", this.state.selectCols);
		console.log("path==", path);
		const uploadProps = {
			  name: 'file',
			  data: {"key": "machine"},
			  action: '/services/v1/common/upload',
			  headers: {
			    "x-auth-token": localStorage.getItem('x-auth-token'),
			  },
			  onChange: this.uploadHandleChange,
		    
		};
		const formItemLayout = {
            labelCol: { span: 6 },
            wrapperCol: { span: 16 },
        };
        let validate={
        	name:{
        		help:'',
        		status:'success'
        	},
        	tag:{
        		help:'',
        		status:'success'
        	},
			operation:{
        		help:'',
        		status:'success'
        	},
        	feed:{
        		help:'',
        		status:'success'
        	}
        };

        if(!this.state.name){
			validate.name.help='请输入机器学习模型名称';
			validate.name.status='warning';
			isValidated=false;
		}else {
			let reg = /^[\u4e00-\u9fa5 \w]{2,20}$/;
			let name = this.state.name;
			if(!reg.test(name)){
				validate.name.help='按照提示输入正确的名称';
				validate.name.status='error';
				isValidated=false;
			}
		}

		if(!this.state.feed){
			validate.feed.help='请输入参数名称';
			validate.feed.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}$/;
			let feed = this.state.feed;
			if(!reg.test(feed)){
				validate.feed.help='按照提示输入feed名称';
				validate.feed.status='error';
				isValidated=false;
			}
		}


		if(!this.state.tag){
			validate.tag.help='请输入tag名称';
			validate.tag.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}$/;
			let tag = this.state.tag;
			if(!reg.test(tag)){
				validate.tag.help='按照提示输入正确的名称';
				validate.tag.status='error';
				isValidated=false;
			}
		}

		if(!this.state.operation){
			validate.operation.help='请输入opration名称';
			validate.operation.status='warning';
			isValidated=false;
		}else {
			let reg = /^[a-zA-z]\w{1,29}\/?\w{1,29}$/;
			let operation = this.state.operation;
			if(!reg.test(operation)){
				validate.operation.help='按照提示输入正确的名称';
				validate.operation.status='error';
				isValidated=false;
			}
		}		

		return (
				<div className="ant-layout-wrapper">
	                <div className="ant-layout-breadcrumb">
	                    <Breadcrumb>
	                        <Breadcrumb.Item>机器学习配置</Breadcrumb.Item>
	                    </Breadcrumb>
	                </div>
					<div className="ant-layout-container">

	 					<Form  layout="horizontal" onSubmit={this.handleSubmit.bind(this, isValidated)}>
							<FormItem required={true} {...formItemLayout} label="模型名称："  help={validate.name.help} validateStatus={validate.name.status}  >
								<Row>
									<Col span={20}>
										<Input type="text" name="name" value={this.state.name} placeholder="请输入模型名称" onChange={this.handlInputChange}/>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'机器学习模型名称'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>

	                    	<FormItem required={true} {...formItemLayout} label="学习框架："  >
								<Row>
									<Col span={20} >
										<Select value={this.state.type}>
											<Option value="TENSOR_DNN">TENSOR_DNN</Option>
										</Select>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'支持的机器学习框架类型'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>

	                        <FormItem required={true} {...formItemLayout} label="算法参数：" style={{display:"None"}}  >
								<Row>
									<Col span={20}>
								      <div>
								        {tags.map((tag, index) => {
								          const isLongTag = tag.length > 20;
								          const tagElem = (
								            <Tag key={tag} closable={index !== 0} afterClose={() => this.handleClose(tag)}>
								              {isLongTag ? `${tag.slice(0, 20)}...` : tag}
								            </Tag>
								          );
								          return isLongTag ? <Tooltip title={tag} key={tag}>{tagElem}</Tooltip> : tagElem;
								        })}
								        {inputVisible && (
								          <Input
								            ref={this.saveInputRef}
								            type="text"
								            size="small"
								            style={{ width: 78 }}
								            value={inputValue}
								            onChange={this.handleInputChange}
								            onBlur={this.handleInputConfirm}
								            onPressEnter={this.handleInputConfirm}
								          />
								        )}
								        {!inputVisible && <Button size="small" type="dashed" onClick={this.showInput}>+参数</Button>}
								      </div>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'机器学习模型调用时需要的其它参数'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
		                    </FormItem>

							<FormItem required={true} {...formItemLayout} label="Tag："  help={validate.tag.help} validateStatus={validate.tag.status}>
								<Row>
									<Col span={20}>
										<Input type="text" name="tag" value={this.state.tag} placeholder="tag" onChange={this.handlInputChange}/>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'tag'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>
							<FormItem required={true} {...formItemLayout} label="Operation："  help={validate.operation.help} validateStatus={validate.operation.status}>
								<Row>
									<Col span={20}>
										<Input type="text" name="operation" value={this.state.operation} placeholder="operation" onChange={this.handlInputChange}/>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'Operation'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>

							<FormItem required={true} {...formItemLayout} label="模型文件">
								<Row>
									
									<Col span={20}>
												<Upload {...uploadProps} accept={'.zip'} fileList={this.state.fileList}>
												    <Button>
												      <Icon type="upload" /> 点击上传
												    </Button>
											  	</Upload>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'机器学习训练后的文件, 仅支持zip格式'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>

		                    <FormItem required={true} {...formItemLayout} label="描叙信息" style={{display:"on"}} >

	                            <Row>
									<Col span={20}>
										 <Input.TextArea name="comment"  value={this.state.comment}  rows={4} placeholder="模型描叙信息。" onChange={this.handlInputChange}/>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'模型描叙信息。'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
		                    </FormItem>    

		                    <FormItem  required={true} {...formItemLayout} label="模型入参" help={validate.feed.help} validateStatus={validate.feed.status}>
								{
									paramsList.map((item,index) => {
										const selected = item.expressions.replace(/abstractions./g,"").split(",");
										console.log("selected:" ,selected);
										let rows = (<Row key={index} id={item.id}>
														<Col span={4}><Input type="text" name="feed" value={item.feed} placeholder="feed" /></Col> 
														<Col span={15} offset={1}>
															<div>
																 <Select
															          mode="tags"
															          size={'default'}
															          placeholder="Please select"
															          value={selected}
															          style={{ width: '100%' }}
															        >
															          {this.state.absColumns}
								        						</Select>
															</div>
														</Col>
														<Col span={1} offset={1}>
															<EditModelConfParam paramId={item.id} abstractions={this.state.absColumns} />
														</Col> 
													 </Row>);
										return 	rows;
									})
								} 
								{
									paramsList.length==0  ? (
													<Row key={-1}>
														<Col span={4}><Input type="text" name="feed" value={this.state.feed}  placeholder="feed" onChange={this.handlInputChange}/></Col> 
														<Col span={15} offset={1}>
															<div>
																 <Select
															          mode="tags"
															          size={'default'}
															          placeholder="Please select"
															          onChange={this.handleChange}
															          style={{ width: '100%' }}
															        >
															          {this.state.absColumns}
								        						</Select>
															</div>
														</Col> 
													 </Row>
										)

									: ""
								}
		                    </FormItem> 
		                    <FormItem>
		                    	<Row>
		                    		
		                    		<Col span={20} offset={18}>			                     
		                    			<Button type="primary" htmlType="submit">
						           			 更新配置
						         		</Button>
						           </Col>
		                    	</Row>

		                    </FormItem>
                                
	                    </Form>
	                </div>    
				</div>
		);
	}
}