import React from 'react';
import {Form, Row, Col, Input, Button, Table, Tooltip, Popconfirm, message, Switch, Tag} from 'antd';
import {withParams} from '../utils/routerUtil';
import { Link } from 'react-router';
const FormItem = Form.Item;

import {FetchUtil} from '../utils/fetchUtil';
import {trim} from '../utils/validateUtil';
import {featureTypeOf, FEATURE_STAGES} from './featureTypes';

import FeatureModal from './modal/FeatureModal';

/**
 * 特征工程统一列表页（v4.1）：ENRICH/AGGREGATE/DERIVED/RATIO 四类特征的
 * 新建（类型选择）/编辑（按类型弹窗）/启停/排序/删除。
 */
class FeatureList extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            label: '',
            type: '',
            tData: [],
            loading: true,
            model: null
        };

        FetchUtil('/scenario/' + this.props.params.id, 'GET', '',
            (data) => {
                this.setState({model: data.data.model});
            });
    }

    fetchTableData = () => {
        this.setState({loading: true});
        var param = {};
        param.pageNo = 1;
        param.pageSize = 1000;
        param.scenarioId = this.props.params.id;
        param.label = this.state.label;
        FetchUtil('/feature', 'POST', JSON.stringify(param),
            (data) => {
                this.setState({
                    loading: false,
                    tData: data.data.page.list
                });
            });
    }

    componentDidMount() {
        this.fetchTableData();
    }

    handleChange = (e) => {
        var name = e.target.name;
        var value = e.target.value;
        var state = this.state;
        state[name] = trim(value);
        this.setState(state);
    }

    deleteFeature = (id) => {
        FetchUtil('/feature', 'DELETE', '[' + id + ']',
            (data) => {
                message.info('删除成功!');
                this.fetchTableData();
            });
    }

    changeStatus = (id, checked) => {
        FetchUtil('/feature/' + (checked ? 'enable/' : 'disable/') + id, 'POST', '',
            (data) => {
                message.info(checked ? '已启用!' : '已停用!');
                this.fetchTableData();
            });
    }

    render() {
        const columns = [
            {
                title: '序号',
                dataIndex: 'id',
                render: (t, r, i) => i + 1
            },
            {
                title: '特征名',
                dataIndex: 'label'
            }, {
                title: '标识',
                dataIndex: 'featureName'
            }, {
                title: '类型',
                dataIndex: 'type',
                render: (t) => {
                    const x = featureTypeOf(t);
                    return <Tag color={x.color}>{x.label}</Tag>;
                }
            }, {
                title: '阶段',
                dataIndex: 'type',
                render: (t) => FEATURE_STAGES[t] || '-'
            }, {
                title: '顺序',
                dataIndex: 'orderNo',
                width: 70
            }, {
                title: '备注',
                dataIndex: 'comment'
            }, {
                title: '操作',
                dataIndex: 'handle',
                render: (t, r) => (
                    <span>
                        <FeatureModal scenarioId={this.props.params.id} row={r} reload={this.fetchTableData}/>
                        <span className="ant-divider"></span>
                        <Popconfirm placement="bottomRight" title={'确认删除该特征吗？'}
                            onConfirm={this.deleteFeature.bind(this, r.id)}>
                            <Tooltip title="删除"><a style={{color: '#FD5B5B'}}>删除</a></Tooltip>
                        </Popconfirm>
                        <span className="ant-divider"></span>
                        <Switch checkedChildren={'开'} unCheckedChildren={'关'} checked={r.status > 0}
                            onChange={this.changeStatus.bind(this, r.id)}/>
                    </span>
                )
            }];

        return (
            <div className="ant-layout-content">
                <div id="header">
                    <Form layout="inline">
                        <FormItem label="特征名：">
                            <Input value={this.state.label} name="label" onChange={this.handleChange}/>
                        </FormItem>
                        {' '}
                        <Button type="primary" onClick={() => {
                            this.setState({label: ''}, () => this.fetchTableData());
                        }}>查询</Button>
                        {' '}
                        <FeatureModal scenarioId={this.props.params.id} reload={this.fetchTableData}/>
                        {' '}
                        <Tooltip title="规则与策略通过 features.<特征名> 引用特征值">
                            <a style={{color: '#999'}}>引用方式：features.&lt;特征名&gt;</a>
                        </Tooltip>
                    </Form>
                </div>

                <div id="table">
                    <Table dataSource={this.state.tData}
                        columns={columns}
                        size="middle"
                        pagination={false}
                        loading={this.state.loading}
                        rowKey={record => record.id}/>
                </div>
            </div>
        );
    }
}

export default withParams(FeatureList);
