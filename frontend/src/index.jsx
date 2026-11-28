import React from 'react';
import { createRoot } from 'react-dom/client';
import { createHashRouter, RouterProvider, Navigate, Outlet, useParams } from 'react-router';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

dayjs.locale('zh-cn');

import StrategyList from './component/strategy/StrategyList';
import Datalist from './component/datalist/Datalist';
import DatalistRecord from './component/datalist/DatalistRecord';
import Field from './component/field/Field';
import ScenarioList from './component/scenario/ScenarioList';
import Scenario from './component/scenario/Scenario';
import FeatureList from './component/feature/FeatureList';
import RuleList from './component/strategy/RuleList';
import HistoryRecordList from './component/strategy/HistoryRecordList';
import StrategyDetail from './component/strategy/StrategyDetail';

import Report from './component/report/Report';
import ListEvent from './component/report/ListEvent';
import RuleGraph from './component/report/RuleGraph';
import ListRule from './component/report/ListRule';
import DashBoard from './component/report/dashboard';
import ConfigCenter from './component/config/ConfigCenter';
import PluginManagement from './component/plugin/PluginManagement';

import Index from './component/Index';
import Login from './component/Login';

import './main.less';

function NotFound() {
  return (
    <div className="ibox">
      <div className="ibox-content">
        <div style={{ width: 450, margin: '200px auto', fontSize: 26 }}>功能尚未完成或页面未找到，敬请期待</div>
      </div>
    </div>
  );
}

function Welcome() {
  return (
    <div className="ibox">
      <div className="ibox-content">
        <h2>欢迎登录风控引擎管理平台！</h2>
      </div>
    </div>
  );
}

function App() {
  return <Outlet />;
}

// /scenario/:id 默认跳转：等价原 IndexRedirect to="/field/:id"（复用同一个 id）
function ScenarioIndexRedirect() {
  const { id } = useParams();
  return <Navigate to={`/field/${id}`} replace />;
}

// URL 结构与 2022 版（react-router 3 绝对 path，扁平 URL）保持一致：
// #/scenarioList、#/field/:id、#/event、#/ruleid/:scenarioId/:ruleId/:strategyName 等。
// Index / Scenario / Report 是嵌套的 pathless layout route，仅承担组件层级，不往 URL 加前缀。
const router = createHashRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <Navigate to="/login" replace /> },
      { path: 'welcome', element: <Welcome /> },
      { path: 'login', element: <Login /> },
      {
        element: <Index />,
        children: [
          { path: 'scenarioList', element: <ScenarioList /> },
          { path: 'scenario/:id', element: <ScenarioIndexRedirect /> },
          {
            // 场景工作区框架（原 rr3 中 /scenario/:id 的布局父组件）
            element: <Scenario />,
            children: [
              { path: 'field/:id', element: <Field /> },
              { path: 'strategy/:id', element: <StrategyList /> },
              { path: 'datalist/:id', element: <Datalist /> },
              { path: 'datalistRecord/:id/:datalistId', element: <DatalistRecord /> },
              { path: 'feature/:id', element: <FeatureList /> },
              { path: 'ruleList/:id/:strategyId', element: <RuleList /> },
              { path: 'strategyDetail/:id/:strategyId', element: <StrategyDetail /> },
              { path: 'historyRecordList/:id/:strategyId/:ruleId', element: <HistoryRecordList /> },
            ],
          },
          { path: 'report', element: <Navigate to="/event" replace /> },
          {
            // 报表工作区框架（向子页注入场景选择等 props，见 Report 的 Outlet context）
            element: <Report />,
            children: [
              { path: 'event', element: <ListEvent /> },
              { path: 'graph', element: <RuleGraph /> },
              { path: 'rule', element: <ListRule /> },
              { path: 'ruleid/:scenarioId/:ruleId/:strategyName', element: <ListEvent /> },
              { path: 'dashboard', element: <DashBoard /> },
            ],
          },
          { path: 'config', element: <ConfigCenter /> },
          { path: 'plugins', element: <PluginManagement /> },
        ],
      },
      { path: '*', element: <Index />, children: [{ index: true, element: <NotFound /> }] },
    ],
  },
]);

createRoot(document.getElementById('react-content')).render(
  // antd5 默认 locale 为 en_US，显式切回中文（与 antd3 时代观感一致）
  <ConfigProvider locale={zhCN}>
    <RouterProvider router={router} />
  </ConfigProvider>
);
