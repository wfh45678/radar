import React from 'react';
import { createRoot } from 'react-dom/client';
import { createHashRouter, RouterProvider, Navigate, Outlet, useParams } from 'react-router';

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

// /model/:id 默认子路由：等价原 IndexRedirect to="/field/:id"（复用同一个 id）
function ModelIndexRedirect() {
  const { id } = useParams();
  return <Navigate to={`/model/${id}/field/${id}`} replace />;
}

// URL 结构与 2022 版（react-router 3 绝对 path）保持一致：
// #/modelList、#/model/:id/field/:id、#/report/event 等，Index 头部用 pathless layout route 承载
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
          {
            path: 'model/:id',
            element: <Model />,
            children: [
              { index: true, element: <ModelIndexRedirect /> },
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
          {
            path: 'report',
            element: <Report />,
            children: [
              { index: true, element: <Navigate to="/report/event" replace /> },
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
      { path: '*', element: <NotFound /> },
    ],
  },
]);

createRoot(document.getElementById('react-content')).render(<RouterProvider router={router} />);
