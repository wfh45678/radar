import React from 'react';
import {Button, Modal, Form, Row, Col, Input, InputNumber, Select, Cascader, Tooltip, message, Switch, Tag} from 'antd';
import {PlusOutlined, QuestionCircleOutlined} from '@ant-design/icons';
const FormItem = Form.Item;

import {FetchUtil} from '../../utils/fetchUtil';
import {FEATURE_TYPES, DERIVED_FUNCTIONS} from '../featureTypes';
import ComplexCondition from '../../abstraction/ComplexCondition';
import {validateRules} from '../../utils/groovyUtil';

const formItemLayout = {labelCol: {span: 6}, wrapperCol: {span: 17}};

/**
 * 特征新建/编辑弹窗（v4.1）：按类型渲染四类编辑表单，
 * 统一保存 PUT /feature（definition 按 TYPE 组装 JSON）。
 */
export default class FeatureModal extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            open: false,
            saving: false,

            id: null,
            type: 'ENRICH',
            label: '',
            featureName: '',
            orderNo: null,
            comment: '',
            status: 1,

            // ENRICH
            plugin: '',
            argsText: '',
            sourceFields: [],
            // AGGREGATE
            aggregateType: 'COUNT',
            searchField: '',
            intervalType: 5,
            intervalValue: 1,
            functionField: '',
            ruleDefinition: null,
            // DERIVED
            fn: 'ADD',
            operands: [],   // [{column} | {const}]

            fieldList: [],
            pluginList: []
        };
    }

    componentDidMount() {
        FetchUtil('/feature/columns/' + this.props.scenarioId, 'GET', '',
            (data) => {
                this.setState({fieldList: data.data.list || []});
            });
        FetchUtil('/common/plugins', 'GET', '',
            (data) => {
                this.setState({pluginList: data.data.plugins || data.data.list || []});
            });
    }

    showModal = () => {
        if (this.props.row) {
            // 编辑：回填
            FetchUtil('/feature/' + this.props.row.id, 'GET', '',
                (data) => {
                    const f = data.data.feature;
                    const d = f.definition || {};
                    this.setState({
                        open: true,
                        id: f.id,
                        type: f.type,
                        label: f.label,
                        featureName: f.featureName,
                        orderNo: f.orderNo,
                        comment: f.comment,
                        status: f.status,
                        plugin: d.plugin || '',
                        argsText: d.args && typeof d.args === 'object' ? JSON.stringify(d.args) : (d.args || ''),
                        sourceFields: d.sourceFields || [],
                        aggregateType: d.aggregateType || 'COUNT',
                        searchField: d.searchField || '',
                        intervalType: d.searchIntervalType || 5,
                        intervalValue: d.searchIntervalValue || 1,
                        functionField: d.functionField || '',
                        ruleDefinition: d.filter || null,
                        fn: d.expression ? d.expression.fn : 'ADD',
                        operands: d.expression ? d.expression.args || [] : []
                    });
                });
        } else {
            this.setState({
                open: true, id: null, type: 'ENRICH', label: '', featureName: '', orderNo: null,
                comment: '', status: 1, plugin: '', argsText: '', sourceFields: [],
                aggregateType: 'COUNT', searchField: '', intervalType: 5, intervalValue: 1,
                functionField: '', ruleDefinition: null, fn: 'ADD', operands: []
            });
        }
    }

    handleState = (name, value) => {
        this.setState({[name]: value});
    }

    handleChange = (e) => {
        this.handleState(e.target.name, e.target.value);
    }

    displayRender = (label) => label.join('.');

    // ===== 求值节点编辑（DERIVED）=====
    addOperand = () => {
        this.setState({operands: this.state.operands.concat([{column: ''}])});
    }

    removeOperand = (i) => {
        this.setState({operands: this.state.operands.filter((o, j) => j !== i)});
    }

    updateOperand = (i, key, value) => {
        this.setState({
            operands: this.state.operands.map((o, j) => {
                if (j === i) { o[key] = value; if (key === 'column') { delete o.const; } if (key === 'const') { delete o.column; } }
                return o;
            })
        });
    }

    // ===== AGGREGATE 过滤条件 =====
    handleFilterChange = (condition) => {
        this.setState({ruleDefinition: condition});
    }

    validate = () => {
        const s = this.state;
        if (!s.label) { return '请填写特征名'; }
        for (const t of FEATURE_TYPES) {
            if (t.value === s.type && (!s.featureName || !/^[a-zA-Z][a-zA-Z0-9_]*$/.test(s.featureName))) {
                return '特征标识必须为字母开头的字母/数字/下划线（引用键 features.<标识>）';
            }
        }
        if (s.type === 'ENRICH') {
            if (!s.plugin) { return '请选择插件'; }
            if (!s.featureName) { return '请填写特征标识'; }
        }
        if (s.type === 'AGGREGATE') {
            if (!s.searchField) { return '请选择搜索字段'; }
            if (['SUM', 'AVERAGE', 'MAX', 'MIN', 'SD', 'VARIANCE', 'DEVIATION', 'MEDIAN'].indexOf(s.aggregateType) >= 0 && !s.functionField) {
                return '该聚合类型需要选择统计字段';
            }
            if (s.ruleDefinition && !validateRules(s.ruleDefinition)) { return '过滤条件不完整'; }
        }
        if (s.type === 'DERIVED') {
            const arity = (DERIVED_FUNCTIONS.find(f => f.value === s.fn) || {arity: [2, 2]}).arity;
            if (s.operands.length < arity[0]) { return '该函数至少需要 ' + arity[0] + ' 个操作数'; }
            for (const o of s.operands) {
                if (!o.column && (o.const === undefined || o.const === null || o.const === '')) {
                    return '存在未填写的操作数';
                }
            }
        }
        if (s.type === 'RATIO') {
            if (!s.operands[0] || !s.operands[1]) { return '请填写分子与分母'; }
        }
        return null;
    }

    buildDefinition = () => {
        const s = this.state;
        if (s.type === 'ENRICH') {
            let args = null;
            if (s.argsText && s.argsText.trim().startsWith('{')) {
                try { args = JSON.parse(s.argsText); } catch (e) { args = s.argsText; }
            } else {
                args = s.argsText || null;
            }
            return {plugin: s.plugin, args: args, sourceFields: s.sourceFields};
        }
        if (s.type === 'AGGREGATE') {
            return {
                aggregateType: s.aggregateType,
                searchField: s.searchField,
                searchIntervalType: s.intervalType,
                searchIntervalValue: Number(s.intervalValue) || 1,
                functionField: s.functionField || '',
                filter: s.ruleDefinition || null
            };
        }
        if (s.type === 'DERIVED') {
            return {expression: {fn: s.fn, args: s.operands}};
        }
        if (s.type === 'RATIO') {
            return {
                left: s.operands[0] ? (s.operands[0].column || s.operands[0].const) : '',
                right: s.operands[1] ? (s.operands[1].column || s.operands[1].const) : '',
                operator: s.fn === 'SUB' ? 'SUB' : 'DIV',
                scale: 2, onZero: 'ZERO'
            };
        }
        return {};
    }

    handleSubmit = () => {
        const error = this.validate();
        if (error) { message.warning(error); return; }
        const s = this.state;
        const param = {
            id: s.id || null,
            scenarioId: Number(this.props.scenarioId),
            featureName: s.featureName || '',
            label: s.label,
            type: s.type,
            orderNo: s.orderNo == null ? null : Number(s.orderNo),
            comment: s.comment,
            status: s.status || 1,
            definition: this.buildDefinition()
        };
        this.setState({saving: true});
        FetchUtil('/feature', 'PUT', JSON.stringify(param),
            (data) => {
                this.setState({saving: false});
                if (data.success) {
                    message.success('特征已保存，保存即生效');
                    this.setState({open: false});
                    this.props.reload && this.props.reload();
                } else {
                    message.error(data.msg);
                }
            });
    }

    renderPluginParams = () => {
        const meta = this.state.pluginList.find(x => x.method === this.state.plugin || x.key === this.state.plugin);
        if (!meta || !meta.paramSchema || meta.paramSchema.length === 0) {
            return (
                <FormItem {...formItemLayout} label="插件参数：">
                    <Input type="textarea" rows={2} name="argsText" value={this.state.argsText}
                        onChange={this.handleChange} placeholder='JSON 对象，如 {"start":0,"end":7}；或自由文本（插件兜底）'/>
                </FormItem>
            );
        }
        return meta.paramSchema.map((d, i) => (
            <FormItem key={i} {...formItemLayout} label={d.label || d.name} required={d.required}>
                {d.options && d.options.length > 0 ?
                    <Select style={{width: 220}} value={this.argValue(d.name, d.defaultValue)}
                        onChange={(v) => this.setArg(d.name, v)}
                        options={d.options.map(o => ({label: o.label || o.value, value: o.value}))}/> :
                    (d.type === 'BOOLEAN' ?
                        <Switch checked={this.argValue(d.name, d.defaultValue) === true || this.argValue(d.name, d.defaultValue) === 'true'}
                            onChange={(v) => this.setArg(d.name, v)}/> :
                        <InputNumber style={{width: 220}} value={this.argValue(d.name, d.defaultValue)}
                            onChange={(v) => this.setArg(d.name, v)}/> )
                }
                {d.placeholder ? <Tooltip title={d.placeholder}><QuestionCircleOutlined style={{marginLeft: 6}}/></Tooltip> : null}
            </FormItem>
        ));
    }

    args = () => {
        // schema 模式参数的临时存储（写入 argsText JSON）
        if (!this.state.argsText || !this.state.argsText.trim().startsWith('{')) {
            return {};
        }
        try { return JSON.parse(this.state.argsText); } catch (e) { return {}; }
    }

    argValue = (name, defaultValue) => {
        const a = this.args();
        return a[name] !== undefined ? a[name] : defaultValue;
    }

    setArg = (name, value) => {
        const a = this.args();
        a[name] = value;
        this.setState({argsText: JSON.stringify(a)});
    }

    render() {
        const s = this.state;
        const typeInfo = FEATURE_TYPES.find(t => t.value === s.type) || FEATURE_TYPES[0];
        const isEdit = !!this.props.row;
        const cascader = (value, onChange, placeholder) => (
            <Cascader style={{width: '100%'}} options={this.state.fieldList} allowClear={false}
                value={value ? value.split('.') : []}
                displayRender={this.displayRender}
                onChange={(v) => onChange(v.join('.'))}
                placeholder={placeholder || '选择字段/特征'}/>
        );

        return (
            <span>
                {isEdit ?
                    <a onClick={this.showModal}>编辑</a> :
                    <Button type="primary" icon={<PlusOutlined/>} onClick={this.showModal}>新增特征</Button>}
                <Modal title={(isEdit ? '编辑特征' : '新增特征') + '（' + typeInfo.label + '）'}
                    open={this.state.open}
                    onCancel={() => this.setState({open: false})}
                    onOk={this.handleSubmit}
                    confirmLoading={this.state.saving}
                    width={860}>
                    <Form>
                        <FormItem {...formItemLayout} label="特征类型：" required>
                            {isEdit ?
                                <Tag color={typeInfo.color}>{typeInfo.label}</Tag> :
                                <Select value={s.type} onChange={this.handleState.bind(this, 'type')}
                                    options={FEATURE_TYPES.map(t => ({label: t.label, value: t.value}))}/>}
                        </FormItem>
                        <FormItem {...formItemLayout} label="特征名：" required
                            help={!s.label ? '' : undefined} validateStatus={s.label ? 'success' : 'warning'}>
                            <Input name="label" value={s.label} onChange={this.handleChange} maxLength={32}/>
                        </FormItem>
                        <FormItem {...formItemLayout} label="特征标识：" required
                            extra="引用键 features.<标识>，字母开头，字母/数字/下划线">
                            <Input name="featureName" value={s.featureName} onChange={this.handleChange}
                                placeholder="如 tran_ip_1_day_qty（留空自动生成）" maxLength={64}/>
                        </FormItem>
                        <FormItem {...formItemLayout} label="执行顺序：">
                            <InputNumber min={0} value={s.orderNo} onChange={this.handleState.bind(this, 'orderNo')}/>
                            <Tooltip title="同阶段内按顺序执行，链式特征请编排顺序"><QuestionCircleOutlined style={{marginLeft: 6}}/></Tooltip>
                        </FormItem>
                        <FormItem {...formItemLayout} label="备注：">
                            <Input name="comment" value={s.comment} onChange={this.handleChange} maxLength={128}/>
                        </FormItem>

                        <div style={{borderTop: '1px dashed #ddd', margin: '8px 0 16px'}}></div>

                        {s.type === 'ENRICH' && (
                            <div>
                                <FormItem {...formItemLayout} label="插件：" required>
                                    <Select showSearch style={{width: 300}} value={s.plugin || undefined}
                                        placeholder="选择插件"
                                        onChange={this.handleState.bind(this, 'plugin')}
                                        options={this.state.pluginList.map(x => ({label: (x.pluginName || x.key) + '（' + x.key + '）', value: x.key}))}/>
                                </FormItem>
                                <FormItem {...formItemLayout} label="来源字段：" required
                                    extra="多个用逗号分隔；可引用前序特征（features.<标识>）实现链式加工">
                                    <Select mode="tags" style={{width: 300}} value={s.sourceFields}
                                        placeholder="输入或选择来源字段"
                                        onChange={this.handleState.bind(this, 'sourceFields')}
                                        options={this.state.fieldList.map(g => ({label: g.label, value: g.value, children: (g.children || []).map(c => ({label: c.label, value: c.value}))}))}/>
                                </FormItem>
                                {this.renderPluginParams()}
                            </div>
                        )}

                        {s.type === 'AGGREGATE' && (
                            <div>
                                <FormItem {...formItemLayout} label="聚合类型：" required>
                                    <Select style={{width: 220}} value={s.aggregateType}
                                        onChange={this.handleState.bind(this, 'aggregateType')}
                                        options={[
                                            {label: '计数 COUNT', value: 'COUNT'},
                                            {label: '去重计数 DISTINCT_COUNT', value: 'DISTINCT_COUNT'},
                                            {label: '求和 SUM', value: 'SUM'},
                                            {label: '平均 AVERAGE', value: 'AVERAGE'},
                                            {label: '最大 MAX', value: 'MAX'},
                                            {label: '最小 MIN', value: 'MIN'},
                                            {label: '标准差 SD', value: 'SD'},
                                            {label: '方差 VARIANCE', value: 'VARIANCE'},
                                            {label: '偏离率 DEVIATION', value: 'DEVIATION'},
                                            {label: '中位数 MEDIAN', value: 'MEDIAN'}
                                        ]}/>
                                </FormItem>
                                <FormItem {...formItemLayout} label="搜索字段：" required
                                    extra="按该字段值检索历史事件（建议已建索引字段）">
                                    {cascader(s.searchField, (v) => this.handleState('searchField', v))}
                                </FormItem>
                                <FormItem {...formItemLayout} label="时间窗：" required>
                                    往前
                                    <InputNumber min={1} style={{width: 80, margin: '0 6px'}} value={s.intervalValue}
                                        onChange={this.handleState.bind(this, 'intervalValue')}/>
                                    <Select style={{width: 100}} value={s.intervalType}
                                        onChange={this.handleState.bind(this, 'intervalType')}
                                        options={[
                                            {label: '年', value: 1}, {label: '月', value: 2}, {label: '日', value: 5},
                                            {label: '时', value: 11}, {label: '分', value: 12}, {label: '秒', value: 13}]}/>
                                </FormItem>
                                {['SUM', 'AVERAGE', 'MAX', 'MIN', 'SD', 'VARIANCE', 'DEVIATION', 'MEDIAN'].indexOf(s.aggregateType) >= 0 && (
                                    <FormItem {...formItemLayout} label="统计字段：" required>
                                        {cascader(s.functionField, (v) => this.handleState('functionField', v))}
                                    </FormItem>
                                )}
                                <FormItem {...formItemLayout} label="过滤条件：">
                                    {s.ruleDefinition ?
                                        <ComplexCondition fieldList={this.state.fieldList} dataList={[]}
                                            condition={s.ruleDefinition} changeParentCondition={this.handleFilterChange} index={0}/> :
                                        <Button onClick={() => this.handleState('ruleDefinition', {class: 'PDCT', enabled: true, linking: 'All', conditions: []})}>
                                            添加过滤条件（缺省恒真）
                                        </Button>}
                                </FormItem>
                            </div>
                        )}

                        {s.type === 'DERIVED' && (
                            <div>
                                <FormItem {...formItemLayout} label="函数：" required>
                                    <Select style={{width: 220}} value={s.fn} onChange={this.handleState.bind(this, 'fn')}
                                        options={DERIVED_FUNCTIONS.map(f => ({label: f.label + '（' + f.value + '）', value: f.value}))}/>
                                </FormItem>
                                <FormItem {...formItemLayout} label="操作数：" required>
                                    {s.operands.map((o, i) => (
                                        <Row key={i} gutter={8} style={{marginBottom: 6}}>
                                            <Col span={17}>
                                                {o.const !== undefined ?
                                                    <Input addonBefore="常量" value={o.const}
                                                        onChange={(e) => this.updateOperand(i, 'const', e.target.value)}/> :
                                                    cascader(o.column, (v) => this.updateOperand(i, 'column', v))}
                                            </Col>
                                            <Col span={6}>
                                                <Button size="small" onClick={() => this.updateOperand(i, 'const', o.const !== undefined ? o.column : '')}>
                                                    {o.const !== undefined ? '改为变量' : '改为常量'}
                                                </Button>
                                                {' '}<a style={{color: '#FD5B5B'}} onClick={this.removeOperand.bind(this, i)}>删除</a>
                                            </Col>
                                        </Row>
                                    ))}
                                    <Button type="dashed" icon={<PlusOutlined/>} onClick={this.addOperand}>添加操作数</Button>
                                </FormItem>
                            </div>
                        )}

                        {s.type === 'RATIO' && (
                            <div>
                                <FormItem {...formItemLayout} label="运算函数：" required>
                                    <Select style={{width: 220}} value={s.fn === 'ADD' ? 'ADD' : (s.fn)}
                                        onChange={this.handleState.bind(this, 'fn')}
                                        options={[{label: '相除（比率）', value: 'DIV'}, {label: '相减（差额）', value: 'SUB'}]}/>
                                </FormItem>
                                {[0, 1].map(i => (
                                    <FormItem key={i} {...formItemLayout} label={i === 0 ? '被运算数：' : '运算数：'} required>
                                        {cascader((s.operands[i] && s.operands[i].column) || '',
                                            (v) => {
                                                const ops = s.operands.slice();
                                                ops[i] = {column: v};
                                                this.setState({operands: ops});
                                            })}
                                    </FormItem>
                                ))}
                            </div>
                        )}
                    </Form>
                </Modal>
            </span>
        );
    }
}
