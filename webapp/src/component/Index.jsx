import React from 'react';
import {Link} from 'react-router';

import {Menu,Breadcrumb,Icon,Tooltip} from 'antd';
const SubMenu = Menu.SubMenu;
import './Index.less';
import {FetchUtil} from './utils/fetchUtil';

export default class Index extends React.Component{
    constructor(props){
        super(props);

    }
    handleLogout=()=>{
      FetchUtil('/merchant/logout','GET','',
            (data) => {
                window.location.href="#/login";
                localStorage.setItem('x-auth-token','')
            });
    }

    render(){

        return (


      <div className="ant-layout-ceiling-demo">
      <div className="ant-layout-header">
        <div className="ant-layout-wrapper">
          <div className="ant-layout-logo">
            <Tooltip title="回到首页"><Link to="/modelList"><Icon type="home" /></Link></Tooltip>
          </div>
          <div className="ant-layout-logo">
            <Tooltip title="统计报表"><Link to="/report"><Icon type="line-chart" /></Link></Tooltip>
          </div>
          <div className="ant-layout-logo" style={{float:"right",marginRight:0}}>
            <Tooltip title="退出登录"><a onClick={this.handleLogout}><Icon type="logout" /></a></Tooltip>
          </div>
        </div>
      </div>
      <div className="ant-layout-main">
          {this.props.children}
        </div>
    </div>
        );
    }
}