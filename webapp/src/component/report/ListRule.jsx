import React from 'react';
import {Form,Button,Table,Pagination,Input,Select,Modal,DatePicker,Cascader} from 'antd';
import moment from 'moment';

const FormItem=Form.Item;
const Option = Select.Option;
const RangePicker = DatePicker.RangePicker;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';

import './ListEvent.less';

export default class Rule extends React.Component{

    constructor(props){
        super(props);
        
        this.state={
            beginTime:moment().add(-3,'days'),
            endTime:moment(),
            rangeSelect:'-3',

            endOpen:false,

            tData:[],
            loading:true,

            pageNo:1,
            rowCount:0,
            pageSize:30,

            activationName:'',
            ruleId:'',
            risk:[],
            activationNameOne:'' //存储策略中的第一个value值，便于按处理结果查询
        }

        if(this.props.params.modelId){
            this.state.activationName=this.props.params.activationName;
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
        param.modelId=this.props.modelId;
        if(this.state.risk.length != 0){
            if(this.state.activationName!=''){
                param.fieldName='activations.'+this.state.activationName+'.risk';
                param.fieldValue=this.state.risk;
            }else {
                param.fieldName='activations.'+this.state.activationNameOne+'.risk';
                param.fieldValue=this.state.risk;
            }
        }else{
            if(this.state.activationName!=''){
                param.fieldName='hitsDetail.'+this.state.activationName+'.rule_'+this.state.ruleId+'.key';
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

    componentWillReceiveProps(nextProps){
        if(nextProps.modelId!=this.props.modelId){
            this.setState({
                beginTime:moment().add(-3,'days'),
                endTime:moment(),
                rangeSelect:'-3',
                activationName:'',
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
        // if(name=='modelId'){
        //     this.setState({
        //         tData:[]
        //     });
        //     FetchUtil('/abstraction/datacolumns/'+value,'GET','',
        //         (data) => {
        //             this.setState({
        //                 fieldList:data.data.list
        //             });
        //         });
        //     FetchUtil('/activation/rulecolumns/'+value,'GET','',
        //         (data) => {
        //             this.setState({
        //                 activationList:data.data.list
        //             });
        //         });
        //     FetchUtil('/model/'+value,'GET','',
        //         (data) => {
        //             this.setState({
        //                 model:data.data.model
        //             })
        //         }
        //     )
        //     this.setState({
        //         activationName:'',
        //         ruleId:''
        //     })
        // }
        if(name=='activationName'){
            const activation=this.props.activationList.filter(x=>x.value==value)[0];
            if(activation.children&&activation.children.length>0){
                this.setState({
                    ruleId:activation.children[0].type
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
                beginTime:moment().add(value,'months'),
                endTime:moment()
            });
        }else {
            this.setState({
                beginTime:moment().add(value,'days'),
                endTime:moment()
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
        const activations=record.activations;
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

        const columnsActivation=[{
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
        let dataActivation=[];
        for(var Keys in activations){
            activations[Keys].name = Keys;
            if(activations[Keys].risk === 'pass'){
                activations[Keys].risk = '通过';
            }else if(activations[Keys].risk === 'review'){
                activations[Keys].risk = '人工审核';
            }else if(activations[Keys].risk === 'reject'){
                activations[Keys].risk = '拒绝';
            }
            dataActivation.push(activations[Keys]);
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
                        dataSource={dataActivation}
                        columns={columnsActivation}
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
                            return moment(t).format('YYYY-MM-DD HH:mm:ss');
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

            for(var Key in info.preItems){
                if(typeof info.preItems[Key]=='object'){
                    for(var subKey in info.preItems[Key]){
                        data['preItems'+Key+subKey]=info.preItems[Key][subKey];
                    }
                }
                else{
                    data['preItems'+Key]=info.preItems[Key];
                }
            }

            data['hitsDetail']=info.hitsDetail;
            data['activations']=info.activations;

            dataList.push(data);
        });

        const actList=this.props.activationList.filter(x=>x.value==this.state.activationName);
        let ruleList=[];
        if(actList.length!=0){
            ruleList=actList[0].children;
        }

        return (
            <div className="ant-layout-content">
                <div id="header">
                    <Form inline>
                        <FormItem label="选择策略：">
                            <Select dropdownMatchSelectWidth={false} value={this.state.activationName} onChange={this.handleSelect.bind(this,'activationName')} style={{width:100}}>
                                {this.props.activationList.map((info,i)=>{
                                    return <Option key={info.label} value={info.value}>{info.label}</Option>
                                })}
                            </Select>
                        </FormItem>
                        <FormItem label="选择规则：">
                            <Select dropdownMatchSelectWidth={false} value={this.state.ruleId} onChange={this.handleSelect.bind(this,'ruleId')} style={{width:100}}>
                                {ruleList==undefined?'':ruleList.map((info,i)=>{
                                    return <Option key={info.label} value={info.type}>{info.label}</Option>
                                })}
                            </Select>
                        </FormItem>
                        &nbsp;&nbsp;&nbsp;&nbsp;

                        <FormItem label="处理结果：">
                            <Select dropdownMatchSelectWidth={false} value={this.state.risk} onChange={this.handleSelect.bind(this,'risk')} style={{width:100}}>
                                <Option value='pass'>通过</Option>
                                <Option value='review'>人工审核</Option>
                                <Option value='reject'>拒绝</Option>
                            </Select>
                        </FormItem>
                        &nbsp;&nbsp;&nbsp;&nbsp;

                        <FormItem label="起始时间">
                            <Select dropdownMatchSelectWidth={false} showSearch defaultValue='-3' onChange={this.handleChangeDate} style={{width:100,marginRight:10}}>
                                <Option value='-3'>三天内</Option>
                                <Option value='-7'>七天内</Option>
                                <Option value='-1'>一月内</Option>
                            </Select>
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