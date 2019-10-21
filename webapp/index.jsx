import React from 'react';
import ReactDOM from 'react-dom';
import { Router, Route, Link, hashHistory,browserHistory,IndexRoute,IndexRedirect } from 'react-router';

import Abstraction from './component/abstraction/Abstraction';
import AbstractionList from './component/abstraction/AbstractionList';
import Activation from './component/activation/Activation';
import Datalist from './component/datalist/Datalist';
import DatalistRecord from './component/datalist/DatalistRecord';
import Field from './component/field/Field';
import ModelList from './component/model/ModelList';
import Model from './component/model/Model';
import PreItem from './component/preItem/PreItem';
import RuleList from './component/activation/RuleList';
import HistoryRecordList from './component/activation/HistoryRecordList';

import Report from './component/report/Report';
import ListEvent from './component/report/ListEvent';
import RuleGraph from './component/report/RuleGraph';
import ListRule from './component/report/ListRule';
import Rule from './component/report/ListRule';
import DashBoard from './component/report/DashBoard';


//import Test from './component/test/Test'; 

import Index from './component/Index';
import Login from './component/Login';

// 引入Ant-Design样式 & Animate.CSS样式
import 'antd/dist/antd.min.css';
import 'animate.css/animate.min.css';

import './main.less';



class NotFound extends React.Component{
  render() {
    return (
      <div>
        <div className="ibox">
          <div className="ibox-content">
              <div style={{
                  width:450,margin:"200px auto",fontSize:26
              }}>功能尚未完成或页面未找到，敬请期待</div>               
          </div>
        </div>
      </div>
    );
  }
};

class Welcome extends React.Component{
  render() {
    return (
      <div>
        <div className="ibox">
          <div className="ibox-content">
              <h2>欢迎登录风控引擎管理平台！</h2>
          </div>
        </div>
      </div>
    );
  }
};

class App extends React.Component{
    render(){
        return (
            <div>
                {this.props.children}
            </div>
        );
    }
}

class AppRoutes extends React.Component{

  render() {
    return (
      <Router history={hashHistory}>
        <Route path="/" component={App}>
          <IndexRedirect to="login"/>
          <Route path="/welcome" component={Welcome}>
          </Route>
          <Route path="/login" component={Login}>
          </Route>
          <Route path="/index" component={Index}>            
            <Route path="/modelList" component={ModelList}/>
            <Route path="/model/:id" component={Model}>
                <IndexRedirect to="/field/:id"/>
                <Route path="/field/:id" component={Field}/>
                <Route path="/activation/:id" component={Activation}/>
                <Route path="/datalist/:id" component={Datalist}/>
                <Route path="/datalistRecord/:id/:datalistId" component={DatalistRecord}/>
                <Route path="/preItem/:id" component={PreItem}/>
                <Route path="/ruleList/:id/:activationId" component={RuleList}/>
                <Route path="/historyRecordList/:id/:activationId/:ruleId" component={HistoryRecordList}/>
                <Route path="/abstractionList/:id" component={AbstractionList}/>

                {/*<Route path="/test/:id" component={Test}/>*/}
            </Route>

            <Route path="/report" component={Report}>
                <IndexRedirect to="/event"/>
                <Route path="/event" component={ListEvent}/>
                <Route path="/graph" component={RuleGraph}/>
                <Route path="/rule" component={ListRule}/>
                <Route path="/ruleid/:modelId/:ruleId/:activationName" component={ListEvent}/>
                <Route path="/dashboard" component={DashBoard}/>

            </Route>            
          </Route>
          <Route path="*" component={Index}>
            <IndexRoute component={NotFound} />
          </Route>
        </Route>
	    </Router>  
    );
  }
};

ReactDOM.render(
  <AppRoutes />
, document.getElementById("react-content"));