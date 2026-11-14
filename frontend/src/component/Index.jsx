import React from 'react';
import { Link, Outlet } from 'react-router';

import {Tooltip} from 'antd';
import {HomeOutlined, LineChartOutlined, LogoutOutlined} from '@ant-design/icons';
import './Index.less';
import {FetchUtil} from './utils/fetchUtil';

export default class Index extends React.Component{
    constructor(props){
        super(props);

    }
    handleLogout=()=>{
      FetchUtil('/user/logout','GET','',
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
            <Tooltip title="回到首页"><Link to="/modelList"><HomeOutlined /></Link></Tooltip>
          </div>
          <div className="ant-layout-logo">
            <Tooltip title="统计报表"><Link to="/report"><LineChartOutlined /></Link></Tooltip>
          </div>
          <div className="ant-layout-logo">
            {/*<Tooltip title="配置中心"><Link to="/config"><SettingOutlined /></Link></Tooltip>*/}
          </div>

          <div className="ant-layout-logo" style={{float:"right",marginRight:0}}>
            <Tooltip title="退出登录"><a onClick={this.handleLogout}><LogoutOutlined /></a></Tooltip>
          </div>
          <div className="ant-layout-logo" style={{float:"right",marginRight:100}}>
               <a target="_blank" href='https://gitee.com/freshday/radar'><img src='https://gitee.com/freshday/radar/badge/star.svg?theme=gvp' alt='star'></img></a>
          </div>
        </div>
      </div>
      <div className="ant-layout-main">
          <Outlet />
        </div>
    </div>
        );
    }
}
