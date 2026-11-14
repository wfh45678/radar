import React from 'react';
import { createRoot } from 'react-dom/client';
import { createHashRouter, RouterProvider, Navigate, Outlet, useParams } from 'react-router';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

dayjs.locale('zh-cn');

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
import DashBoard from './component/report/dashboard';
import ConfigCenter from './component/config/ConfigCenter';
import ModelConfig from './component/modelconfig/ModelConfig';

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

// /model/:id 默认跳转：等价原 IndexRedirect to="/field/:id"（复用同一个 id）
function ModelIndexRedirect() {
  const { id } = useParams();
  return <Navigate to={`/field/${id}`} replace />;
}

// URL 结构与 2022 版（react-router 3 绝对 path，扁平 URL）保持一致：
// #/modelList、#/field/:id、#/event、#/ruleid/:modelId/:ruleId/:activationName 等。
// Index / Model / Report 是嵌套的 pathless layout route，仅承担组件层级，不往 URL 加前缀。
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
          { path: 'modelList', element: <ModelList /> },
          { path: 'model/:id', element: <ModelIndexRedirect /> },
          {
            // 模型工作区框架（原 rr3 中 /model/:id 的布局父组件）
            element: <Model />,
            children: [
              { path: 'field/:id', element: <Field /> },
              { path: 'activation/:id', element: <Activation /> },
              { path: 'datalist/:id', element: <Datalist /> },
              { path: 'datalistRecord/:id/:datalistId', element: <DatalistRecord /> },
              { path: 'preItem/:id', element: <PreItem /> },
              { path: 'ruleList/:id/:activationId', element: <RuleList /> },
              { path: 'historyRecordList/:id/:activationId/:ruleId', element: <HistoryRecordList /> },
              { path: 'abstractionList/:id', element: <AbstractionList /> },
              { path: 'modelConfig/:id', element: <ModelConfig /> },
            ],
          },
          { path: 'report', element: <Navigate to="/event" replace /> },
          {
            // 报表工作区框架（向子页注入模型选择等 props，见 Report 的 Outlet context）
            element: <Report />,
            children: [
              { path: 'event', element: <ListEvent /> },
              { path: 'graph', element: <RuleGraph /> },
              { path: 'rule', element: <ListRule /> },
              { path: 'ruleid/:modelId/:ruleId/:activationName', element: <ListEvent /> },
              { path: 'dashboard', element: <DashBoard /> },
            ],
          },
          { path: 'config', element: <ConfigCenter /> },
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
