import React from 'react';
import {Form,Button,Table,Pagination,Input,Select,Modal,DatePicker,Cascader} from 'antd';
import {withParams, withOutletCtx} from '../utils/routerUtil';
import dayjs from 'dayjs';

const FormItem=Form.Item;
const RangePicker = DatePicker.RangePicker;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

import './ListEvent.less';

class Rule extends React.Component{

    constructor(props){
        super(props);
        
        this.state={
            beginTime:dayjs().add(-3,'days'),
            endTime:dayjs(),
            rangeSelect:'-3',

            endOpen:false,

            tData:[],
            loading:true,

            pageNo:1,
            rowCount:0,
            pageSize:30,

            strategyName:'',
            ruleId:'',
            risk:[],
            strategyNameOne:'' //存储策略中的第一个value值，便于按处理结果查询
        }

        if(this.props.params.scenarioId){
            this.state.strategyName=this.props.params.strategyName;
            this.state.ruleId=this.props.params.ruleId+'';
        }
    }

    // 获取表格数据
    fetchTableData=()=>{
        const pageSize=30;
        this.setState({loading:true});

        var param={};
        param.pageNo=this.state.pageNo;
        param.pageSize=pageSize;
        param.scenarioId=this.props.scenarioId;
        if(this.state.risk.length != 0){
            if(this.state.strategyName!=''){
                param.fieldName='strategies.'+this.state.strategyName+'.risk';
                param.fieldValue=this.state.risk;
            }else {
                param.fieldName='strategies.'+this.state.strategyNameOne+'.risk';
                param.fieldValue=this.state.risk;
            }
        }else{
            if(this.state.strategyName!=''){
                param.fieldName='hitsDetail.'+this.state.strategyName+'.rule_'+this.state.ruleId+'.key';
                param.fieldValue=this.state.ruleId;
            }
        }

        param.beginTime=this.state.beginTime.format('YYYY-MM-DD HH:mm:ss');
        param.endTime=this.state.endTime.format('YYYY-MM-DD HH:mm:ss');

        FetchUtil('/event/search','POST',JSON.stringify(param),
            (data) => {
                this.setState({
                    tData:data.data.page.list.map((info)=>{return JSON.parse(info)}),
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
                this.setState({loading:false});
            });
    }

    componentDidMount=()=>{
        this.fetchTableData();
    }

    // 原 componentWillReceiveProps 在 React 17+ 已移除，等价迁移为 componentDidUpdate
    componentDidUpdate(prevProps){
        if(prevProps.scenarioId!=this.props.scenarioId){
            this.setState({
                beginTime:dayjs().add(-3,'days'),
                endTime:dayjs(),
                rangeSelect:'-3',
                strategyName:'',
                ruleId:'',
            },this.fetchTableData());
        }
    }

    handleChange=(e)=>{
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

    handleSelect=(name,value)=>{
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
        // if(name=='scenarioId'){
        //     this.setState({
        //         tData:[]
        //     });
        //     FetchUtil('/abstraction/datacolumns/'+value,'GET','',
        //         (data) => {
        //             this.setState({
        //                 fieldList:data.data.list
        //             });
        //         });
        //     FetchUtil('/strategy/rulecolumns/'+value,'GET','',
        //         (data) => {
        //             this.setState({
        //                 strategyList:data.data.list
        //             });
        //         });
        //     FetchUtil('/scenario/'+value,'GET','',
        //         (data) => {
        //             this.setState({
        //                 model:data.data.model
        //             })
        //         }
        //     )
        //     this.setState({
        //         strategyName:'',
        //         ruleId:''
        //     })
        // }
        if(name=='strategyName'){
            const strategy=this.props.strategyList.filter(x=>x.value==value)[0];
            if(strategy.children&&strategy.children.length>0){
                this.setState({
                    ruleId:strategy.children[0].type
                });
            }
        }
    }

    handleCalendar=(dates,dateStrings)=>{
        this.setState({
            beginTime:dates[0],
            endTime:dates[1]
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

    }

    handleSearch=()=>{
        this.fetchTableData();
    }

    selectPage=(page)=>{
        this.setState({
            pageNo:page
        },()=>{this.fetchTableData()});
    }

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
                            return dayjs(t).format('YYYY-MM-DD HH:mm:ss');
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
                        &nbsp;&nbsp;&nbsp;&nbsp;

                        <FormItem label="处理结果：">
                            <Select dropdownMatchSelectWidth={false} value={this.state.risk} onChange={this.handleSelect.bind(this,'risk')} style={{width:100}}
                                options={[{label:'通过', value:'pass'},{label:'人工审核', value:'review'},{label:'拒绝', value:'reject'}]}
                            />
                        </FormItem>
                        &nbsp;&nbsp;&nbsp;&nbsp;

                        <FormItem label="起始时间">
                            <Select dropdownMatchSelectWidth={false} showSearch defaultValue='-3' onChange={this.handleChangeDate} style={{width:100,marginRight:10}}
                                options={[{label:'三天内', value:'-3'},{label:'七天内', value:'-7'},{label:'一月内', value:'-1'}]}
                            />
                            <RangePicker value={[this.state.beginTime,this.state.endTime]} showTime format="YYYY/MM/DD HH:mm:ss" onChange={this.handleCalendar} />
                        </FormItem>

                        <Button type="primary" onClick={this.handleSearch}>查询</Button>
                        {' '}
                    </Form>
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

export default withParams(withOutletCtx(Rule));
