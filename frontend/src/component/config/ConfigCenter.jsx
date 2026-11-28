import React from 'react';
import {Breadcrumb, Menu, Form, Select, Card, Row, Col} from 'antd';
import {Link} from 'react-router';
const FormItem=Form.Item;

import {FetchUtil} from '../utils/fetchUtil';

export default class ConfigCenter extends React.Component{

	render() {
		return (
				<div className="ant-layout-wrapper">
					<div className="ant-layout-breadcrumb">
	                    <Breadcrumb items={['首页', '配置中心']}/>
	                </div>
				  <div className="ant-layout-container">
				  	 <Row gutter={8}>
						
						<Col span={6}>
						    <Card size="small" title="数据列表配置" extra={<Link to={"/datalist/1"}>More</Link>} style={{ width: 300 }}>
						     	  <p>配置系统级黑白名单数据，此数据所有场景共用。</p>
						     	  <p>&nbsp;</p>
						     	  <Link to={"/datalist/1"}>进入配置</Link>
						    </Card>
						</Col>
						<Col span={6}>
						    <Card size="small" title="待开发" extra={<a >More</a>} style={{ width: 300 }}>
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