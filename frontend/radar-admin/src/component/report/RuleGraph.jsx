import React from 'react';
import {Form,Button,Table,Pagination,Input,Select,Modal,DatePicker,Cascader} from 'antd';
//import moment from 'moment';
import { Link } from 'react-router';
import echarts from 'echarts';
import moment from 'moment';


const FormItem=Form.Item;
const Option = Select.Option;
const RangePicker = DatePicker.RangePicker;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

export default class RuleGraph extends React.Component{

	
	constructor(props){
		super(props);

		this.state={
			modelId:'',
			tData:[],

			loading:true,

			pageNo:1,
			rowCount:0,

			ruleLabelList:[],
			countList:[],

			modelList:[],
			fieldList:[],
			beginTime:moment().add(-1,'months'),
			endTime:moment(),
            rangeSelect:'-1'
		}

	}
	

	 // 获取表格数据
    fetchTableData=(value)=>{
        this.setState({loading:true});
 		let param={};
        param.beginTime=this.state.beginTime.format('YYYY-MM-DD HH:mm:ss');
        param.endTime=this.state.endTime.format('YYYY-MM-DD HH:mm:ss');

        FetchUtil('/rule/hitsSort/'+value,'POST',JSON.stringify(param),
            (data) => {
				this.setState({
					countList:data.data.hits.map((hit)=>{return hit.count}),
					ruleLabelList:data.data.hits.map((hit)=>{return hit.ruleLable}),
					tData:data.data.hits,
					loading:false
				});
				
            }
		);
    }

    componentDidMount=()=>{
    	this.fetchTableData(this.props.modelId);
    }

    componentWillReceiveProps(nextProps){
        if(nextProps.modelId!=this.props.modelId){
             this.fetchTableData(nextProps.modelId);
        }
    }

    handleSelect=(name,value)=>{
    	var state = this.state;
        state[name] = trim(value);
        this.setState(state);
 		let param={};
        param.beginTime=this.state.beginTime.format('YYYY-MM-DD HH:mm:ss');
        param.endTime=this.state.endTime.format('YYYY-MM-DD HH:mm:ss');
		FetchUtil('/rule/hitsSort/'+value,'POST',JSON.stringify(param),
			(data) => {
				console.log(data.data.hits);
				this.setState({
					countList:data.data.hits.map((hit)=>{return hit.count}),
					ruleLabelList:data.data.hits.map((hit)=>{return hit.ruleLable}),
					tData:data.data.hits
				});

				this.setState({loading:false});
			}
		);
    }
    handleSearch = () => {
        this.setState({loading:true});
 		this.fetchTableData(this.props.modelId);
    }
    handleCalendar=(dates,dateStrings)=>{
    	this.setState({
    		beginTime:dates[0],
    		endTime:dates[1],
            rangeSelect:'',
            exportDisabled:true
    	});
    }

	handleChangeDate=(value)=>{
		//console.log(value);
		if(value === '-1'){
			this.setState({
				beginTime:moment().add(value,'months'),
				endTime:moment()
			});
		}else {
			this.setState({
				beginTime:moment().add(value,'days'),
				endTime:moment()
			});
		}
		this.setState({
            rangeSelect:value,
            exportDisabled:true
        });
	}

	render(){

		/*定义表格列*/
		const columns = [
			{
				title: '字段名',
				dataIndex: 'ruleLable'
			},
			{
				title: '命中数',
				dataIndex: 'count'
			},
			{
				title: '操作',
				dataIndex: 'handle',
				render:(t,r)=>{
					return <Link to={"/ruleid/"+this.props.modelId+"/"+r.id+"/"+r.activationName} target="_blank">查看明细</Link>;
				}

			}
		];

		return (
			<div className="ant-layout-content">
				<div id="serachCond">
                        <Form inline="true">
                            <FormItem label="起始时间">
								<Select dropdownMatchSelectWidth={false} showSearch placeholder="选择时间段" value={this.state.rangeSelect} onChange={this.handleChangeDate} style={{width:100,marginRight:10}}>
									<Option value='-3'>三天内</Option>
									<Option value='-7'>七天内</Option>
									<Option value='-1'>一月内</Option>
								</Select>
                            	<RangePicker value={[this.state.beginTime,this.state.endTime]} showTime format="YYYY/MM/DD HH:mm:ss" onChange={this.handleCalendar} />
                            </FormItem>               
                            <Button type="primary" onClick={this.handleSearch}>查询</Button>
                        </Form>
				</div>
            	<div id="echartsMain" style={{width:900,height:400}}></div>
            	<div id="table">
					<Table
						dataSource={this.state.tData}
						columns={columns}
						size="middle"
						bordered
						pagination={false}
						loading={this.state.loading}
					/>
				</div>
           </div>
		);
	}

	componentDidUpdate=()=>{
		// 基于准备好的dom元素，初始化echarts实例
		let myChart = echarts.init(document.getElementById('echartsMain'));
		// 绘制图表
		myChart.setOption({
			tooltip : {
				trigger: 'axis',
				axisPointer : {            // 坐标轴指示器，坐标轴触发有效
					type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
				}
			},
			legend: {
				data: ['命中数']
			},
			grid: {
				left: '3%',
				right: '4%',
				bottom: '3%',
				containLabel: true
			},
			xAxis:  {
				type: 'value'
			},
			yAxis: {
				type: 'category',
				data: this.state.ruleLabelList
			},
			series: [
				{
					name: '命中数',
					type: 'bar',
					stack: '总量',
					label: {
						normal: {
							show: true,
							position: 'right'
						}
					},
					data: this.state.countList
				}
			]
		});
	}
}