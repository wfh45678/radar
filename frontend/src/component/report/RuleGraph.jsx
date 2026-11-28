import React from 'react';
import {Form,Button,Table,Pagination,Input,Select,Modal,DatePicker,Cascader} from 'antd';
import { Link } from 'react-router';
import * as echarts from 'echarts';
import dayjs from 'dayjs';


const FormItem=Form.Item;
const RangePicker = DatePicker.RangePicker;

import {FetchUtil} from '../utils/fetchUtil';
import {withOutletCtx} from '../utils/routerUtil';
import {trim} from '../utils/validateUtil';

class RuleGraph extends React.Component{

	
	constructor(props){
		super(props);

		this.state={
			scenarioId:'',
			tData:[],

			loading:true,

			pageNo:1,
			rowCount:0,

			ruleLabelList:[],
			countList:[],

			scenarioList:[],
			fieldList:[],
			beginTime:dayjs().add(-1,'months'),
			endTime:dayjs(),
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
    	this.fetchTableData(this.props.scenarioId);
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
 		this.fetchTableData(this.props.scenarioId);
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
				beginTime:dayjs().add(value,'months'),
				endTime:dayjs()
			});
		}else {
			this.setState({
				beginTime:dayjs().add(value,'days'),
				endTime:dayjs()
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
					return <Link to={"/ruleid/"+this.props.scenarioId+"/"+r.id+"/"+r.strategyName} target="_blank">查看明细</Link>;
				}

			}
		];

		return (
			<div className="ant-layout-content">
				<div id="serachCond">
                        <Form layout="inline">
                            <FormItem label="起始时间">
								<Select dropdownMatchSelectWidth={false} showSearch placeholder="选择时间段" value={this.state.rangeSelect} onChange={this.handleChangeDate} style={{width:100,marginRight:10}}
						options={[{label:'三天内', value:'-3'},{label:'七天内', value:'-7'},{label:'一月内', value:'-1'}]}
					/>
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
						rowKey={record => record.id}
					/>
				</div>
           </div>
		);
	}

	// componentDidUpdate 合并实现：scenarioId 变更重取数据 + echarts 重绘（见上方方法声明，
	// 此处箭头字段会覆盖原型方法，因此两段逻辑都放在这里）
	componentDidUpdate=(prevProps)=>{
		if(prevProps && prevProps.scenarioId!=this.props.scenarioId){
			this.fetchTableData(this.props.scenarioId);
		}
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
						show: true,
						position: 'right'
					},
					data: this.state.countList
				}
			]
		});
	}
}

export default withOutletCtx(RuleGraph);
