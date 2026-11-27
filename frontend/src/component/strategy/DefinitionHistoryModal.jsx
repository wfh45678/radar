import React from 'react';
import {Modal, Table, Tag, Typography} from 'antd';
import {FetchUtil} from '../utils/fetchUtil';
import {strategyTypeOf} from './strategyTypes';

/**
 * 策略定义变更历史弹窗（v4.0）：展示 engine_strategy_history 快照（评分卡/决策树）。
 * 规则集的历史走原有规则修改历史页面。
 */
export default class DefinitionHistoryModal extends React.Component{

    constructor(props){
        super(props);
        this.state={
            list:[],
            loading:false
        }
    }

    componentDidMount(){
        this.fetchHistory();
    }

    fetchHistory=()=>{
        this.setState({loading:true});
        FetchUtil('/strategy/history/'+this.props.strategyId,'GET','',
            (data) => {
                this.setState({
                    list:data.data.list||[],
                    loading:false
                });
            },()=>{
                this.setState({loading:false});
            });
    }

    render(){
        const columns=[
            {
                title:'保存时间', dataIndex:'updateTime', width:170,
                render:(t)=>t?(new Date(t).toLocaleString()):''
            },
            {
                title:'操作人', dataIndex:'merchantCode', width:120,
                render:(t)=>t||'-'
            },
            {
                title:'类型', dataIndex:'type', width:110,
                render:(t)=>{const x=strategyTypeOf(t);return <Tag color={x.color}>{x.label}</Tag>;}
            }
        ];
        return (
            <Modal title="定义变更历史（最近 50 次）" open={this.props.open} onCancel={this.props.onClose}
                footer={null} width={860}>
                <Table size="small" rowKey="id" columns={columns} loading={this.state.loading}
                    dataSource={this.state.list} pagination={false}
                    expandable={{
                        expandedRowRender: (record) => (
                            <Typography.Paragraph code copyable style={{whiteSpace:'pre-wrap',maxHeight:260,overflow:'auto',margin:0}}>
                                {record.definition?JSON.stringify(record.definition,null,2):'（空）'}
                            </Typography.Paragraph>
                        )
                    }}/>
            </Modal>
        );
    }
}
