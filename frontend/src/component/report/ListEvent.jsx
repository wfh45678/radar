import React from 'react';
import {Form,Button,Table,Pagination,Input,Select,Modal,DatePicker,Cascader} from 'antd';
import {withParams, withOutletCtx} from '../utils/routerUtil';
import dayjs from 'dayjs';

const FormItem=Form.Item;
const RangePicker = DatePicker.RangePicker;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

import './ListEvent.less';

import ExportField from './modal/ExportField';

class ListEvent extends React.Component{

	constructor(props){
		super(props);

		this.state={
			fieldName:'',
			fieldValue:'',
            strategyName:'',
            ruleId:'',
            risk:'',
			beginTime:dayjs().add(-3,'days'),
			endTime:dayjs(),
            rangeSelect:'-3',

			tData:[],
			loading:true,

			pageNo:1,
			rowCount:0,
			pageSize:30,

            exportDisabled:true,
            showAdvance:false,
            searchType:''
		}

        if(this.props.params.scenarioId){
            this.state.showAdvance=true;
            this.state.searchType='rule';
            this.state.strategyName=this.props.params.strategyName;
            this.state.ruleId=this.props.params.ruleId+'';
        }
	}

	 // 获取表格数据
    fetchTableData=()=>{
        const pageSize=30;
        let param={};
        let errMsg='';        
        switch(this.state.searchType){
            case '':param.fieldName='';param.fieldValue='';break;
            case 'field':
                if(!this.state.fieldName){
                    errMsg='请选择字段！';
                    break;
                }
                if(!this.state.fieldValue){
                    errMsg='请输入字段值！';
                    break;
                }
                param.fieldName=this.state.fieldName;
                param.fieldValue=this.state.fieldValue;
                break;
            case 'rule':
                if(!this.state.strategyName){
                    errMsg='请选择策略！';
                    break;
                }
                if(!this.state.ruleId){
                    errMsg='请选择规则！';
                    break;
                }
                param.fieldName='hitsDetail.'+this.state.strategyName+'.rule_'+this.state.ruleId+'.key';
                param.fieldValue=this.state.ruleId;
                break;
            case 'risk':
                if(!this.state.strategyName){
                    errMsg='请选择策略！';
                    break;
                }
                if(!this.state.risk){
                    errMsg='请选择评估结果！';
                    break;
                }
                param.fieldName='strategies.'+this.state.strategyName+'.risk';
                param.fieldValue=this.state.risk;
                break;
        }

        if(errMsg){
            Modal.error({
                title:errMsg
            });
            return;
        }

        param.pageNo=this.state.pageNo;
        param.pageSize=pageSize;
        param.scenarioId=this.props.scenarioId;
        param.beginTime=this.state.beginTime.format('YYYY-MM-DD HH:mm:ss');
        param.endTime=this.state.endTime.format('YYYY-MM-DD HH:mm:ss');

        this.setState({loading:true});
        FetchUtil('/event/search','POST',JSON.stringify(param),
            (data) => {              
                this.setState({
                    tData:data.data.page.list,
                    pageNo:data.data.page.pageNum
                });
				if(data.data.page.rowCount > 9990){
					this.setState({
						rowCount:9990
					});
				}else {
					this.setState({
						rowCount:data.data.page.rowCount
					});
				}
            },
            ()=>{
                this.setState({loading:false,exportDisabled:false});
            });
    }

    componentDidMount(){
        this.fetchTableData();
    }


    // 原 componentWillReceiveProps 在 React 17+ 已移除，等价迁移为 componentDidUpdate
    componentDidUpdate(prevProps){
        if(prevProps.scenarioId!=this.props.scenarioId){
            this.setState({
                fieldName:'',
                fieldValue:'',
                strategyName:'',
                ruleId:'',
                risk:'',
                pageNo:1,
                beginTime:dayjs().add(-3,'days'),
                endTime:dayjs(),
                rangeSelect:'-3',
                showAdvance:false,
                searchType:''
            },this.fetchTableData());
        }
    }

    toggleAdvance=()=>{
        this.setState({
            showAdvance:!this.state.showAdvance,
            searchType:'',
            fieldName:'',
            fieldValue:'',
            strategyName:'',
            ruleId:'',
            risk:'',
        });
    }

	handleChange=(e)=>{
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        state['exportDisabled']=true;
        this.setState(state);
    }

    handleSelect=(name,value)=>{
        var state = this.state;
        state[name]=value;
        state['exportDisabled']=true;
        this.setState(state);
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

    handleField=(value)=>{
    	this.setState({
    		fieldName:value.join("."),
            fieldValue:'',
            exportDisabled:true
    	})
    }

	handleSearch=()=>{
		this.fetchTableData();
        if(this.props.location.pathname.indexOf('ruleid')!=-1){
            window.location.href='/#/event';
        }
	}

	selectPage=(page)=>{
		this.setState({
            pageNo:page
        },()=>{this.fetchTableData()});
	}

	// selectedOptions 中可能存在 null（value 路径在 fieldList 中匹配不到：字段树未加载完成或字段已变更），需判空防止白屏
	displayRender = (labels, selectedOptions) => labels.map((label, i) => {
	  const option = selectedOptions && selectedOptions[i];
	  const key = (option ? option.value : label) + i;
	  if (i === labels.length - 1) {
	    return (
	      <span key={key}>
	        {label}
	      </span>
	    );
	  }
	  return <span key={key}>{label} / </span>;
	})

	showModal=(record)=>{
		const hitsDetail=record.hitsDetail;
		const strategies=record.strategies;
		const columns=[{
            title: '序号',
            dataIndex: 'id',
            key:'id',
            width:50,
            render:(t,r,i)=>{
                return i+1;
            }
        },{
        	title: '命中规则',
        	dataIndex: 'desc',
        	key: 'rule'
        },{
        	title: '得分',
        	dataIndex:'value'
        }];
		let data=[];
		for(var Key in hitsDetail){
			for(var subKey in hitsDetail[Key]){
				data.push(hitsDetail[Key][subKey]);
			}
		}
		const columnsStrategy=[{
			title: '序号',
			dataIndex: 'id',
			key:'id',
			width:50,
			render:(t,r,i)=>{
				return i+1;
			}
		},{
			title: '策略名称',
			dataIndex: 'name',
		},{
			title: '得分',
			dataIndex:'score'
		},{
			title: '处理结果',
			dataIndex:'risk'
		}];
		let dataStrategy=[];
		for(var Keys in strategies){
			strategies[Keys].name = Keys;
			if(strategies[Keys].risk === 'pass'){
				strategies[Keys].risk = '通过';
			}else if(strategies[Keys].risk === 'review'){
				strategies[Keys].risk = '人工审核';
			}else if(strategies[Keys].risk === 'reject'){
				strategies[Keys].risk = '拒绝';
			}
			dataStrategy.push(strategies[Keys]);
		}

		Modal.info({
		    title: '风险详情',
		    width:600,
		    content: (
				<div style={{paddingTop:20}}>
					<h3>命中明细</h3>
					<Table
						dataSource={data}
						columns={columns}
						size="middle"
						bordered
						pagination={false}
						loading={this.state.loading}
					/>
					<h3 style={{paddingTop:20}}>策略明细</h3>
					<Table
						dataSource={dataStrategy}
						columns={columnsStrategy}
						size="middle"
						bordered
						pagination={false}
						loading={this.state.loading}
					/>
				</div>
		    )
		});
	}	

	render(){
		let columns = [
        {
            title: '序号',
            dataIndex: 'id',
            key:'id',
            width:50,
            fixed:'left',
            render:(t,r,i)=>{
                return i+1;
            }
        }];


        let getChildren=(valueArr,children)=>{
        	return children.map((info)=>{
        		let va=valueArr.concat(info.value);
        		if(info.children==undefined){
        			let column={
        				title:info.label,
        				dataIndex:va.join(''),
        				key:va.join(''),
        				rowSpan:4-va.length,
        				className:'fixed-table'
        			};
                    if(this.state.model!=null&&this.state.model.referenceDate==info.value){
                        column.render=(t)=>{
                            return dayjs(parseInt(t)).format('YYYY-MM-DD HH:mm:ss');
                        }
                    }

                    return column;
        		}
        		else{        		
        			return {
        				title:info.label,
        				children:getChildren(va,info.children)
        			}
        		}
        	})
		}
		columns=columns.concat(getChildren([],this.props.fieldList));

        let dataList=[];
        this.state.tData.map((info)=>{
        	let data={};

        	for(var Key in info.fields){
        		data['fields'+Key]=info.fields[Key];
        	}

        	// v4.1 ES 文档键：features（老文档兼容读 preItems，列头由变量树驱动）
        	var featureSrc = info.features || info.preItems || {};
        	var featurePrefix = info.features ? 'features' : 'preItems';
        	for(var Key in featureSrc){
        		if(typeof featureSrc[Key]=='object'){
        			for(var subKey in featureSrc[Key]){
        				data[featurePrefix+Key+subKey]=featureSrc[Key][subKey];
        			}
        		}
        		else{
        			data[featurePrefix+Key]=featureSrc[Key];
        		}
        	}

        	data['hitsDetail']=info.hitsDetail;
        	data['strategies']=info.strategies;

        	dataList.push(data);
        });

        const actList=this.props.strategyList.filter(x=>x.value==this.state.strategyName);
        let ruleList=[];
        if(actList.length!=0){
            ruleList=actList[0].children;
        }

		return (
			<div className="ant-layout-content">
                    <div id="header">
                        <Form layout="inline">
                            <FormItem label="起始时间">
								<Select dropdownMatchSelectWidth={false} showSearch placeholder="选择时间段" value={this.state.rangeSelect} onChange={this.handleChangeDate} style={{width:100,marginRight:10}}
									options={[{label:'三天内', value:'-3'},{label:'七天内', value:'-7'},{label:'一月内', value:'-1'}]}
								/>
                            	<RangePicker value={[this.state.beginTime,this.state.endTime]} showTime format="YYYY/MM/DD HH:mm:ss" onChange={this.handleCalendar} />
                            </FormItem>
                                                            
                            <Button type="primary" onClick={this.handleSearch}>查询</Button>
                            {' '}
                            <ExportField eventFieldList={this.props.eventFieldList} disabled={this.state.exportDisabled}/>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <a onClick={this.toggleAdvance}>高级搜索&gt;&gt;</a>
                        </Form>
                        {this.state.showAdvance?
                        <Form layout="inline" style={{marginTop:5}}>
                            <FormItem label="搜索种类">
                                <Select dropdownMatchSelectWidth={false} placeholder="选择时间段" value={this.state.searchType} onChange={this.handleSelect.bind(this,'searchType')} style={{marginRight:10}}
                                    options={[{label:'请选择搜索种类', value:''},{label:'按字段搜索', value:'field'},{label:'按规则搜索', value:'rule'},{label:'按评估结果搜索', value:'risk'}]}
                                />
                            </FormItem>
                            {this.state.searchType==''?''
                            :this.state.searchType=='field'?
                            <span>
                                <FormItem label="选择字段：">
                                    <Cascader
                                        options={this.props.fieldList}
                                        value={this.state.fieldName.split('.')}
                                        displayRender={this.displayRender}
                                        onChange={this.handleField}
                                        allowClear
                                      />
                                </FormItem>
                                <FormItem label="字段值：">
                                    <Input value={this.state.fieldValue} name="fieldValue" id="blue" onChange={this.handleChange}/>
                                </FormItem>
                            </span>
                            :this.state.searchType=='rule'?
                            <span>
                                <FormItem label="选择策略：">
                                    <Select dropdownMatchSelectWidth={false} value={this.state.strategyName} onChange={this.handleSelect.bind(this,'strategyName')} style={{width:100}}
                                        options={this.props.strategyList.map((info)=>({label:info.label, value:info.value}))}
                                    />
                                </FormItem>
                                <FormItem label="选择规则：">
                                    <Select dropdownMatchSelectWidth={false} value={this.state.ruleId} onChange={this.handleSelect.bind(this,'ruleId')} style={{width:100}}
                                        options={ruleList==undefined?[]:ruleList.map((info)=>({label:info.label, value:info.type}))}
                                    />
                                </FormItem>
                            </span>
                            :this.state.searchType=='risk'?
                            <span>
                                <FormItem label="选择策略：">
                                    <Select dropdownMatchSelectWidth={false} value={this.state.strategyName} onChange={this.handleSelect.bind(this,'strategyName')} style={{width:100}}
                                        options={this.props.strategyList.map((info)=>({label:info.label, value:info.value}))}
                                    />
                                </FormItem>
                                <FormItem label="处理结果：">
                                    <Select dropdownMatchSelectWidth={false} value={this.state.risk} onChange={this.handleSelect.bind(this,'risk')} style={{width:100}}
                                        options={[{label:'通过', value:'pass'},{label:'人工审核', value:'review'},{label:'拒绝', value:'reject'}]}
                                    />
                                </FormItem>
                            </span>
                            :''}
                        </Form>
                        :''
                        }
                    </div>

                    <div id="table" className="fixed-table">
                        <Table 
                            dataSource={dataList} 
                            columns={columns} 
                            size="middle"
                            bordered
                            onRowClick={this.showModal}
                            pagination={false}
                            loading={this.state.loading}
                            scroll={{ x: true }}
                            rowKey={record => record.id}
                        />
                        <div style={{width:"100%",marginTop:16,height:40}}>
                            <div style={{float:"right"}}>
                            <Pagination onChange={this.selectPage} defaultCurrent={this.state.pageNo} defaultPageSize={this.state.pageSize} total={this.state.rowCount} />
                            </div>
                        </div>                 
                    </div>
                </div>);
	}
}

export default withParams(withOutletCtx(ListEvent));
