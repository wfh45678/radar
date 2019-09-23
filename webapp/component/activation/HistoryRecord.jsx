import React from 'react';

import {Form,Input,InputNumber,Breadcrumb,Row,Col,Icon,Card,Select,Button,Cascader,Tooltip,message,Modal} from 'antd';

const FormItem = Form.Item;
const Option = Select.Option;

import ComplexCondition from '../abstraction/ComplexCondition';

export default class HistoryRecord extends React.Component{

    constructor(props){
        super(props);

        this.state={
            height:40
        }
    }

    slideDown=()=>{
        if(this.state.height=="auto"){
            return;
        }
        let height = this.refs.content1.offsetHeight+this.refs.content2.offsetHeight+40;
        if(this.state.height<height){
            this.setState({
                height:this.state.height+15
            },()=>{
                setTimeout(this.slideDown,1);
            })
        }else{
            this.setState({
                height:"auto"
            })
            this.refs.content.className = 'up'
        }
    }

    slideUp=()=>{
        let height = this.refs.content1.offsetHeight+this.refs.content2.offsetHeight+40;
        if(this.state.height=="auto"){
            this.state.height=height;
        }
        if(this.state.height>55){
            this.setState({
                height:this.state.height-15
            },()=>{
                setTimeout(this.slideUp,1);
            })
        }
        else{
            this.setState({
                height:40
            })
            this.refs.content.className = 'down'
        }
    }

    handleClick=()=>{
        if(this.refs.content.className === 'down'){
            this.slideDown();
        }
        if(this.refs.content.className === 'up'){
            this.slideUp();
        }
    }

    render(){
        const formItemLayout = {
            labelCol: { span: 4 },
            wrapperCol: { span: 18 },
        };

        let abstractionList=[];
        if(this.props.fieldList.length>0){
            abstractionList=this.props.fieldList.filter(x=>x.value=='abstractions')[0].children;
        }

        const ruleHistory=this.props.ruleHistory;

        return (
            <div ref="content" className="down" style={{border:'1px solid #d9d9d9',borderRadius:'5px',padding:'10px',height:this.state.height,overflow:'hidden'}}>
                <div style={{padding:'0 0 20px 20px',cursor:'pointer'}} onClick={this.handleClick}>用户{ruleHistory.merchantCode}修改</div>
                <div ref="content1" style={{width:750}}>
                    <Form horizontal form={this.props.form}>
                        <FormItem required={true} {...formItemLayout} label="显示名称：">
                            <Row>
                                <Col span={20}>
                                    <Input type="text" name="label" value={ruleHistory.label} readOnly/>
                                </Col>
                            </Row>
                        </FormItem>

                        <FormItem required={true} {...formItemLayout} label="命中初始得分：">
                            <Row>
                                <Col span={4}>
                                    <InputNumber name="initScore" value={ruleHistory.initScore} readOnly/>
                                </Col>
                            </Row>
                        </FormItem>

                        <FormItem required={true} {...formItemLayout} label="命中基数：">
                            <Row>
                                <Col span={4}>
                                    <InputNumber name="baseNum" value={ruleHistory.baseNum} readOnly/>
                                </Col>
                            </Row>
                        </FormItem>

                        <FormItem {...formItemLayout} label="操作符：">
                            <Select value={ruleHistory.operator} readOnly>
                                <Option value="NONE">无</Option>
                                <Option value="ADD">加</Option>
                                <Option value="DEC">减</Option>
                                <Option value="MUL">乘</Option>
                                <Option value="DIV">除</Option>
                            </Select>
                        </FormItem>

                        <FormItem {...formItemLayout} label="指标字段：">
                            <Select value={ruleHistory.abstractionName} readOnly>
                                {abstractionList==undefined?null:abstractionList.map((x,index)=><Option key={x.value+index} value={x.value}>{x.label}</Option>)}
                            </Select>
                        </FormItem>

                        <FormItem required={true} {...formItemLayout} label="比率：">
                            <Row>
                                <Col span={4}>
                                    <InputNumber name="rate" value={ruleHistory.rate} readOnly/>
                                </Col>
                            </Row>
                        </FormItem>
                    </Form>
                </div>
                <div ref="content2">
                    <div>
                        <Tooltip title="添加过滤条件" onClick={this.handleAddCondition}><span className="addRule"><Icon type="plus" />&nbsp;添加过滤条件</span></Tooltip>
                    </div>
                    <ComplexCondition readOnly fieldList={this.props.fieldList} dataList={this.props.dataList} condition={ruleHistory.ruleDefinition==undefined?null:JSON.parse(ruleHistory.ruleDefinition)} changeParentCondition={()=>{}} index={0}/>
                </div>
            </div>
        );
    }
}