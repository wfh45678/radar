import React from 'react';
import {Breadcrumb, Menu, Icon, Form, Upload, Input, Select, Card, Row, Col,Button, Tooltip, Tag} from 'antd';

const FormItem=Form.Item;
const Option = Select.Option;

import {FetchUtil} from '../utils/fetchUtil';

export default class LearningConfig extends React.Component{
	constructor(props){
		super(props);

		this.state={
			visible:false,

			destField:'',
			label:'',
			sourceField:'',
			sourceLabel:'',
			plugin:'Tensorflow',
			status:1,
			args:'',
			reqType:'GET',
			configJson:'',
			tags: ['x=1', 'y=2', 'z=3'],
			tags2: ['指标1', '指标2', '指标3'],
		    inputVisible: false,
		    inputVisible2: false,
		    inputValue: '',
		    inputValue2: '',
		}
       
	}

  handleClose = (removedTag) => {
    const tags = this.state.tags.filter(tag => tag !== removedTag);
    console.log(tags);
    this.setState({ tags });
  }

  showInput = () => {
    this.setState({ inputVisible: true }, () => this.input.focus());
  }

  showInput2 = () => {
    this.setState({ inputVisible2: true }, () => this.input.focus());
  }

  handleInputChange = (e) => {
    this.setState({ inputValue: e.target.value });
  }

  handleInputChange2 = (e) => {
    this.setState({ inputValue2: e.target.value });
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

  handleInputConfirm2 = () => {
    const state = this.state;
    const inputValue2 = state.inputValue2;
    let tags2 = state.tags2;
    if (inputValue2 && tags2.indexOf(inputValue2) === -1) {
      tags2 = [...tags2, inputValue2];
    }
    console.log(tags2);
    this.setState({
      tags2,
      inputVisible2: false,
      inputValue2: '',
    });
  }

  saveInputRef = input => this.input = input

	render() {
		const uploadProps = {
			  name: 'file',
			  data: {"dataListId": ""},
			  action: '/services/v1/common/upload',
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
		const plugin=this.state.plugin;
		const { tags,tags2, inputVisible, inputValue, inputVisible2, inputValue2 } = this.state;
		return (
				<div className="ant-layout-wrapper">
	                <div className="ant-layout-breadcrumb">
	                    <Breadcrumb>
	                        <Breadcrumb.Item>首页</Breadcrumb.Item>
	                        <Breadcrumb.Item>机器学习配置</Breadcrumb.Item>
	                    </Breadcrumb>
	                </div>
					<div className="ant-layout-container">

	 					<Form horizontal form={this.props.form}>
							<FormItem required={true} {...formItemLayout} label="模型名称："  >
								<Row>
									<Col span={20}>
										<Input type="text" name="label"  placeholder="请输入模型名称"/>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'机器学习模型名称'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>

	                    	<FormItem required={true} {...formItemLayout} label="学习框架：" help={validate.plugin.help} validateStatus={validate.plugin.status}>
								<Row>
									<Col span={20}>
										<Select >
											<Option value="Tensorflow">Tensorflow</Option>
											<Option value="Caffe">Caffe</Option>
											<Option value="Neuroph">Neuroph</Option>
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
								        {tags2.map((tag, index) => {
								          const isLongTag = tag.length > 20;
								          const tagElem = (
								            <Tag key={tag} closable={index !== 0} afterClose={() => this.handleClose(tag)}>
								              {isLongTag ? `${tag.slice(0, 20)}...` : tag}
								            </Tag>
								          );
								          return isLongTag ? <Tooltip title={tag} key={tag}>{tagElem}</Tooltip> : tagElem;
								        })}
								        {inputVisible2 && (
								          <Input
								            ref={this.saveInputRef}
								            type="text"
								            size="small"
								            style={{ width: 78 }}
								            value={inputValue2}
								            onChange={this.handleInputChange2}
								            onBlur={this.handleInputConfirm2}
								            onPressEnter={this.handleInputConfirm2}
								          />
								        )}
								        {!inputVisible2 && <Button size="small" type="dashed" onClick={this.showInput2}>+特征</Button>}
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
												<Upload accept={'.zip'}>
												    <Button>
												      <Icon type="upload" /> 点击上传
												    </Button>
											  	</Upload>
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'机器学习训练后的文件'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
	                        </FormItem>

		                    <FormItem required={true} {...formItemLayout} label="描叙信息" style={{display:"on"}} help={validate.args.help} validateStatus={validate.args.status}>

	                            <Row>
									<Col span={20}>
										 <Input.TextArea name="configJson"   rows={4} placeholder="模型描叙信息。" />
	                            	</Col>
	                            	<Col span={2} offset={1}>
			                            <Tooltip placement="right" title={'模型描叙信息。'}>
			                            	<Icon style={{fontSize:16}} type="question-circle-o" />
			                            </Tooltip>
	                            	</Col>
	                            </Row>
		                    </FormItem>                                      
	                    </Form>
	                </div>    
				</div>
		);
	}
}