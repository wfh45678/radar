import React from 'react';
import {Breadcrumb, Button, Table, Tag, Tooltip, Upload, Switch, Popconfirm, message} from 'antd';
import {ReloadOutlined, UploadOutlined} from '@ant-design/icons';

import {FetchUtil} from '../utils/fetchUtil';

/**
 * 插件管理（v3.1）：上传外置插件 jar / 启停 / 删除 / 手动重载 / 执行统计快照。
 * 后端：/services/v1/plugins（生命周期）+ 引擎插件配置随列表返回。
 */
export default class PluginManagement extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            plugins: [],
            config: {},
            loading: true,
            uploading: false
        };
        this.fetchList();
    }

    fetchList = () => {
        this.setState({loading: true});
        FetchUtil('/plugins', 'GET', '',
            (data) => {
                this.setState({
                    plugins: data.data.plugins || [],
                    config: data.data.config || {},
                    loading: false
                });
            },
            () => this.setState({loading: false}));
    };

    handleUpload = (file) => {
        this.setState({uploading: true});
        const formData = new FormData();
        formData.append('file', file);
        // FetchUtil 只发 JSON，multipart 在此单独走 fetch（带同样的鉴权头）
        fetch('/services/v1/plugins', {
            method: 'POST',
            credentials: 'include',
            headers: {'x-auth-token': localStorage.getItem('x-auth-token')},
            body: formData
        }).then((res) => res.json()).then((data) => {
            this.setState({uploading: false});
            if (data.success) {
                const keys = (data.data && data.data.upload && data.data.upload.keys) || [];
                message.success(data.msg + (keys.length ? '：' + keys.join(', ') : ''));
                this.fetchList();
            } else {
                message.error(data.msg || '上传失败');
            }
        }).catch((e) => {
            this.setState({uploading: false});
            message.error('上传失败：' + e.message);
        });
        return false; // 阻止 antd 默认上传行为
    };

    handleToggle = (record, enabled) => {
        FetchUtil('/plugins/' + encodeURIComponent(record.key) + '/enabled?enabled=' + enabled, 'PUT', '',
            () => {
                message.success(enabled ? '已启用' : '已禁用');
                this.fetchList();
            });
    };

    handleDelete = (record) => {
        FetchUtil('/plugins/' + encodeURIComponent(record.key), 'DELETE', '',
            (data) => {
                message.success(data.msg || '已删除');
                this.fetchList();
            });
    };

    handleReload = () => {
        FetchUtil('/plugins/reload', 'POST', '',
            (data) => {
                const registered = (data.data && data.data.registered) || [];
                message.success('重载完成，外置插件 ' + registered.length + ' 个生效');
                this.fetchList();
            });
    };

    renderSource = (source) => {
        if (source === 'EXTERNAL') {
            return <Tag color="green">外置</Tag>;
        }
        if (source === 'BUILTIN_V2') {
            return <Tag color="orange">内置 v2</Tag>;
        }
        return <Tag color="blue">内置</Tag>;
    };

    render() {
        const hotReload = this.state.config.hotReload !== false;
        const columns = [
            {
                title: '插件 key', dataIndex: 'key', key: 'key',
                sorter: (a, b) => a.key.localeCompare(b.key)
            },
            {title: '描述', dataIndex: 'desc', key: 'desc'},
            {
                title: '版本', dataIndex: 'version', key: 'version',
                render: (v) => v ? <Tag>{v}</Tag> : <span style={{color: '#ccc'}}>-</span>
            },
            {
                title: '提供方', dataIndex: 'provider', key: 'provider',
                render: (v) => v || <span style={{color: '#ccc'}}>-</span>
            },
            {
                title: '来源', dataIndex: 'source', key: 'source', width: 90,
                render: (s) => this.renderSource(s)
            },
            {
                title: 'jar 文件', dataIndex: 'jarFile', key: 'jarFile',
                render: (v) => v || <span style={{color: '#ccc'}}>-</span>
            },
            {
                title: '启用', dataIndex: 'enabled', key: 'enabled', width: 80,
                render: (enabled, record) => (
                    <Switch checked={!!enabled} size="small"
                        onChange={(v) => this.handleToggle(record, v)} />
                )
            },
            {
                title: '执行统计', dataIndex: 'metrics', key: 'metrics',
                render: (m) => {
                    if (!m) {
                        return '-';
                    }
                    return (
                        <span>
                            成功 {m.success || 0} / 失败 {m.fail || 0}
                            {m.notFound ? <Tooltip title="配置了不存在的插件"><Tag color="red">缺失 {m.notFound}</Tag></Tooltip> : null}
                            {m.count ? <span style={{color: '#888'}}>　均 {Math.round(m.avgMillis)}ms</span> : null}
                        </span>
                    );
                }
            },
            {
                title: '操作', key: 'action', width: 90,
                render: (_, record) => (
                    record.source === 'EXTERNAL'
                        ? (
                            <Popconfirm title={'确定删除插件 ' + record.key + ' 吗？'}
                                onConfirm={() => this.handleDelete(record)}>
                                <a style={{color: '#f5222d'}}>删除</a>
                            </Popconfirm>
                        )
                        : <span style={{color: '#ccc'}}>-</span>
                )
            }
        ];

        return (
            <div className="ant-layout-wrapper">
                <div className="ant-layout-breadcrumb">
                    <Breadcrumb items={['首页', '插件管理']}/>
                </div>
                <div className="ant-layout-container">
                    <div style={{marginBottom: 12}}>
                        <Upload beforeUpload={this.handleUpload} showUploadList={false}
                            accept=".jar" disabled={this.state.uploading}>
                            <Button type="primary" icon={<UploadOutlined/>} loading={this.state.uploading}>
                                上传插件 jar
                            </Button>
                        </Upload>
                        <Button icon={<ReloadOutlined/>} style={{marginLeft: 8}}
                            onClick={this.handleReload}>
                            手动重载
                        </Button>
                        <Tooltip title={hotReload
                            ? '上传/删除后自动热重载（旧加载器宽限释放）'
                            : '热重载已关闭（plugin.admin.hot-reload=false），上传/删除需重启引擎生效'}>
                            <Tag color={hotReload ? 'green' : 'orange'} style={{marginLeft: 8, cursor: 'default'}}>
                                {hotReload ? '热重载：即时生效' : '热重载关闭：重启生效'}
                            </Tag>
                        </Tooltip>
                        <span style={{color: '#999', marginLeft: 8}}>
                            插件目录：{this.state.config.pluginsDir || '-'}
                        </span>
                    </div>
                    <Table columns={columns} rowKey="key" size="middle"
                        dataSource={this.state.plugins}
                        loading={this.state.loading}
                        pagination={false}/>
                </div>
            </div>
        );
    }
}
