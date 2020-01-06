import React from 'react';
import {Breadcrumb, Menu, Icon, Form, Upload, Input, Select, Card, Row, Col,Button, Tooltip, Tag, message} from 'antd';

const FormItem=Form.Item;
const Option = Select.Option;

import {FetchUtil} from '../utils/fetchUtil';

export default class ModelConfig extends React.Component{
	constructor(props){
		super(props);

		this.state={
			visible:false,

			plugin:'TENSOR_DNN',
			status:1,
			args:'',
			tags: ['argx=1', 'argy=2', 'argz=3'],
		    inputVisible: false,
		    inputValue: '',
		    pageNo:1,
            rowCount:0,
            model: null,
            modelConfig: null, 
            name: "",
            type: "",
            path: "",
            comment:"",
            absColumns: [],
            selectCols: [],
            fileList:[],
            paramsList:[],
		}

	    FetchUtil('/modelConfig/list/' + this.props.params.id,'GET','',
	    	(data) => {
	            const modelConfig=data.data.modelConfig;
	            let paramVO = modelConfig.params[0];
	            let selectCols = [];
	            selectCols = paramVO.expressions.replace(/abstractions./g,"").split(",");
	        	this.setState({
	        		name: modelConfig.name,
	        		type: modelConfig.type,
	        		path: modelConfig.path,
	        		comment: modelConfig.comment,
	        		selectCols: selectCols,
	        		paramsList: modelConfig.params,
	        		fileList: [{
					      uid: -1,
					      name: modelConfig.path,
					      status: 'done',
					 }],

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



  handleInputChange = (e) => {
    this.setState({ inputValue: e.target.value });
  }



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



  handleChange(value) {
  //console.log(`Selected: ${value}`);
 }

  uploadHandleChange = (info) => {
    let fileList = info.fileList;
    fileList = fileList.slice(-1);
    this.setState({ fileList });
  }

  saveInputRef = input => this.input = input

	render() {
		const plugin=this.state.plugin;
		const { tags,path, inputVisible, inputValue, paramsList} = this.state;
		console.log("cols==", this.state.selectCols);
		console.log("path==", path);
		const uploadProps = {
			  name: 'file',
			  data: {"dataListId": ""},
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
        	plugin:{
        		help:'',
        		status:'success'
        	},
        	label:{
        		help:'',
        		status:'success'
        	},
			sourceField:{
        		help:'',
        		status:'success'
        	},
        	args:{
        		help:'',
        		status:'success'
        	}
        };

		return (
				<div className="ant-layout-wrapper">
	                <div className="ant-layout-breadcrumb">
	                    <Breadcrumb>
	                        <Breadcrumb.Item>机器学习配置</Breadcrumb.Item>
	                    </Breadcrumb>
	                </div>
					<div className="ant-layout-container">

	 					<Form horizontal form={this.props.form}>
							<FormItem required={true} {...formItemLayout} label="模型名称："  >
								<Row>
									<Col span={20}>
										<Input type="text" name="name" value={this.state.name} placeholder="请输入模型名称"/>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'机器学习模型名称'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>

	                    	<FormItem required={true} {...formItemLayout} label="学习框架：" help={validate.plugin.help} >
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

	                        <FormItem required={true} {...formItemLayout} label="算法参数：" style={{display:"on"}} help={validate.sourceField.help} validateStatus={validate.sourceField.status}>
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

	                        <FormItem required={true} {...formItemLayout} label="特征指标：" style={{display:"on"}} help={validate.sourceField.help} validateStatus={validate.sourceField.status}>
								<Row>
									<Col span={20}>
								      <div>
								        <Select
								          mode="tags"
								          size={'default'}
								          placeholder="Please select"
								          value={this.state.selectCols}
								          onChange={this.handleChange}
								          style={{ width: '100%' }}
								        >
								          {this.state.absColumns}
								        </Select>
								      </div>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'模型需要的特征指标描叙，模型计算依赖的数据'}>
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

		                    <FormItem required={true} {...formItemLayout} label="描叙信息" style={{display:"on"}} help={validate.args.help} validateStatus={validate.args.status}>

	                            <Row>
									<Col span={20}>
										 <Input.TextArea name="comment"  value={this.state.comment}  rows={4} placeholder="模型描叙信息。" />
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'模型描叙信息。'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
		                    </FormItem>    

		                    <FormItem {...formItemLayout} label="模型入参">
								{
									paramsList.map(item => {
										const selected = item.expressions.replace(/abstractions./g,"").split(",");
										const rows = (<Row>
														<Col span={4}><Input type="text" name="feed" value={item.feed} placeholder="feed"/></Col> 
														<Col span={15} offset={1}>
															<div>
																 <Select
															          mode="tags"
															          size={'default'}
															          placeholder="Please select"
															          value={selected}
															          onChange={this.handleChange}
															          style={{ width: '100%' }}
															        >
															          {this.state.absColumns}
								        						</Select>
															</div>
														</Col> 
													 </Row>);
										return 	rows;
									})
								}
		                    </FormItem>                                  
	                    </Form>
	                </div>    
				</div>
		);
	}
}