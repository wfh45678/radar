import React from 'react';
import {Breadcrumb,Menu,Icon,Form,Select, Card, Row, Col} from 'antd';
import {Link} from 'react-router';
const FormItem=Form.Item;
const Option = Select.Option;

import {FetchUtil} from '../utils/fetchUtil';

export default class ConfigCenter extends React.Component{

	render() {
		return (
				<div className="ant-layout-wrapper">
					<div className="ant-layout-breadcrumb">
	                    <Breadcrumb>
	                        <Breadcrumb.Item>首页</Breadcrumb.Item>
	                        <Breadcrumb.Item>配置中心</Breadcrumb.Item>
	                    </Breadcrumb>
	                </div>
				  <div className="ant-layout-container">
				  	 <Row gutter={8}>
						
						<Col span={6}>
						    <Card size="small" title="数据列表配置" extra={<a >More</a>} style={{ width: 300 }}>
						     	  <p>全局黑白名单数据的配置。。。</p>
						     	  <p>&nbsp;</p>
						     	  <Link >进入配置</Link>
						    </Card>
						</Col>
						<Col span={6}>
						    <Card title="待开发" extra={<a >More</a>} style={{ width: 300 }}>
						      	<p>待开发。。。</p>
						      	<p>&nbsp;</p>
						     	<Link >进入配置</Link>
						    </Card>
						</Col>
						<Col span={6} />

						<Col span={6} />

				  	 </Row>
					 <Row>
					 	
					 </Row>

				  </div>
				</div>
			);
	}
}